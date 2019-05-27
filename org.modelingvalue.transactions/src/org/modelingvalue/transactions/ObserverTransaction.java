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
import org.modelingvalue.transactions.Observer.Observerds;

public class ObserverTransaction extends ActionTransaction {

    public static final Context<Boolean>            OBSERVE = Context.of(true);

    private final Concurrent<Set<ObservedInstance>> getted  = Concurrent.of();
    private final Concurrent<Set<ObservedInstance>> setted  = Concurrent.of();

    private boolean                                 changed;

    protected ObserverTransaction(UniverseTransaction universeTransaction) {
        super(universeTransaction);
    }

    public Observer<?> observer() {
        return (Observer<?>) action();
    }

    @Override
    protected String traceId() {
        return "observer";
    }

    @Override
    protected void run(State pre, UniverseTransaction universeTransaction) {
        Observer<?> observer = observer();
        try {
            long rootCount = universeTransaction.runCount();
            if (observer.runCount < rootCount) {
                observer.runCount = rootCount;
                observer.changes = 0;
                observer.stopped = false;
            }
            if (observer.stopped || universeTransaction.isKilled()) {
                return;
            }
            getted.init(Set.of());
            setted.init(Set.of());
            super.run(pre, universeTransaction);
            Set<ObservedInstance> gets = getted.result();
            Set<ObservedInstance> sets = setted.result();
            if (changed) {
                checkTooManyChanges(pre, sets, gets);
            }
            observe(observer, sets, gets);
        } catch (EmptyMandatoryException soe) {
            clear();
            init(pre);
            observe(observer, setted.result(), getted.result());
        } catch (StopObserverException soe) {
            observe(observer, Set.of(), Set.of());
        } finally {
            changed = false;
            getted.clear();
            setted.clear();
            TraceTimer.traceEnd("observer");
        }
    }

    private void observe(Observer<?> observer, Set<ObservedInstance> sets, Set<ObservedInstance> gets) {
        gets = gets.removeAll(sets);
        Observerds[] observeds = observer.observeds();
        Mutable mutable = parent().mutable();
        Set<ObservedInstance> oldGets = observeds[Direction.forward.nr].set(mutable, gets);
        Set<ObservedInstance> oldSets = observeds[Direction.backward.nr].set(mutable, sets);
        if (oldGets.isEmpty() && oldSets.isEmpty() && !(sets.isEmpty() && gets.isEmpty())) {
            observer.instances++;
        } else if (!(oldGets.isEmpty() && oldSets.isEmpty()) && sets.isEmpty() && gets.isEmpty()) {
            observer.instances--;
        }
        checkTooManyObserved(sets, gets);
    }

    protected void checkTooManyObserved(Set<ObservedInstance> sets, Set<ObservedInstance> gets) {
        if (universeTransaction().maxNrOfObserved() < gets.size() + sets.size()) {
            throw new TooManyObservedException(parent().mutable(), observer(), gets.addAll(sets), universeTransaction());
        }
    }

    @SuppressWarnings("unchecked")
    protected void checkTooManyChanges(State pre, Set<ObservedInstance> sets, Set<ObservedInstance> gets) {
        UniverseTransaction universeTransaction = universeTransaction();
        Observer<?> observer = observer();
        Mutable mutable = parent().mutable();
        if (universeTransaction.isDebugging()) {
            State post = result();
            init(post);
            Set<ObserverTrace> traces = observer.traces.get(mutable);
            ObserverTrace trace = new ObserverTrace(mutable, observer, traces.sorted().findFirst().orElse(null), observer.changesPerInstance(), //
                    gets.addAll(sets).toMap(s -> Entry.of(s, pre.get(s.object(), s.property()))), //
                    sets.toMap(s -> Entry.of(s, post.get(s.object(), s.property()))));
            observer.traces.set(mutable, traces.add(trace));
        }
        int totalChanges = universeTransaction.countTotalChanges();
        int changesPerInstance = observer.countChangesPerInstance();
        if (changesPerInstance > universeTransaction.maxNrOfChanges()) {
            universeTransaction.setDebugging();
            if (changesPerInstance > universeTransaction.maxNrOfChanges() * 2) {
                hadleTooManyChanges(mutable, observer, changesPerInstance);
            }
        } else if (totalChanges > universeTransaction.maxTotalNrOfChanges()) {
            universeTransaction.setDebugging();
            if (totalChanges > universeTransaction.maxTotalNrOfChanges() + universeTransaction.maxNrOfChanges()) {
                hadleTooManyChanges(mutable, observer, totalChanges);
            }
        }
    }

    private void hadleTooManyChanges(Mutable mutable, Observer<?> observer, int changes) {
        State result = result();
        init(result);
        ObserverTrace last = result.get(mutable, observer.traces).sorted().findFirst().orElse(null);
        if (last != null && last.done().size() >= universeTransaction().maxNrOfChanges()) {
            getted.init(Set.of());
            setted.init(Set.of());
            observer.stopped = true;
            throw new TooManyChangesException(result, last, changes);
        }
    }

    @SuppressWarnings("rawtypes")
    protected void countChanges(Observed observed) {
        changed = true;
    }

    @Override
    public <O, T> T get(O object, Getable<O, T> property) {
        if (property instanceof Observed && Constant.DERIVED.get() != null && ObserverTransaction.OBSERVE.get()) {
            throw new NonDeterministicException("Reading observed '" + property + "' while initializing constant '" + Constant.DERIVED.get() + "'");
        }
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
            ObservedInstance observedInstance = ObservedInstance.of(object, (Observed) property);
            if (set) {
                setted.change(o -> o.add(observedInstance));
            } else {
                getted.change(o -> o.add(observedInstance));
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected <O, T> void changed(O object, Setable<O, T> setable, T preValue, T postValue) {
        runNonObserving(() -> super.changed(object, setable, preValue, postValue));
        if (setable instanceof Observed) {
            countChanges((Observed) setable);
            trigger(parent().mutable(), (Observer<Mutable>) observer(), Direction.backward);
        }
    }

}
