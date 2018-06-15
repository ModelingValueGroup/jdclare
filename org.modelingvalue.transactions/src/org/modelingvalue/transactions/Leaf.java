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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Concurrent;
import org.modelingvalue.collections.util.Context;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.TraceTimer;

public class Leaf extends Transaction {

    protected static final Context<Leaf> CURRENT = Context.of();

    public static Leaf of(Object id, Compound parent, Runnable action) {
        return new Leaf(id, parent, action, Priority.high);
    }

    public static Leaf of(Object id, Compound parent, Runnable action, Priority initPrio) {
        return new Leaf(id, parent, action, initPrio);
    }

    private final Runnable                                   action;
    @SuppressWarnings("rawtypes")
    protected Concurrent<Map<Pair<Object, Setable>, Object>> setted = new Setted();
    protected State                                          preState;
    private final Priority                                   initPrio;

    protected Leaf(Object id, Compound parent, Runnable action, Priority initPrio) {
        super(id, parent);
        this.action = action;
        this.initPrio = initPrio;
    }

    protected Priority initPrio() {
        return initPrio;
    }

    public void trigger() {
        getCurrent().trigger(this, initPrio());
    }

    public State pre() {
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
        if (preState == null) {
            throw new ConcurrentModificationException();
        }
        setted.clear();
        preState = null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected State result() {
        State result = pre();
        for (Entry<Pair<Object, Setable>, Object> set : setted.result()) {
            result = result.set(set.getKey().get0(), set.getKey().get1(), set.getValue());
        }
        preState = null;
        return result;
    }

    @Override
    public State apply(State state) {
        TraceTimer.traceBegin(traceId());
        try {
            init(state);
            CURRENT.run(this, action);
            return result();
        } finally {
            TraceTimer.traceEnd(traceId());
        }
    }

    protected String traceId() {
        return "leaf";
    }

    @Override
    public boolean isAncestorOf(Transaction child) {
        return false;
    }

    public <O, T> T get(O object, Getable<O, T> property) {
        if (property instanceof Observed) {
            ConstantSetable<?, ?> lazyConstant = ConstantSetable.CURRENT.get();
            if (lazyConstant != null) {
                throw new NonDeterministicException("Reading changeable '" + property + "' while initializing constant '" + lazyConstant + "'");
            }
        }
        return pre().get(object, property);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <O> void clear(O object) {
        Map<Setable, Object> properties = pre().properties(object);
        if (properties != null) {
            for (Entry<Setable, Object> e : properties) {
                set(object, e.getKey(), e.getKey().getDefault());
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <O, T, E> T set(O object, Setable<O, T> property, BiFunction<T, E, T> function, E element) {
        Pair<Object, Setable> slot = Pair.of(object, property);
        T prePre = pre().get(object, property);
        Entry<Pair<Object, Setable>, Object> e = setted.get().getEntry(slot);
        return set(object, property, function.apply(e == null ? prePre : (T) e.getValue(), element), slot, prePre);
    }

    @SuppressWarnings("rawtypes")
    public <O, T> T set(O object, Setable<O, T> property, T post) {
        Pair<Object, Setable> slot = Pair.of(object, property);
        T prePre = pre().get(object, property);
        return set(object, property, post, slot, prePre);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <O, T> T set(O object, Setable<O, T> property, T post, Pair<Object, Setable> slot, T prePre) {
        Map<Pair<Object, Setable>, Object> map = setted.get();
        Entry<Pair<Object, Setable>, Object> bra = map.getEntry(slot);
        T pre = bra == null ? prePre : (T) bra.getValue();
        if (!Objects.equals(pre, post)) {
            if (merge() && bra != null) {
                post = (T) Leaf.merge(slot, prePre, pre, post);
            }
            setted.set(map.put(slot, post));
            changed(object, property, pre, post);
        }
        return prePre;
    }

    protected boolean merge() {
        return true;
    }

    protected void trigger(Set<Observer> leafs, Priority prio, Object object, Object observed, Object post) {
        for (Observer leaf : leafs) {
            trigger(leaf, prio);
        }
    }

    protected void trigger(Leaf leaf, Priority prio) {
        if (!equals(leaf)) {
            set(commonAncestor(leaf), prio.triggered, Set::add, leaf);
        }
    }

    protected <O, T> void changed(O object, Setable<O, T> property, T preValue, T postValue) {
        property.changed(this, object, preValue, postValue);
    }

    public static Consumer<Leaf> consumer(Runnable action) {
        return $ -> CURRENT.run($, action);
    }

    public static <R> Function<Leaf, R> function(Supplier<R> supplier) {
        return $ -> CURRENT.get($, supplier);
    }

    public static Leaf getCurrent() {
        return CURRENT.get();
    }

    public static void setCurrent(Leaf t) {
        CURRENT.set(t);
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
                        post = Leaf.merge(slot, Leaf.this.pre().get(slot.a(), slot.b()), bra.getValue(), post);
                    }
                    base = base.put(slot, post);
                }
            }
            return base;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object merge(Pair<Object, Setable> slot, Object prePre, Object pre, Object post) {
        if (pre == null) {
            return post;
        } else if (post == null) {
            return pre;
        } else if (prePre instanceof Mergeable) {
            return ((Mergeable) prePre).merge2(pre, post);
        } else {
            throw new ConcurrentModificationException(slot.a() + "." + slot.b() + "= " + prePre + " -> " + pre + " | " + post);
        }
    }

    public boolean mayChange() {
        return true;
    }

}
