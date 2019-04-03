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
import org.modelingvalue.transactions.Observed.Observers;
import org.modelingvalue.transactions.Priority.PrioritySetable;

public class Compound extends Transaction {

    private static final int               MAX_STACK_DEPTH = Integer.getInteger("MAX_STACK_DEPTH", 4);

    @SuppressWarnings("rawtypes")
    private static final PrioritySetable[] SCHEDULED;
    static {
        Priority[] priorities = Priority.values();
        SCHEDULED = new PrioritySetable[Priority.values().length * 2];
        for (int i = 0; i < priorities.length; i++) {
            SCHEDULED[i * 2] = priorities[i].compScheduled;
            SCHEDULED[i * 2 + 1] = priorities[i].leafScheduled;
        }
    }

    public static Compound of(Object id, Compound parent) {
        return new Compound(id, parent);
    }

    protected Compound(Object id, Compound parent) {
        super(id, parent);
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

    @Override
    @SuppressWarnings("unchecked")
    protected State run(State state, Root root, Priority maxPrio) {
        TraceTimer.traceBegin("compound");
        CompoundRun run = startRun(root);
        try {
            Set<Transaction>[] ts = new Set[1];
            State[] sa = new State[]{state};
            int i = 0;
            boolean sequential = false;
            while (!root.isKilled() && i < SCHEDULED.length && SCHEDULED[i].prio().nr <= maxPrio.nr) {
                sa[0] = sa[0].set(getId(), SCHEDULED[i], Set.of(), ts);
                if (ts[0].isEmpty()) {
                    i++;
                } else {
                    Priority prio = SCHEDULED[i].prio();
                    if (this == root) {
                        root.startPriority(prio);
                    }
                    if (sequential) {
                        for (Transaction t : ts[0].random()) {
                            sa[0] = t.run(sa[0], root, prio);
                        }
                    } else {
                        try {
                            sa[0] = run.merge(sa[0], ts[0].random().reduce(sa, (s, t) -> {
                                State[] r = s.clone();
                                r[0] = t.run(s[0], root, prio);
                                return r;
                            }, (a, b) -> {
                                State[] r = Arrays.copyOf(a, a.length + b.length);
                                System.arraycopy(b, 0, r, a.length, b.length);
                                return r;
                            }));
                        } catch (NotMergeableException nme) {
                            sequential = true;
                            for (Transaction t : ts[0].random()) {
                                sa[0] = t.run(sa[0], root, prio);
                            }
                        }
                    }
                    sa[0] = schedule(sa[0], maxPrio);
                    i = 0;
                    if (this == root) {
                        root.endPriority(prio);
                    }
                }
            }
            return sa[0];
        } catch (TooManyChangesException tmce) {
            throw tmce;
        } catch (TooManySubscriptionsException tmse) {
            throw tmse;
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
            stopRun(run);
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
    private State schedule(State state, Priority maxPrio) {
        Set<AbstractLeaf>[] ls = new Set[1];
        for (Priority prio : Priority.values()) {
            state = state.set(getId(), prio.leafTriggered, Set.of(), ls);
            if (!ls[0].isEmpty()) {
                if (prio.nr <= maxPrio.nr) {
                    state = schedule(state, ls[0], prio);
                } else {
                    state = state.set(parent.getId(), prio.leafTriggered, Set::addAll, ls[0]);
                }
            }
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
        Compound p = leaf.parent;
        state = state.set(p.getId(), prio.leafScheduled, Set::add, leaf);
        while (!getId().equals(p.getId())) {
            state = state.set(p.parent.getId(), prio.compScheduled, Set::add, p);
            p = p.parent;
        }
        return state;
    }

    @Override
    protected CompoundRun startRun(Root root) {
        return root().compoundRuns.get().open(this, root);
    }

    @Override
    protected void stopRun(TransactionRun<?> run) {
        root().compoundRuns.get().close((CompoundRun) run);
    }

    protected static class CompoundRun extends TransactionRun<Compound> {

        private final Concurrent<Set<AbstractLeaf>>[] triggered;
        private ReadOnly                              merger;

        @SuppressWarnings("unchecked")
        protected CompoundRun() {
            super();
            triggered = new Concurrent[Priority.values().length];
            for (int i = 0; i < triggered.length; i++) {
                triggered[i] = Concurrent.of();
            }
        }

        @Override
        protected void start(Compound transaction, Root root) {
            super.start(transaction, root);
            merger = ReadOnly.of(Pair.of(transaction, "merger"), transaction.root());
        }

        @Override
        protected void stop() {
            merger = null;
            super.stop();
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private State merge(State base, State[] branches) {
            TraceTimer.traceBegin("merge");
            try {
                Compound transaction = transaction();
                return transaction.root().isKilled() ? base : merger.get(() -> {
                    for (int i = 0; i < triggered.length; i++) {
                        triggered[i].init(Set.of());
                    }
                    State state = base.merge((o, ps, psm, psbs) -> {
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
                                        Object branchValue = State.get(psb, observedProp);
                                        if (!Objects.equals(branchValue, baseValue)) {
                                            Set<Observer> addedObservers = observers.removeAll(State.get(psb, observersProp));
                                            if (!addedObservers.isEmpty()) {
                                                triggered[observersProp.prio().nr].change(ts -> ts.addAll(addedObservers));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }, branches);
                    for (Priority prio : Priority.values()) {
                        for (AbstractLeaf leaf : triggered[prio.nr].result()) {
                            state = state.set(transaction.commonAncestor(leaf.parent).getId(), prio.leafTriggered, Set::add, leaf);
                        }
                    }
                    return state;
                }, branches);
            } finally {
                for (int i = 0; i < triggered.length; i++) {
                    triggered[i].clear();
                }
                TraceTimer.traceEnd("merge");
            }
        }

    }

}
