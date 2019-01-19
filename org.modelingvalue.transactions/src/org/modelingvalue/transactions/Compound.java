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

import java.util.Arrays;
import java.util.Objects;

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Concurrent;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.TraceTimer;
import org.modelingvalue.transactions.Observed.Observers;

public class Compound extends Transaction {

    private static final int                                   MAX_STACK_DEPTH    = Integer.getInteger("MAX_STACK_DEPTH", 4);

    public static final Setable<Compound, Set<Compound>>       SCHEDULED_COMPOUND = Setable.of("scheduledCompound", Set.of());

    @SuppressWarnings("unchecked")
    private static final Setable<Compound, Set<Transaction>>[] SCHEDULED          =                                           //
            new Setable[]{Priority.first.scheduled, SCHEDULED_COMPOUND, Priority.high.scheduled, Priority.low.scheduled};

    public static Compound of(Object id, Compound parent) {
        return new Compound(id, parent);
    }

    private final Concurrent<Set<Observer>>[]             triggered;
    @SuppressWarnings("rawtypes")
    private final Concurrent<Set<Pair<Object, Observed>>> conflicts;
    private final ReadOnly                                merger;

    @SuppressWarnings("unchecked")
    protected Compound(Object id, Compound parent) {
        super(id, parent);
        triggered = new Concurrent[Priority.values().length];
        for (int i = 0; i < triggered.length; i++) {
            triggered[i] = Concurrent.of();
        }
        conflicts = Concurrent.of();
        merger = ReadOnly.of(Pair.of(this, "merger"), root());
        if (parent != null && parent.ancestorId(id)) {
            throw new Error("Cyclic Compound Transaction id " + id);
        }
    }

    Compound(Object id) {
        this(id, null);
    }

    public boolean ancestorId(Object id) {
        return getId().equals(id) || (parent != null && parent.ancestorId(id));
    }

    @Override
    public boolean isAncestorOf(Transaction child) {
        while (!equals(child)) {
            child = child.parent();
            if (child == null) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public State apply(State state) {
        TraceTimer.traceBegin("compound");
        Set<Transaction>[] ts = new Set[1];
        State[] sa = new State[]{state};
        int i;
        try {
            do {
                for (i = 0; i == 0 || (ts[0].isEmpty() && i < SCHEDULED.length); i++) {
                    sa[0] = sa[0].set(this, SCHEDULED[i], Set.of(), ts);
                }
                if (!ts[0].isEmpty()) {
                    sa[0] = scheduleTriggered(merge(sa[0], ts[0].reduce(sa, (s, t) -> {
                        State[] r = s.clone();
                        r[0] = t.apply(s[0]);
                        return r;
                    }, (a, b) -> {
                        State[] r = Arrays.copyOf(a, a.length + b.length);
                        System.arraycopy(b, 0, r, a.length, b.length);
                        return r;
                    })));
                }
            } while (!ts[0].isEmpty());
            return sa[0];
        } catch (TooManyChangesException tmce) {
            throw tmce;
        } catch (Throwable t) {
            Error error = new TransactionException("Exception in transaction \"" + state.get(() -> toString()) + "\"", t);
            StackTraceElement[] est = error.getStackTrace();
            error.setStackTrace(Arrays.copyOf(est, Math.min(est.length, MAX_STACK_DEPTH)));
            if (!(t instanceof TransactionException)) {
                est = error.getStackTrace();
                StackTraceElement[] tst = t.getStackTrace();
                t.setStackTrace(Arrays.copyOf(tst, Math.min(tst.length, t.getCause() instanceof TransactionException ? MAX_STACK_DEPTH : reduceStackLength(est, tst))));
            }
            throw error;
        } finally {
            TraceTimer.traceEnd("compound");
        }
    }

    private int reduceStackLength(StackTraceElement[] outer, StackTraceElement[] inner) {
        for (int i = 0; i < inner.length; i++) {
            for (int o = 0; o < outer.length; o++) {
                if (inner[i].equals(outer[o])) {
                    return i + 2;
                }
            }
        }
        return inner.length;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private State merge(State base, State[] branches) {
        TraceTimer.traceBegin("merge");
        try {
            return merger.get(() -> {
                for (int i = 0; i < triggered.length; i++) {
                    triggered[i].init(Set.of());
                }
                conflicts.init(Set.of());
                State state = base.merge((ps, psm, psbs) -> {
                    for (Entry<Setable, Object> p : psm) {
                        if (p.getKey() instanceof Observers) {
                            Observers<?, ?> observersProp = (Observers) p.getKey();
                            Set<Observer> baseObservers = State.get(ps, observersProp);
                            Set<Observer> observers = (Set) p.getValue();
                            observers = observers.removeAll(baseObservers);
                            if (!observers.isEmpty()) {
                                Observed<?, ?> observedProp = observersProp.observed();
                                Object baseValue = State.get(ps, observedProp);
                                for (Map<Setable, Object> psb : psbs) {
                                    if (!Objects.equals(State.get(psb, observedProp), baseValue)) {
                                        Set<Observer> addedObservers = observers.removeAll(State.get(psb, observersProp));
                                        if (!addedObservers.isEmpty()) {
                                            triggered[observersProp.prio().nr].change(o -> o.addAll(addedObservers));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }, (o, p) -> {
                    if (p instanceof Observed) {
                        conflicts.change(c -> c.add(Pair.of(o, (Observed) p)));
                        return true;
                    } else {
                        return false;
                    }
                }, branches);
                for (int i = 0; i < triggered.length; i++) {
                    state = trigger(state, triggered[i].result(), Priority.values()[i], null, null, null, null);
                }
                for (Pair<Object, Observed> c : conflicts.result()) {
                    for (Observers<Object, ?> obs : c.b().observers()) {
                        state = trigger(state, state.get(c.a(), obs), obs.prio(), c.a(), c.b(), null, null);
                    }
                }
                return state;
            }, branches);
        } finally {
            TraceTimer.traceEnd("merge");
        }
    }

    @SuppressWarnings("rawtypes")
    protected State trigger(State state, Set<Observer> leafs, Priority prio, Object object, Setable setable, Object pre, Object post) {
        Root root = root();
        for (Observer leaf : leafs) {
            state = state.set(commonAncestor(leaf), prio.triggered, Set::add, leaf);
            leaf.checkTooManyChanges(root, this, prio, object, setable, pre, post);
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    protected State scheduleTriggered(State state) {
        Set<AbstractLeaf>[] ls = new Set[1];
        for (Priority prio : Priority.values()) {
            state = state.set(this, prio.triggered, Set.of(), ls);
            state = schedule(state, ls[0], prio);
        }
        return state;
    }

    protected State schedule(State state, Set<AbstractLeaf> ls, Priority prio) {
        for (AbstractLeaf leaf : ls) {
            state = schedule(state, leaf, prio);
        }
        return state;
    }

    protected State schedule(State state, AbstractLeaf leaf, Priority prio) {
        Compound parent = leaf.parent;
        state = state.set(parent, prio.scheduled, Set::add, leaf);
        while (!equals(parent)) {
            state = state.set(parent.parent, SCHEDULED_COMPOUND, Set::add, parent);
            parent = parent.parent;
        }
        return state;
    }

}
