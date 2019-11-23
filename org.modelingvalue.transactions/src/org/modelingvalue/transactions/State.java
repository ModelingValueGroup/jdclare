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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.DefaultMap;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.NotMergeableException;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.StringUtil;
import org.modelingvalue.collections.util.TriConsumer;

public class State implements Serializable {
    private static final long                                           serialVersionUID   = -3468784705870374732L;

    @SuppressWarnings("rawtypes")
    public static final DefaultMap<Setable, Object>                     EMPTY_SETABLES_MAP = DefaultMap.of(s -> s.getDefault());
    @SuppressWarnings("rawtypes")
    public static final DefaultMap<Object, DefaultMap<Setable, Object>> EMPTY_OBJECTS_MAP  = DefaultMap.of(o -> EMPTY_SETABLES_MAP);

    @SuppressWarnings("rawtypes")
    private static final Comparator<Entry>                              COMPARATOR         = (a, b) -> StringUtil.toString(a.getKey()).compareTo(StringUtil.toString(b.getKey()));

    @SuppressWarnings("rawtypes")
    private final DefaultMap<Object, DefaultMap<Setable, Object>>       map;
    private final UniverseTransaction                                   universeTransaction;

    @SuppressWarnings("rawtypes")
    State(UniverseTransaction universeTransaction, DefaultMap<Object, DefaultMap<Setable, Object>> map) {
        this.universeTransaction = universeTransaction;
        this.map = map;
    }

    protected State clone(UniverseTransaction universeTransaction) {
        return new State(universeTransaction, map);
    }

    public <O, T> T get(O object, Getable<O, T> property) {
        return get(getProperties(object), (Setable<O, T>) property);
    }

    public <O, A, B> A getA(O object, Getable<O, Pair<A, B>> property) {
        return getA(getProperties(object), (Setable<O, Pair<A, B>>) property);
    }

    public <O, A, B> B getB(O object, Getable<O, Pair<A, B>> property) {
        return getB(getProperties(object), (Setable<O, Pair<A, B>>) property);
    }

    @SuppressWarnings("unchecked")
    public <O, E, T> Collection<E> getCollection(O object, Getable<O, T> property) {
        T v = get(object, property);
        return v instanceof Collection ? (Collection<E>) v : v instanceof Iterable ? Collection.of((Iterable<E>) v) : v == null ? Set.of() : Set.of((E) v);
    }

    @SuppressWarnings("rawtypes")
    public <O, T> State set(O object, Setable<O, T> property, T value) {
        DefaultMap<Setable, Object> props = getProperties(object);
        DefaultMap<Setable, Object> set = setProperties(props, property, value);
        return set != props ? set(object, set) : this;
    }

