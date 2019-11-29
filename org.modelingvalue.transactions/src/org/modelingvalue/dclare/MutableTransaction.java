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

package org.modelingvalue.dclare;

import java.util.Arrays;
import java.util.Objects;

import org.modelingvalue.collections.DefaultMap;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Concurrent;
import org.modelingvalue.collections.util.NotMergeableException;
import org.modelingvalue.collections.util.StringUtil;
import org.modelingvalue.collections.util.TraceTimer;
import org.modelingvalue.dclare.Direction.Queued;
import org.modelingvalue.dclare.Observed.Observers;

public class MutableTransaction extends Transaction implements StateMergeHandler {

    @SuppressWarnings("rawtypes")
    private final Concurrent<Map<Observer, Set<Mutable>>>[] triggeredActions;
    private final Concurrent<Set<Mutable>>[]                triggeredMutables;
    @SuppressWarnings("unchecked")
    private final Set<TransactionClass>[]                   ts = new Set[1];
    private final State[]                                   sa = new State[1];
    @SuppressWarnings("unchecked")
    private final Set<Mutable>[]                            cs = new Set[1];
    @SuppressWarnings("unchecked")
    private final Set<Action<?>>[]                          ls = new Set[1];

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

    public final Mutable mutable() {
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
    protected State run(State state) {
        TraceTimer.traceBegin("compound");
        try {
            State sb = null;
            Mutable mutable = mutable();
            sa[0] = state;
            if (this == universeTransaction()) {
                sa[0] = schedule(mutable, sa[0], Direction.forward);
            }
            int i = 0;
            boolean sequential = false;
            while (!universeTransaction().isKilled() && i < 3) {
                sa[0] = sa[0].set(mutable, Direction.scheduled.sequence[i], Set.of(), ts);
                if (ts[0].isEmpty()) {
                    if (++i == 3 && this == universeTransaction()) {
                        sb = schedule(mutable, sa[0], Direction.backward);
                        if (sb != sa[0]) {
                            sa[0] = sb;
                            universeTransaction().startOpposite();
                            i = 0;
                        }
                    }
                } else {
                    if (this == universeTransaction()) {
                        universeTransaction().startPriority(Direction.scheduled.sequence[i].priority());
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
                        universeTransaction().endPriority(Direction.scheduled.sequence[i].priority());
                    }
                    sa[0] = schedule(mutable, sa[0], Direction.forward);
                    i = 0;
                }
            }
            return sa[0];
        } catch (Throwable t) {
            universeTransaction().handleException(new TransactionException(mutable(), t));
            return state;
        } finally {
            sa[0] = null;
            ts[0] = null;
            cs[0] = null;
            ls[0] = null;
            TraceTimer.traceEnd("compound");
        }
    }

    private State schedule(Mutable mutable, State state, Direction direction) {
        state = state.set(mutable, direction.preDepth, Set.of(), ls);
        state = state.set(mutable, Direction.scheduled.preDepth, Set::addAll, ls[0]);
        state = state.set(mutable, direction.depth, Set.of(), cs);
        state = state.set(mutable, Direction.scheduled.depth, Set::addAll, cs[0]);
        state = state.set(mutable, direction.postDepth, Set.of(), ls);
        state = state.set(mutable, Direction.scheduled.postDepth, Set::addAll, ls[0]);
        Set<Mutable> csc = cs[0];
        for (Mutable r : csc) {
            state = schedule(r, state, direction);
        }
        return state;
    }

    private State merge(State base, State[] branches) {
        if (universeTransaction().isKilled()) {
            return base;
        } else {
            TraceTimer.traceBegin("merge");
            for (int ia = 0; ia < 2; ia++) {
                triggeredActions[ia].init(Map.of());
                triggeredMutables[ia].init(Set.of());
            }
            try {
                State state = base.merge(this, branches, branches.length);
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

    @SuppressWarnings("rawtypes")
    @Override
    public void handleMergeConflict(Object object, Setable property, Object pre, Object... branches) {
        throw new NotMergeableException(object + "." + property + "= " + pre + " -> " + StringUtil.toString(branches));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void handleChange(Object o, DefaultMap<Setable, Object> ps, Entry<Setable, Object> p, DefaultMap<Setable, Object>[] psbs) {
        if (p.getKey() instanceof Observers) {
            Observers<?, ?> os = (Observers) p.getKey();
            DefaultMap<Observer, Set<Mutable>> observers = (DefaultMap) p.getValue();
            observers = observers.removeAll(State.get(ps, os), Set::removeAll);
            if (!observers.isEmpty()) {
                Observed<?, ?> observedProp = os.observed();
                Object baseValue = State.get(ps, observedProp);
                for (DefaultMap<Setable, Object> psb : psbs) {
                    Object branchValue = State.get(psb, observedProp);
                    if (!Objects.equals(branchValue, baseValue)) {
                        Map<Observer, Set<Mutable>> addedObservers = observers.removeAll(State.get(psb, os), Set::removeAll).//
                                toMap(e -> Entry.of(e.getKey(), e.getValue().map(m -> m.resolve((Mutable) o)).toSet()));
                        triggeredActions[os.direction().nr].change(ts -> ts.addAll(addedObservers, Set::addAll));
                    }
                }
            }
        } else if (p.getKey() instanceof Queued) {
            Queued<Mutable> ds = (Queued) p.getKey();
            if (ds.priority() == Priority.depth && ds.direction() != Direction.scheduled) {
                Set<Mutable> depth = (Set<Mutable>) p.getValue();
                depth = depth.removeAll(State.get(ps, ds));
                if (!depth.isEmpty()) {
                    Mutable baseParent = State.getA(ps, Mutable.D_PARENT_CONTAINING);
                    for (DefaultMap<Setable, Object> psb : psbs) {
                        Mutable branchParent = State.getA(psb, Mutable.D_PARENT_CONTAINING);
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    private State trigger(State state, Map<Observer, Set<Mutable>> leafs, Direction direction) {
        for (Entry<Observer, Set<Mutable>> e : leafs) {
            for (Mutable m : e.getValue()) {
                state = trigger(state, m, e.getKey(), direction);
            }
        }
        return state;
    }

    private State triggerMutables(State state, Set<Mutable> mutables, Direction direction) {
        for (Mutable mutable : mutables) {
            state = trigger(state, mutable, null, direction);
        }
        return state;
    }

    protected <O extends Mutable> State trigger(State state, O mutable, Action<O> action, Direction direction) {
        Mutable object = mutable;
        if (action != null) {
            state = state.set(object, direction.priorities[action.priority().nr], Set::add, action);
        }
        Mutable parent = state.getA(object, Mutable.D_PARENT_CONTAINING);
        while (parent != null && (Direction.backward == direction || !mutable().equals(object))) {
            state = state.set(parent, direction.depth, Set::add, object);
            object = parent;
            parent = state.getA(object, Mutable.D_PARENT_CONTAINING);
        }
        return state;
    }

}
