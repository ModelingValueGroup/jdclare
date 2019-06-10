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
import org.modelingvalue.collections.util.NotMergeableException;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.TraceTimer;
import org.modelingvalue.transactions.Direction.DirectionSetable;
import org.modelingvalue.transactions.Observed.Observers;

public class MutableTransaction extends Transaction {

    private static final int                        MAX_STACK_DEPTH = Integer.getInteger("MAX_STACK_DEPTH", 4);

    private final Concurrent<Set<ActionInstance>>[] triggeredActions;
    private final Concurrent<Set<Mutable>>[]        triggeredMutables;

    @SuppressWarnings("unchecked")
    protected MutableTransaction(UniverseTransaction universeTransaction) {
        super(universeTransaction);
        triggeredActions = new Concurrent[2];
        triggeredMutables = new Concurrent[2];
        for (int ia = 0; ia < 2; ia++) {
            triggeredActions[ia] = Concurrent.of();
            triggeredMutables[ia] = Concurrent.of();
        }
    }

    public Mutable mutable() {
        return (Mutable) cls();
    }

    public boolean ancestorEqualsMutable(Mutable mutable) {
        MutableTransaction mt = this;
        while (mt != null && !mt.mutable().equals(mutable)) {
            mt = mt.parent();
        }
        return mt != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected State run(State state) {
        TraceTimer.traceBegin("compound");
        try {
            State sb = null;
            Set<TransactionClass>[] ts = new Set[1];
            State[] sa = new State[]{state};
            if (this == universeTransaction()) {
                sa[0] = schedule(sa[0], Direction.forward);
            }
            int i = 0;
            boolean sequential = false;
            while (!universeTransaction().isKilled() && i < 3) {
                sa[0] = sa[0].set(mutable(), Direction.scheduled.sequence[i], Set.of(), ts);
                if (ts[0].isEmpty()) {
                    if (++i == 3 && this == universeTransaction()) {
                        sb = schedule(sa[0], Direction.backward);
                        if (sb != sa[0]) {
                            sa[0] = sb;
                            universeTransaction().startOpposite();
                            i = 0;
                        }
                    }
                } else {
                    if (this == universeTransaction()) {
                        universeTransaction().startPriority(Priority.values()[i]);
                    }
                    if (sequential) {
                        for (TransactionClass t : ts[0].random()) {
                            sa[0] = t.run(sa[0], this);
                        }
                    } else {
                        try {
                            sa[0] = sa[0].get(() -> merge(sa[0], ts[0].random().reduce(sa, (s, t) -> {
                                State[] r = s.clone();
                                r[0] = t.run(s[0], this);
                                return r;
                            }, (a, b) -> {
                                State[] r = Arrays.copyOf(a, a.length + b.length);
                                System.arraycopy(b, 0, r, a.length, b.length);
                                return r;
                            })));
                        } catch (NotMergeableException nme) {
                            sequential = true;
                            for (TransactionClass t : ts[0].random()) {
                                sa[0] = t.run(sa[0], this);
                            }
                        }
                    }
                    if (this == universeTransaction()) {
                        universeTransaction().endPriority(Priority.values()[i]);
                    }
                    sa[0] = schedule(sa[0], Direction.forward);
                    i = 0;
                }
            }
            return sa[0];
        } catch (TooManyChangesException | TooManyObservedException | TooManyObserversException tme) {
            throw tme;
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

    @SuppressWarnings("unchecked")
    private State schedule(State state, Direction direction) {
        Set<Mutable>[] cs = new Set[1];
        Set<Action<?>>[] ls = new Set[1];
        return schedule(mutable(), state, direction, cs, ls);
    }

    private static State schedule(Mutable mutable, State state, Direction direction, Set<Mutable>[] cs, Set<Action<?>>[] ls) {
        state = state.set(mutable, direction.preDepth, Set.of(), ls);
        state = state.set(mutable, Direction.scheduled.preDepth, Set::addAll, ls[0]);
        state = state.set(mutable, direction.depth, Set.of(), cs);
        state = state.set(mutable, Direction.scheduled.depth, Set::addAll, cs[0]);
        state = state.set(mutable, direction.postDepth, Set.of(), ls);
        state = state.set(mutable, Direction.scheduled.postDepth, Set::addAll, ls[0]);
        Set<Mutable> csc = cs[0];
        for (Mutable r : csc) {
            state = schedule(r, state, direction, cs, ls);
        }
        return state;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private State merge(State base, State[] branches) {
        if (universeTransaction().isKilled()) {
            return base;
        } else {
            TraceTimer.traceBegin("merge");
            for (int ia = 0; ia < 2; ia++) {
                triggeredActions[ia].init(Set.of());
                triggeredMutables[ia].init(Set.of());
            }
            try {
                State state = base.merge((o, ps, psm, psbs) -> {
                    for (Entry<Setable, Object> p : psm) {
                        if (p.getKey() instanceof Observers) {
                            Observers<?, ?> os = (Observers) p.getKey();
                            Set<ActionInstance> observers = (Set) p.getValue();
                            observers = observers.removeAll(State.get(ps, os));
                            if (!observers.isEmpty()) {
                                Observed<?, ?> observedProp = os.observed();
                                Object baseValue = State.get(ps, observedProp);
                                for (Map<Setable, Object> psb : psbs) {
                                    Object branchValue = State.get(psb, observedProp);
                                    if (!Objects.equals(branchValue, baseValue)) {
                                        Set<ActionInstance> addedObservers = observers.removeAll(State.get(psb, os));
                                        for (ActionInstance ai : addedObservers) {
                                            triggeredActions[os.direction().nr].change(ts -> ts.add(ai.actionInstance((Mutable) o)));
                                        }
                                    }
                                }
                            }
                        } else if (p.getKey() instanceof DirectionSetable) {
                            DirectionSetable<Mutable> ds = (DirectionSetable) p.getKey();
                            if (ds.priority() == Priority.depth && ds.direction() != Direction.scheduled) {
                                Set<Mutable> depth = (Set<Mutable>) p.getValue();
                                depth = depth.removeAll(State.get(ps, ds));
                                if (!depth.isEmpty()) {
                                    Pair<Mutable, Setable<Mutable, ?>> baseParent = State.get(ps, Mutable.D_PARENT_CONTAINING);
                                    for (Map<Setable, Object> psb : psbs) {
                                        Pair<Mutable, Setable<Mutable, ?>> branchParent = State.get(psb, Mutable.D_PARENT_CONTAINING);
                                        if (!Objects.equals(branchParent, baseParent)) {
                                            Set<Mutable> addedDepth = depth.removeAll(State.get(psb, ds));
                                            if (!addedDepth.isEmpty()) {
                                                triggeredMutables[ds.direction().nr].change(ts -> ts.addAll(addedDepth));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }, branches);
                for (int ia = 0; ia < 2; ia++) {
                    state = trigger(state, triggeredActions[ia].result(), Direction.values()[ia]);
                    state = triggerMutables(state, triggeredMutables[ia].result(), Direction.values()[ia]);
                }
                return state;
            } finally {
                for (int ia = 0; ia < 2; ia++) {
                    triggeredActions[ia].clear();
                    triggeredMutables[ia].clear();
                }
                TraceTimer.traceEnd("merge");
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected State trigger(State state, Set<ActionInstance> leafs, Direction direction) {
        for (ActionInstance leaf : leafs) {
            state = trigger(state, leaf.mutable(), leaf.action(), direction);
        }
        return state;
    }

    private State triggerMutables(State state, Set<Mutable> mutables, Direction direction) {
        for (Mutable mutable : mutables) {
            state = trigger(state, mutable, null, direction);
        }
        return state;
    }

    protected <O extends Mutable> State trigger(State state, O mutable, Action<O> leaf, Direction direction) {
        Mutable object = mutable;
        if (leaf != null) {
            state = state.set(object, direction.priorities[leaf.priority().nr], Set::add, leaf);
        }
        Pair<Mutable, ?> parent = state.get(object, Mutable.D_PARENT_CONTAINING);
        while (parent != null && (Direction.backward == direction || !mutable().equals(object))) {
            state = state.set(parent.a(), direction.depth, Set::add, object);
            object = parent.a();
            parent = state.get(object, Mutable.D_PARENT_CONTAINING);
        }
        return state;
    }

}
