//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018 Modeling Value Group B.V. (http://modelingvalue.org)                                             ~
//                                                                                                                     ~
// Licensed under the GNU Lesser General Public License v3.0 (the "License"). You may not use this file except in      ~
// compliance with the License. You may obtain a copy of the License at: https://choosealicense.com/licenses/lgpl-3.0  ~
// Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on ~
// an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the  ~
// specific language governing permissions and limitations under the License.                                          ~
//                                                                                                                     ~
// Contributors:                                                                                                       ~
//     Wim Bast, Carel Bast, Tom Brus, Arjan Kok, Ronald Krijgsheld                                                    ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package org.modelingvalue.transactions;

import java.util.function.BiFunction;

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Concurrent;
import org.modelingvalue.collections.util.Context;
import org.modelingvalue.collections.util.TraceTimer;
import org.modelingvalue.transactions.Rule.Observerds;

public class Observer extends Leaf {

    private static final Context<Boolean> OBSERVE = Context.of(true);

    public static Observer of(Rule rule, Compound parent, Runnable action) {
        return of(rule, parent, action, Direction.forward, Priority.postDepth);
    }

    public static Observer of(Rule rule, Compound parent, Runnable action, Priority priority) {
        return of(rule, parent, action, Direction.forward, priority);
    }

    public static Observer of(Rule rule, Compound parent, Runnable action, Direction initDirection, Priority priority) {
        return new Observer(rule, parent, action, initDirection, priority);
    }

    public Observer(Rule rule, Compound parent, Runnable action, Direction initDirection, Priority priority) {
        super(rule, parent, action, initDirection, priority);
    }

    public Rule rule() {
        return (Rule) getId();
    }

    @Override
    protected String traceId() {
        return "observer";
    }

    @Override
    protected void run(LeafRun<?> leaf, State pre, Root root) {
        Rule rule = rule();
        ObserverRun run = (ObserverRun) leaf;
        try {
            long rootCount = root.runCount();
            if (rule.runCount < rootCount) {
                rule.runCount = rootCount;
                rule.changes = 0;
                rule.stopped = false;
            }
            if (rule.stopped || root.isKilled()) {
                return;
            }
            run.getted.init(Set.of());
            run.setted.init(Set.of());
            super.run(run, pre, root);
            Set<Slot> gets = run.getted.result();
            Set<Slot> sets = run.setted.result();
            if (run.changed) {
                checkTooManyChanges(run, pre, sets, gets);
            }
            observe(run, rule, sets, gets);
        } catch (EmptyMandatoryException soe) {
            run.clear();
            run.init(pre);
            observe(run, rule, run.setted.result(), run.getted.result());
        } catch (StopObserverException soe) {
            observe(run, rule, Set.of(), Set.of());
        } finally {
            run.changed = false;
            run.getted.clear();
            run.setted.clear();
            TraceTimer.traceEnd("observer");
        }
    }

    private void observe(ObserverRun run, Rule rule, Set<Slot> sets, Set<Slot> gets) {
        gets = gets.removeAll(sets);
        Observerds[] observeds = rule.observeds();
        Set<Slot> oldGets = observeds[Direction.forward.nr].set(parent.getId(), gets);
        Set<Slot> oldSets = observeds[Direction.backward.nr].set(parent.getId(), sets);
        if (oldGets.isEmpty() && oldSets.isEmpty() && !(sets.isEmpty() && gets.isEmpty())) {
            rule.instances++;
        } else if (!(oldGets.isEmpty() && oldSets.isEmpty()) && sets.isEmpty() && gets.isEmpty()) {
            rule.instances--;
        }
        checkTooManyObserved(run, sets, gets);
    }

    protected void checkTooManyObserved(ObserverRun run, Set<Slot> sets, Set<Slot> gets) {
        if (run.root().maxNrOfObserved() < gets.size() + sets.size()) {
            throw new TooManyObservedException(this, gets.addAll(sets));
        }
    }

