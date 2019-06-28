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
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Concurrent;
import org.modelingvalue.collections.util.Context;
import org.modelingvalue.collections.util.TraceTimer;
import org.modelingvalue.transactions.Observer.Observerds;

public class ObserverTransaction extends ActionTransaction {

    public static final Context<Boolean>                  OBSERVE = Context.of(true);

    @SuppressWarnings("rawtypes")
    private final Concurrent<Map<Observed, Set<Mutable>>> getted  = Concurrent.of();
    @SuppressWarnings("rawtypes")
    private final Concurrent<Map<Observed, Set<Mutable>>> setted  = Concurrent.of();

    private boolean                                       changed;

    protected ObserverTransaction(UniverseTransaction universeTransaction) {
        super(universeTransaction);
    }

    public final Observer<?> observer() {
        return (Observer<?>) action();
    }

    @Override
    protected String traceId() {
        return "observer";
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void run(State pre, UniverseTransaction universeTransaction) {
        Observer<?> observer = observer();
        try {
            long rootCount = universeTransaction.runCount();
            if (observer.runCount != rootCount) {
                observer.runCount = rootCount;
                observer.changes = 0;
                observer.stopped = false;
            }
            if (observer.stopped || universeTransaction.isKilled()) {
                return;
            }
            getted.init(Observed.OBSERVED_MAP);
            setted.init(Observed.OBSERVED_MAP);
            super.run(pre, universeTransaction);
            Map<Observed, Set<Mutable>> gets = getted.result();
            Map<Observed, Set<Mutable>> sets = setted.result();
            if (changed) {
                checkTooManyChanges(pre, sets, gets);
            }
            observe(observer, sets, gets);
        } catch (EmptyMandatoryException soe) {
            clear();
            init(pre);
            observe(observer, setted.result(), getted.result());
        } catch (StopObserverException soe) {
            observe(observer, Observed.OBSERVED_MAP, Observed.OBSERVED_MAP);
        } finally {
            changed = false;
            getted.clear();
            setted.clear();
            TraceTimer.traceEnd("observer");
        }
    }

    @SuppressWarnings("rawtypes")
    private void observe(Observer<?> observer, Map<Observed, Set<Mutable>> sets, Map<Observed, Set<Mutable>> gets) {
        gets = gets.removeAll(sets, Set::removeAll);
        Mutable mutable = parent().mutable();
        Observerds[] observeds = observer.observeds();
        Map<Observed, Set<Mutable>> oldGets = observeds[Direction.forward.nr].set(mutable, gets);
        Map<Observed, Set<Mutable>> oldSets = observeds[Direction.backward.nr].set(mutable, sets);
        if (oldGets.isEmpty() && oldSets.isEmpty() && !(sets.isEmpty() && gets.isEmpty())) {
            observer.instances++;
        } else if (!(oldGets.isEmpty() && oldSets.isEmpty()) && sets.isEmpty() && gets.isEmpty()) {
            observer.instances--;
        }
        checkTooManyObserved(sets, gets);
    }

    @SuppressWarnings("rawtypes")
    protected void checkTooManyObserved(Map<Observed, Set<Mutable>> sets, Map<Observed, Set<Mutable>> gets) {
        if (universeTransaction().maxNrOfObserved() < size(gets) + size(sets)) {
            throw new TooManyObservedException(parent().mutable(), observer(), gets.addAll(sets, Set::addAll), universeTransaction());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void checkTooManyChanges(State pre, Map<Observed, Set<Mutable>> sets, Map<Observed, Set<Mutable>> gets) {
        UniverseTransaction universeTransaction = universeTransaction();
        Observer<?> observer = observer();
        Mutable mutable = parent().mutable();
        if (universeTransaction.isDebugging()) {
            State post = result();
            init(post);
            Set<ObserverTrace> traces = observer.traces.get(mutable);
            ObserverTrace trace = new ObserverTrace(mutable, observer, traces.sorted().findFirst().orElse(null), observer.changesPerInstance(), //
                    gets.addAll(sets, Set::addAll).flatMap(e -> e.getValue().map(m -> {
                        m = m.resolve(mutable);
                        return Entry.of(ObservedInstance.of(m, e.getKey()), pre.get(m, e.getKey()));
                    })).toMap(e -> e), //
                    sets.flatMap(e -> e.getValue().map(m -> {
                        m = m.resolve(mutable);
                        return Entry.of(ObservedInstance.of(m, e.getKey()), post.get(m, e.getKey()));
                    })).toMap(e -> e));
            observer.traces.set(mutable, traces.add(trace));
        }
        int totalChanges = universeTransaction.countTotalChanges();
        int changesPerInstance = observer.countChangesPerInstance();
        if (changesPerInstance > universeTransaction.maxNrOfChanges()) {
            universeTransaction.setDebugging();
            if (changesPerInstance > universeTransaction.maxNrOfChanges() * 2) {
                hadleTooManyChanges(universeTransaction, mutable, observer, changesPerInstance);
            }
        } else if (totalChanges > universeTransaction.maxTotalNrOfChanges()) {
            universeTransaction.setDebugging();
            if (totalChanges > universeTransaction.maxTotalNrOfChanges() + universeTransaction.maxNrOfChanges()) {
                hadleTooManyChanges(universeTransaction, mutable, observer, totalChanges);
            }
        }
    }

    private void hadleTooManyChanges(UniverseTransaction universeTransaction, Mutable mutable, Observer<?> observer, int changes) {
        State result = result();
        init(result);
        ObserverTrace last = result.get(mutable, observer.traces).sorted().findFirst().orElse(null);
        if (last != null && last.done().size() >= (changes > universeTransaction.maxTotalNrOfChanges() ? 1 : universeTransaction.maxNrOfChanges())) {
            getted.init(Observed.OBSERVED_MAP);
            setted.init(Observed.OBSERVED_MAP);
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
        T value = super.get(object, property);
        observe(object, property, value, false);
        return value;
    }

    @Override
    public <O, T> T pre(O object, Getable<O, T> property) {
        T value = super.pre(object, property);
        observe(object, property, value, false);
        return value;
    }

    @Override
    public <O, T, E> T set(O object, Setable<O, T> property, BiFunction<T, E, T> function, E element) {
        T value = super.set(object, property, function, element);
        observe(object, property, value, true);
        return value;
    }

    @Override
    public <O, T> T set(O object, Setable<O, T> property, T value) {
        observe(object, property, value, true);
        return super.set(object, property, value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <O, T> void observe(O object, Getable<O, T> property, T value, boolean set) {
        if (object instanceof Mutable && property instanceof Observed && getted.isInitialized() && OBSERVE.get()) {
            (set ? setted : getted).change(o -> o.add(((Observed) property).entry((Mutable) object, parent().mutable()), Set::addAll));
        }
    }

    @Override
    public void runNonObserving(Runnable action) {
        if (getted.isInitialized()) {
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
