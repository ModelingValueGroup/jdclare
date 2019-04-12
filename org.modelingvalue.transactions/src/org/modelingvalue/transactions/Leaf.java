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

import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.function.BiFunction;

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.util.Concurrent;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.TraceTimer;

public class Leaf extends AbstractLeaf {

    public static Leaf of(Object id, Compound parent, Runnable action) {
        return new Leaf(id, parent, action, Phase.triggeredForward, Priority.postDepth);
    }

    public static Leaf of(Object id, Compound parent, Runnable action, Priority priority) {
        return new Leaf(id, parent, action, Phase.triggeredForward, priority);
    }

    public static Leaf of(Object id, Compound parent, Runnable action, Phase initPhase, Priority priority) {
        return new Leaf(id, parent, action, initPhase, priority);
    }

    private final Runnable action;

    protected Leaf(Object id, Compound parent, Runnable action, Phase initPhase, Priority priority) {
        super(id, parent, initPhase, priority);
        this.action = action;
    }

    protected void run(LeafRun<?> run, State pre, Root root) {
        action.run();
    }

    @Override
    protected State run(State state, Root root) {
        TraceTimer.traceBegin(traceId());
        LeafRun<?> run = startRun(root);
        run.init(state);
        try {
            CURRENT.run(run, () -> run(run, state, root));
            return run.result();
        } finally {
            run.clear();
            stopRun(run);
            TraceTimer.traceEnd(traceId());
        }
    }

    protected String traceId() {
        return "leaf";
    }

    @Override
    protected LeafRun<?> startRun(Root root) {
        return root().leafRuns.get().open(this, root);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void stopRun(TransactionRun<?> run) {
        root().leafRuns.get().close((LeafRun<Leaf>) run);
    }

    public static class LeafRun<L extends Leaf> extends AbstractLeafRun<L> {
        @SuppressWarnings("rawtypes")
        private Concurrent<Map<Pair<Object, Setable>, Object>> setted = new Setted();
        private State                                          preState;

        @Override
        public State state() {
            if (preState == null) {
                throw new ConcurrentModificationException();
            }
            return preState;
        }

        protected void init(State state) {
            if (preState != null) {
                throw new ConcurrentModificationException();
            }
            setted.init(Map.of());
            preState = state;
        }

        protected void clear() {
            setted.clear();
            preState = null;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        protected State result() {
            State result = state();
            for (Entry<Pair<Object, Setable>, Object> set : setted.result()) {
                result = result.set(set.getKey().get0(), set.getKey().get1(), set.getValue());
            }
            preState = null;
            return result;
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <O, T, E> T set(O object, Setable<O, T> property, BiFunction<T, E, T> function, E element) {
            Pair<Object, Setable> slot = Pair.of(object, property);
            T prePre = state().get(object, property);
            Map<Pair<Object, Setable>, Object> map = setted.get();
            Entry<Pair<Object, Setable>, Object> bra = map.getEntry(slot);
            T post = function.apply(bra == null ? prePre : (T) bra.getValue(), element);
            return set(object, property, post, slot, prePre, map, bra);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public <O, T> T set(O object, Setable<O, T> property, T post) {
            Pair<Object, Setable> slot = Pair.of(object, property);
            T prePre = state().get(object, property);
            Map<Pair<Object, Setable>, Object> map = setted.get();
            Entry<Pair<Object, Setable>, Object> bra = map.getEntry(slot);
            return set(object, property, post, slot, prePre, map, bra);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private <O, T> T set(O object, Setable<O, T> property, T post, Pair<Object, Setable> slot, T prePre, Map<Pair<Object, Setable>, Object> map, Entry<Pair<Object, Setable>, Object> bra) {
            T pre = bra == null ? prePre : (T) bra.getValue();
            if (!Objects.equals(pre, post)) {
                if (bra != null) {
                    post = (T) merge(slot, prePre, pre, post);
                }
                setted.set(map.put(slot, post));
                changed(object, property, pre, post);
            }
            return prePre;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private final class Setted extends Concurrent<Map<Pair<Object, Setable>, Object>> {
            @Override
            protected Map<Pair<Object, Setable>, Object> merge(Map<Pair<Object, Setable>, Object> base, Map<Pair<Object, Setable>, Object>[] branches) {
                for (Map<Pair<Object, Setable>, Object> b : branches) {
                    for (Entry<Pair<Object, Setable>, Object> e : b) {
                        Pair<Object, Setable> slot = e.getKey();
                        Object post = e.getValue();
                        Entry<Pair<Object, Setable>, Object> bra = base.getEntry(slot);
                        if (bra != null && !Objects.equals(bra.getValue(), post)) {
                            post = LeafRun.this.merge(slot, LeafRun.this.state().get(slot.a(), slot.b()), bra.getValue(), post);
                        }
                        base = base.put(slot, post);
                    }
                }
                return base;
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private Object merge(Pair<Object, Setable> slot, Object prePre, Object pre, Object post) {
            if (pre == null) {
                return post;
            } else if (post == null) {
                return pre;
            } else if (prePre instanceof Mergeable) {
                return ((Mergeable) prePre).merge2(pre, post);
            } else if (slot.b() instanceof Observed && transaction() instanceof Observer) {
                trigger(transaction(), Phase.triggeredBackward);
                return post;
            } else {
                throw new ConcurrentModificationException(slot.a() + "." + slot.b() + "= " + prePre + " -> " + pre + " | " + post);
            }
        }

    }

}