    @SuppressWarnings("unchecked")
    protected void checkTooManyChanges(ObserverRun run, State pre, Set<Slot> sets, Set<Slot> gets) {
        Root root = run.root();
        Rule rule = rule();
        if (root.isDebugging()) {
            State post = run.result();
            run.init(post);
            Set<ObserverTrace> traces = rule.traces.get(parent.getId());
            ObserverTrace trace = new ObserverTrace(this, traces.sorted().findFirst().orElse(null), rule.changesPerInstance(), //
                    gets.addAll(sets).toMap(s -> Entry.of(s, pre.get(s.object(), s.property()))), //
                    sets.toMap(s -> Entry.of(s, post.get(s.object(), s.property()))));
            rule.traces.set(parent.getId(), traces.add(trace));
        }
        int totalChanges = root.countTotalChanges();
        int changesPerInstance = rule.countChangesPerInstance();
        if (changesPerInstance > root.maxNrOfChanges()) {
            root.setDebugging();
            if (changesPerInstance > root.maxNrOfChanges() * 2) {
                hadleTooManyChanges(run, rule, changesPerInstance);
            }
        } else if (totalChanges > root.maxTotalNrOfChanges()) {
            root.setDebugging();
            if (totalChanges > root.maxTotalNrOfChanges() + root.maxNrOfChanges()) {
                hadleTooManyChanges(run, rule, totalChanges);
            }
        }
    }

    private void hadleTooManyChanges(ObserverRun run, Rule rule, int changes) {
        State result = run.result();
        run.init(result);
        ObserverTrace last = result.get(parent.getId(), rule.traces).sorted().findFirst().get();
        if (last.done().size() >= run.root().maxNrOfChanges()) {
            run.getted.init(Set.of());
            run.setted.init(Set.of());
            throw new TooManyChangesException(result, last, changes);
        }
    }

    @SuppressWarnings("rawtypes")
    protected void countChanges(ObserverRun run, Observed observed) {
        run.changed = true;
    }

    @Override
    protected ObserverRun startRun(Root root) {
        return root().observerRuns.get().open(this, root);
    }

    @Override
    protected void stopRun(TransactionRun<?> run) {
        root().observerRuns.get().close((ObserverRun) run);
    }

    public static class ObserverRun extends LeafRun<Observer> {

        private final Concurrent<Set<Slot>> getted = Concurrent.of();
        private final Concurrent<Set<Slot>> setted = Concurrent.of();

        private boolean                     changed;

        @Override
        public <O, T> T get(O object, Getable<O, T> property) {
            observe(object, property, false);
            return super.get(object, property);
        }

        @Override
        public <O, T> T pre(O object, Getable<O, T> property) {
            observe(object, property, false);
            return super.pre(object, property);
        }

        @Override
        public <O, T, E> T set(O object, Setable<O, T> property, BiFunction<T, E, T> function, E element) {
            observe(object, property, true);
            return super.set(object, property, function, element);
        }

        @Override
        public <O, T> T set(O object, Setable<O, T> property, T value) {
            observe(object, property, true);
            return super.set(object, property, value);
        }

        @SuppressWarnings("rawtypes")
        private <O, T> void observe(O object, Getable<O, T> property, boolean set) {
            if (property instanceof Observed && getted.isInitialized() && setted.isInitialized() && OBSERVE.get()) {
                Slot slot = Slot.of(object, (Observed) property);
                if (set) {
                    setted.change(o -> o.add(slot));
                } else {
                    getted.change(o -> o.add(slot));
                }
            }
        }

        @Override
        public void runNonObserving(Runnable action) {
            if (getted.isInitialized() && setted.isInitialized()) {
                OBSERVE.run(false, action);
            } else {
                super.runNonObserving(action);
            }

        }

        @SuppressWarnings("rawtypes")
        @Override
        protected <O, T> void changed(O object, Setable<O, T> setable, T preValue, T postValue) {
            runNonObserving(() -> super.changed(object, setable, preValue, postValue));
            if (setable instanceof Observed) {
                transaction().countChanges(this, (Observed) setable);
                trigger(transaction(), Direction.backward);
            }
        }

    }

}