    public <O, T> State set(O object, Setable<O, T> property, T value, T[] old) {
        return set(object, property, (pre, post) -> {
            old[0] = pre;
            return post;
        }, value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <O> O canonical(O object) {
        Entry<Object, DefaultMap<Setable, Object>> entry = map.getEntry(object);
        return entry != null ? (O) entry.getKey() : object;
    }

    @SuppressWarnings("rawtypes")
    public <O, T, E> State set(O object, Setable<O, T> property, BiFunction<T, E, T> function, E element, T[] oldNew) {
        DefaultMap<Setable, Object> props = getProperties(object);
        oldNew[0] = get(props, property);
        oldNew[1] = function.apply(oldNew[0], element);
        return !Objects.equals(oldNew[0], oldNew[1]) ? set(object, setProperties(props, property, oldNew[1])) : this;
    }

    @SuppressWarnings("rawtypes")
    public <O, T, E> State set(O object, Setable<O, T> property, BiFunction<T, E, T> function, E element) {
        DefaultMap<Setable, Object> props = getProperties(object);
        T preVal = get(props, property);
        T postVal = function.apply(preVal, element);
        return !Objects.equals(preVal, postVal) ? set(object, setProperties(props, property, postVal)) : this;
    }

    @SuppressWarnings("rawtypes")
    <O> DefaultMap<Setable, Object> getProperties(O object) {
        return map.get(object);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <O, T> T get(DefaultMap<Setable, Object> props, Setable<O, T> property) {
        return (T) props.get(property);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <O, A, B> A getA(DefaultMap<Setable, Object> props, Setable<O, Pair<A, B>> property) {
        Pair<A, B> pair = (Pair<A, B>) props.get(property);
        return pair != null ? pair.a() : null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <O, A, B> B getB(DefaultMap<Setable, Object> props, Setable<O, Pair<A, B>> property) {
        Pair<A, B> pair = (Pair<A, B>) props.get(property);
        return pair != null ? pair.b() : null;
    }

    @SuppressWarnings("rawtypes")
    private static <O, T> DefaultMap<Setable, Object> setProperties(DefaultMap<Setable, Object> props, Setable<O, T> property, T newValue) {
        return Objects.equals(property.getDefault(), newValue) ? props.removeKey(property) : props.put(property.entry(newValue, props));
    }

    @SuppressWarnings("rawtypes")
    <O, T> State set(O object, DefaultMap<Setable, Object> post) {
        if (post.isEmpty()) {
            DefaultMap<Object, DefaultMap<Setable, Object>> niw = map.removeKey(object);
            return niw.isEmpty() ? universeTransaction.emptyState() : new State(universeTransaction, niw);
        } else {
            return new State(universeTransaction, map.put(object, post));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public State merge(StateMergeHandler changeHandler, State[] branches, int length) {
        DefaultMap<Object, DefaultMap<Setable, Object>>[] maps = new DefaultMap[length];
        for (int i = 0; i < length; i++) {
            maps[i] = branches[i].map;
        }
        DefaultMap<Object, DefaultMap<Setable, Object>> niw = map.merge((o, ps, pss, pl) -> {
            DefaultMap<Setable, Object> props = ps.merge((p, v, vs, vl) -> {
                if (v instanceof Mergeable) {
                    return ((Mergeable) v).merge(vs, (int) vl);
                } else {
                    Object r = v;
                    for (int i = 0; i < vl; i++) {
                        if (vs[i] != null && !vs[i].equals(v)) {
                            if (!Objects.equals(r, v)) {
                                if (changeHandler != null) {
                                    changeHandler.handleMergeConflict(o, p, v, vs);
                                } else {
                                    throw new NotMergeableException(o + "." + p + "= " + v + " -> " + StringUtil.toString(vs));
                                }
                            } else {
                                r = vs[i];
                            }
                        }
                    }
                    return r;
                }
            }, pss, pl);
            if (changeHandler != null) {
                for (Entry<Setable, Object> p : props) {
                    if (p != ps.getEntry(p.getKey())) {
                        p.getKey().deduplicate(p, props);
                        changeHandler.handleChange(o, ps, p, pss);
                    }
                }
            }
            return props;
        }, maps, maps.length);
        return niw.isEmpty() ? universeTransaction.emptyState() : new State(universeTransaction, niw);

    }

    @Override
    public String toString() {
        return "State" + "[" + universeTransaction.getClass().getSimpleName() + getProperties(universeTransaction.universe()).toString().substring(3) + "]";
    }

    public String asString() {
        return asString(o -> true, s -> true);
    }

    @SuppressWarnings("rawtypes")
    public String asString(Predicate<Object> objectFilter, Predicate<Setable> setableFilter) {
        return get(() -> {
            return "State{" + filter(objectFilter, setableFilter).sorted(COMPARATOR).reduce("", (s1, e1) -> s1 + "\n  " + StringUtil.toString(e1.getKey()) + //
            "{" + e1.getValue().sorted(COMPARATOR).reduce("", (s2, e2) -> s2 + "\n    " + StringUtil.toString(e2.getKey()) + "=" + //
            (e2.getValue() instanceof State ? "State{...}" : StringUtil.toString(e2.getValue())), (a2, b2) -> a2 + b2) + "}", //
                    (a1, b1) -> a1 + b1) + "}";
        });
    }

    @SuppressWarnings("rawtypes")
    public Map<Setable, Integer> count() {
        return get(() -> map.toValues().flatMap(m -> m).reduce(Map.of(), (m, e) -> {
            Integer cnt = m.get(e.getKey());
            return m.put(e.getKey(), cnt == null ? 1 : cnt + 1);
        }, (a, b) -> {
            return a.addAll(b, (x, y) -> x + y);
        }));
    }

    public <R> R get(Supplier<R> supplier) {
        ReadOnlyTransaction tx = universeTransaction.runOnState.openTransaction(universeTransaction);
        try {
            return tx.get(supplier, this);
        } finally {
            universeTransaction.runOnState.closeTransaction(tx);
        }
    }

    public void run(Runnable action) {
        ReadOnlyTransaction tx = universeTransaction.runOnState.openTransaction(universeTransaction);
        try {
            tx.run(action, this);
        } finally {
            universeTransaction.runOnState.closeTransaction(tx);
        }
    }

    public <T> Collection<T> getObjects(Class<T> filter) {
        return map.toKeys().filter(filter);
    }

    public Collection<?> getObjects() {
        return getObjects(Object.class);
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    @SuppressWarnings("rawtypes")
    public Collection<Entry<Object, Collection<Entry<Setable, Object>>>> filter(Predicate<Object> objectFilter, Predicate<Setable> setableFilter) {
        return map.filter(e1 -> objectFilter.test(e1.getKey())).map(e1 -> Entry.of(e1.getKey(), e1.getValue().filter(e2 -> setableFilter.test(e2.getKey()))));
    }

    @SuppressWarnings("rawtypes")
    public Collection<Entry<Object, Map<Setable, Pair<Object, Object>>>> diff(State other) {
        return diff(other, o -> true, s -> true);
    }

    @SuppressWarnings("rawtypes")
    public Collection<Entry<Object, Map<Setable, Pair<Object, Object>>>> diff(State other, Predicate<Object> objectFilter, Predicate<Setable> setableFilter) {
        return map.diff(other.map).filter(d1 -> objectFilter.test(d1.getKey())).map(d2 -> {
            Map<Setable, Pair<Object, Object>> diff = d2.getValue().a().diff(d2.getValue().b()).filter(d3 -> setableFilter.test(d3.getKey())).toMap(e -> e);
            return diff.isEmpty() ? null : Entry.of(d2.getKey(), diff);
        }).notNull();
    }

    @SuppressWarnings("rawtypes")
    public Collection<Entry<Object, Pair<DefaultMap<Setable, Object>, DefaultMap<Setable, Object>>>> diff(State other, Predicate<Object> objectFilter) {
        return map.diff(other.map).filter(d1 -> objectFilter.test(d1.getKey()));
    }

    @SuppressWarnings("rawtypes")
    public String diffString(State other, Predicate<Object> objectFilter, Predicate<Setable> setableFilter) {
        return get(() -> diff(other, objectFilter, setableFilter).reduce("", (s1, e1) -> s1 + "\n  " + StringUtil.toString(e1.getKey()) + //
                " {" + e1.getValue().reduce("", (s2, e2) -> s2 + "\n      " + StringUtil.toString(e2.getKey()) + " =" + //
                        valueDiffString(e2.getValue().a(), e2.getValue().b()), (a2, b2) -> a2 + b2) + "}", (a1, b1) -> a1 + b1));
    }

    public String diffString(State other) {
        return diffString(other, o -> true, s -> true);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String valueDiffString(Object a, Object b) {
        if (a instanceof Set && b instanceof Set) {
            return "\n          <+ " + ((Set) a).removeAll((Set) b) + "\n          +> " + ((Set) b).removeAll((Set) a);
        } else {
            return "\n          <- " + StringUtil.toString(a) + "\n          -> " + StringUtil.toString(b);
        }
    }

    @SuppressWarnings("rawtypes")
    public void forEach(TriConsumer<Object, Setable, Object> consumer) {
        map.forEach(e0 -> e0.getValue().forEach(e1 -> consumer.accept(e0.getKey(), e1.getKey(), e1.getValue())));
    }

    @Override
    public int hashCode() {
        return universeTransaction.hashCode() + map.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof State)) {
            return false;
        } else if (!universeTransaction.equals(((State) obj).universeTransaction)) {
            return false;
        } else {
            return Objects.equals(map, ((State) obj).map);
        }
    }

    public UniverseTransaction universeTransaction() {
        return universeTransaction;
    }

}
