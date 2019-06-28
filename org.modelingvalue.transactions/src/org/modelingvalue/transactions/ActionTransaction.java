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
import org.modelingvalue.collections.util.TraceTimer;

public class ActionTransaction extends LeafTransaction {

    @SuppressWarnings("rawtypes")
    private static final Map<Object, Map<Setable, Object>> MAP_DEFAULT = Map.of(o -> Map.of());

    private final Setted                                   setted      = new Setted();
    private State                                          preState;

    protected ActionTransaction(UniverseTransaction universeTransaction) {
        super(universeTransaction);
    }

    public final Action<?> action() {
        return (Action<?>) cls();
    }

    @SuppressWarnings("unchecked")
    protected void run(State pre, UniverseTransaction universeTransaction) {
        ((Action<Mutable>) action()).run(parent().mutable());
    }

    @Override
    protected State run(State state) {
        TraceTimer.traceBegin(traceId());
        init(state);
        try {
            CURRENT.run(this, () -> run(state, universeTransaction()));
            return result();
        } finally {
            clear();
            TraceTimer.traceEnd(traceId());
        }
    }

    protected String traceId() {
        return "leaf";
    }

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

        setted.init(MAP_DEFAULT);
        preState = state;
    }

    protected void clear() {
        setted.clear();
        preState = null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected State result() {
        State result = state();
        for (Entry<Object, Map<Setable, Object>> obj : setted.result()) {
            Map<Setable, Object> props = result.properties(obj.getKey());
            for (Entry<Setable, Object> set : obj.getValue()) {
                props = State.setProperties(props, set.getKey(), set.getValue());
            }
            result = result.set(obj.getKey(), props);
        }
        preState = null;
        return result;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <O, T, E> T set(O object, Setable<O, T> property, BiFunction<T, E, T> function, E element) {
        T prePre = state().get(object, property);
        Map<Object, Map<Setable, Object>> set = setted.get();
        Map<Setable, Object> map = set.get(object);
        Entry<Setable, Object> bra = map.getEntry(property);
        T post = function.apply(bra == null ? prePre : (T) bra.getValue(), element);
        return set(object, property, post, prePre, set, map, bra);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public <O, T> T set(O object, Setable<O, T> property, T post) {
        T prePre = state().get(object, property);
        Map<Object, Map<Setable, Object>> set = setted.get();
        Map<Setable, Object> map = set.get(object);
        Entry<Setable, Object> bra = map.getEntry(property);
        return set(object, property, post, prePre, set, map, bra);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <O, T> T set(O object, Setable<O, T> property, T post, T prePre, Map<Object, Map<Setable, Object>> set, Map<Setable, Object> map, Entry<Setable, Object> bra) {
        T pre = bra == null ? prePre : (T) bra.getValue();
        if (!Objects.equals(pre, post)) {
            if (bra != null) {
                post = (T) merge(object, property, prePre, pre, post);
            }
            setted.set(set.put(object, map.put(property, post)));
            changed(object, property, pre, post);
        }
        return prePre;
    }

    @SuppressWarnings("rawtypes")
    private final class Setted extends Concurrent<Map<Object, Map<Setable, Object>>> {

        @SuppressWarnings({"resource", "unchecked"})
        @Override
        protected Map<Object, Map<Setable, Object>> merge(Map<Object, Map<Setable, Object>> base, Map<Object, Map<Setable, Object>>[] branches) {
            for (Map<Object, Map<Setable, Object>> b : branches) {
                for (Entry<Object, Map<Setable, Object>> obj : b) {
                    Map<Setable, Object> baseObj = base.get(obj.getKey());
                    for (Entry<Setable, Object> set : obj.getValue()) {
                        Object post = set.getValue();
                        Entry<Setable, Object> bra = baseObj.getEntry(set.getKey());
                        if (bra != null && !Objects.equals(bra.getValue(), post)) {
                            post = ActionTransaction.this.merge(obj.getKey(), set.getKey(), state().get(obj.getKey(), set.getKey()), bra.getValue(), post);
                        }
                        baseObj = baseObj.put(set.getKey(), post);
                    }
                    base = base.put(obj.getKey(), baseObj);
                }
            }
            return base;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object merge(Object object, Setable property, Object prePre, Object pre, Object post) {
        if (pre == null) {
            return post;
        } else if (post == null) {
            return pre;
        } else if (prePre instanceof Mergeable) {
            return ((Mergeable) prePre).merge2(pre, post);
        } else if (property instanceof Observed && this instanceof ObserverTransaction) {
            trigger(parent().mutable(), (Action<Mutable>) action(), Direction.backward);
            return post;
        } else {
            throw new ConcurrentModificationException(object + "." + property + "= " + prePre + " -> " + pre + " | " + post);
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected Mutable dParent(Mutable object) {
        Entry<Setable, Object> set = setted.get().get(object).getEntry(Mutable.D_PARENT);
        return set != null ? (Mutable) set.getValue() : super.dParent(object);
    }

    @Override
    public ActionInstance actionInstance() {
        return ActionInstance.of(parent().mutable(), action());
    }

}
