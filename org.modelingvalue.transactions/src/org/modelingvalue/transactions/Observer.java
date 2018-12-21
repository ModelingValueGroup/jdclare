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

import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Concurrent;
import org.modelingvalue.collections.util.TraceTimer;

public class Observer extends Leaf {

    private static final Observerds[] OBSERVEDS;
    static {
        OBSERVEDS = new Observerds[Priority.values().length];
        for (int i = 0; i < OBSERVEDS.length; i++) {
            OBSERVEDS[i] = Observerds.of(Priority.values()[i]);
        }
    }

    public static Observer of(Object id, Compound parent, Runnable action) {
        return of(id, parent, action, Priority.high);
    }

    public static Observer of(Object id, Compound parent, Runnable action, Priority initPrio) {
        return new Observer(id, parent, action, initPrio);
    }

    private Concurrent<Set<Slot>> getted = Concurrent.of();
    private Concurrent<Set<Slot>> setted = Concurrent.of();
    private long                  count  = -1;
    private int                   changes;

    public Observer(Object id, Compound parent, Runnable action, Priority initPrio) {
        super(id, parent, action, initPrio);
    }

    @Override
    public <O, T> T get(O object, Getable<O, T> property) {
        T result = super.get(object, property);
        observe(object, property, false);
        return result;
    }

    @Override
    public <O, T, E> T set(O object, Setable<O, T> property, BiFunction<T, E, T> function, E element) {
        T pre = super.set(object, property, function, element);
        observe(object, property, true);
        return pre;
    }

    @Override
    public <O, T> T set(O object, Setable<O, T> property, T value) {
        T pre = super.set(object, property, value);
        observe(object, property, true);
        return pre;
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
        return "observerRun";
    }

    @Override
    public State apply(final State pre) {
        TraceTimer.traceBegin("observer");
        try {
            getted.init(Set.of());
            setted.init(Set.of());
            State post = super.apply(pre);
            if (post != pre) {
                countChanges();
                init(post);
                getted.clear();
                setted.clear();
            } else {
                init(pre);
                setObserveds(setted.result(), getted.result());
            }
            return result();
        } catch (EmptyMandatoryException soe) {
            if (isChanged()) {
                countChanges();
            }
            clear();
            init(pre);
            setObserveds(setted.result(), getted.result());
            return result();
        } catch (StopObserverException soe) {
            init(result());
            getted.clear();
            setted.clear();
            setObserveds(Set.of(), Set.of());
            return result();
        } finally {
            clear();
            getted.clear();
            setted.clear();
            TraceTimer.traceEnd("observer");
        }
    }

    private void setObserveds(Set<Slot> sets, Set<Slot> gets) {
        CURRENT.run(this, () -> {
            OBSERVEDS[2].set(this, sets);
            if (initPrio() == Priority.first) {
                OBSERVEDS[0].set(this, gets.removeAll(sets));
            } else {
                OBSERVEDS[1].set(this, gets.removeAll(sets));
            }
        });
    }

    private void countChanges() {
        Root root = root();
        int totalChanges = root.countTotalChanges();
        long runCount = root.runCount();
        if (runCount > count) {
            count = runCount;
            changes = 1;
        } else if (changes++ > root.maxNrOfChanges * 2) {
            throw new TooManyChangesException("Changes: " + changes + ", running: " + root.preState().get(this::toString));
        } else if (totalChanges > root.maxTotalNrOfChanges + root.maxNrOfChanges) {
            throw new TooManyChangesException("Total changes: " + totalChanges + ", running: " + root.preState().get(this::toString));
        }
    }

    @Override
    protected <O, T> void changed(O object, Setable<O, T> property, T preValue, T postValue) {
        super.changed(object, property, preValue, postValue);
        set(parent, Priority.low.triggered, Set::add, this);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void trigger(Set<Observer> leafs, Priority prio, Object object, Observed observed, Object pre, Object post) {
        super.trigger(leafs, prio, object, observed, pre, post);
        Root root = root();
        int totalChanges = root.totalChanges();
        if (changes > root.maxNrOfChanges || totalChanges > root.maxTotalNrOfChanges) {
            System.err.println("ERROR: Too many changes: " + (totalChanges > root.maxTotalNrOfChanges ? totalChanges : changes) + "\n       Running: " + root.preState().get(() -> this + "\n       Change: " + object + "." + observed + "=" + pre + " -> " + post + "\n       Triggers: " + leafs.toString().substring(3)));
        }
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
