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
import org.modelingvalue.collections.util.TraceTimer;

public class Observer extends Leaf {

    public static final Setable<Observer, Set<ObserverRun>> RUNS = Setable.of("RUNS", Set.of());

    private static final Observerds[]                       OBSERVEDS;
    static {
        OBSERVEDS = new Observerds[Priority.values().length];
        for (int i = 0; i < OBSERVEDS.length; i++) {
            OBSERVEDS[i] = Observerds.of(Priority.values()[i]);
        }
    }

    public static Observer of(Object id, Compound parent, Runnable action) {
        return of(id, parent, action, Priority.mid);
    }

    public static Observer of(Object id, Compound parent, Runnable action, Priority initPrio) {
        return new Observer(id, parent, action, initPrio);
    }

    private final Concurrent<Set<Slot>> getted    = Concurrent.of();
    private final Concurrent<Set<Slot>> setted    = Concurrent.of();
    private long                        runCount  = -1;
    private int                         changes;
    private boolean                     changed;
    private boolean                     stopped;
    private boolean                     firstTime = true;

    public Observer(Object id, Compound parent, Runnable action, Priority initPrio) {
        super(id, parent, action, initPrio);
    }

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
        if (property instanceof Observed) {
            Slot slot = Slot.of(object, (Observed) property);
            if (set) {
                setted.change(o -> o.add(slot));
            } else {
                getted.change(o -> o.add(slot));
            }
        }
    }

    @Override
    protected String traceId() {
        return "observer";
    }

    public boolean firstTime() {
        return this.firstTime;
    }

    @Override
    protected void run(State pre, Root root) {
        try {
            long rootCount = root.runCount();
            if (runCount < rootCount) {
                runCount = rootCount;
                changes = 0;
                stopped = false;
                firstTime = true;
            }
            if (stopped || root.isKilled()) {
                return;
            }
            getted.init(Set.of());
            setted.init(Set.of());
            super.run(pre, root);
            Set<Slot> gets = getted.result();
            Set<Slot> sets = setted.result();
            if (changed) {
                checkTooManyChanges(pre, root, sets, gets);
            }
            observe(root, sets, gets);
        } catch (EmptyMandatoryException soe) {
            clear();
            init(pre);
            observe(root, setted.result(), getted.result());
        } catch (StopObserverException soe) {
            stopped = true;
            observe(root, Set.of(), Set.of());
        } finally {
            changed = false;
            firstTime = false;
            getted.clear();
            setted.clear();
            TraceTimer.traceEnd("observer");
        }
    }

    private void observe(Root root, Set<Slot> sets, Set<Slot> gets) {
        OBSERVEDS[2].set(this, sets);
        if (initPrio() == Priority.high) {
            OBSERVEDS[0].set(this, gets.removeAll(sets));
        } else {
            OBSERVEDS[1].set(this, gets.removeAll(sets));
        }
    }

    @SuppressWarnings("unchecked")
    protected void checkTooManyChanges(State pre, Root root, Set<Slot> sets, Set<Slot> gets) {
        if (root.isDebugging()) {
            State post = result();
            init(post);
            Set<ObserverRun> runs = RUNS.get(this);
            ObserverRun run = new ObserverRun(this, runs.sorted().findFirst().orElse(null), changes, //
                    gets.addAll(sets).toMap(s -> Entry.of(s, pre.get(s.object(), s.property()))), //
                    sets.toMap(s -> Entry.of(s, post.get(s.object(), s.property()))));
            RUNS.set(this, runs.add(run));
        }
        int totalChanges = root.countTotalChanges();
        if (++changes > root.maxNrOfChanges()) {
            root.setDebugging();
            if (changes > root.maxNrOfChanges() * 2) {
                hadleTooManyChanges(root, changes);
            }
        } else if (totalChanges > root.maxTotalNrOfChanges()) {
            root.setDebugging();
            if (totalChanges > root.maxTotalNrOfChanges() + root.maxNrOfChanges()) {
                hadleTooManyChanges(root, totalChanges);
            }
        }
    }

    private void hadleTooManyChanges(Root root, int changes) {
        State result = result();
        init(result);
        ObserverRun last = result.get(this, Observer.RUNS).sorted().findFirst().get();
        if (last.done().size() >= root.maxNrOfChanges()) {
            getted.init(Set.of());
            setted.init(Set.of());
            throw new TooManyChangesException(result, last, changes);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected <O, T> void changed(O object, Setable<O, T> setable, T preValue, T postValue) {
        super.changed(object, setable, preValue, postValue);
        if (setable instanceof Observed) {
            countChanges((Observed) setable);
            trigger(this, Priority.low);
        }
    }

    @SuppressWarnings("rawtypes")
    protected void countChanges(Observed observed) {
        changed = true;
    }

    private static final class Observerds extends Setable<Observer, Set<Slot>> {

        public static Observerds of(Priority prio) {
            return new Observerds(prio, prio);
        }

        @SuppressWarnings("unchecked")
        private Observerds(Object id, Priority prio) {
            super(id, Set.of(), ($, constr, pre, post) -> pre.compare(post).forEach(d -> {
                if (d[0] == null) {
                    d[1].forEach(n -> $.set(n.object(), n.property().observers(prio), Set<Leaf>::add, constr));
                }
                if (d[1] == null) {
                    d[0].forEach(o -> $.set(o.object(), o.property().observers(prio), Set<Leaf>::remove, constr));
                }
            }));
        }

    }

    @Override
    public void runNonObserving(Runnable action) {
        Set<Slot> s = setted.get();
        Set<Slot> g = getted.get();
        try {
            super.runNonObserving(action);
        } finally {
            setted.set(s);
            getted.set(g);
        }
    }

}
