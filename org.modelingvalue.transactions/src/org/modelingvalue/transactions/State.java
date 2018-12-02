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
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.StringUtil;
import org.modelingvalue.collections.util.TriConsumer;

public class State implements Serializable {

    private static final long                       serialVersionUID = -3468784705870374732L;

    @SuppressWarnings("rawtypes")
    private static final Comparator<Entry>          COMPARATOR       = (a, b) -> StringUtil.toString(a.getKey()).compareTo(StringUtil.toString(b.getKey()));

    @SuppressWarnings("rawtypes")
    private final Map<Object, Map<Setable, Object>> map;
    private final Root                              root;

    @SuppressWarnings("rawtypes")
    State(Root root, Map<Object, Map<Setable, Object>> map) {
        this.root = root;
        this.map = map;
    }

    @SuppressWarnings("rawtypes")
    public <O, T> T get(O object, Getable<O, T> property) {
        Map<Setable, Object> props = properties(object);
        return get(props, (Setable<O, T>) property);
    }

    @SuppressWarnings("rawtypes")
    public <O, T> State set(O object, Setable<O, T> property, T value) {
        Map<Setable, Object> props = properties(object);
        return set(object, props, property, value);
    }

    public <O, T> State set(O object, Setable<O, T> property, T value, T[] old) {
        return set(object, property, (pre, post) -> {
            old[0] = pre;
            return post;
        }, value);
    }

    @SuppressWarnings("rawtypes")
    public <O, T, E> State set(O object, Setable<O, T> property, BiFunction<T, E, T> function, E element, T[] oldNew) {
        Map<Setable, Object> props = properties(object);
        oldNew[0] = get(props, property);
        oldNew[1] = function.apply(oldNew[0], element);
        return !Objects.equals(oldNew[0], oldNew[1]) ? set(object, props, property, oldNew[1]) : this;
    }

    @SuppressWarnings("rawtypes")
    public <O, T, E> State set(O object, Setable<O, T> property, BiFunction<T, E, T> function, E element) {
        Map<Setable, Object> props = properties(object);
        T preVal = get(props, property);
        T postVal = function.apply(preVal, element);
        return !Objects.equals(preVal, postVal) ? set(object, props, property, postVal) : this;
    }

    @SuppressWarnings("rawtypes")
    public <O> Map<Setable, Object> properties(O object) {
        return map == null ? null : map.get(object);
    }

    @SuppressWarnings("rawtypes")
    public <O> State clear(O object) {
        Map<Object, Map<Setable, Object>> niw = map == null ? null : map.removeKey(object);
        return map == niw ? this : niw == null || niw.isEmpty() ? root.emptyState() : new State(root, niw);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <O, T> T get(Map<Setable, Object> props, Setable<O, T> property) {
        Entry entry = props == null ? null : props.getEntry(property);
        return entry == null ? property.getDefault() : (T) entry.getValue();
    }

    @SuppressWarnings("rawtypes")
    <O, T> State set(O object, Map<Setable, Object> pre, Setable<O, T> property, T newValue) {
        Map<Setable, Object> post;
        if (Objects.equals(property.getDefault(), newValue)) {
            post = pre == null ? null : pre.removeKey(property);
            post = post == null || post.isEmpty() ? null : post;
        } else {
            post = pre == null ? Map.of(Entry.of(property, newValue)) : pre.put(property, newValue);
        }
        if (pre == post) {
            return this;
        } else if (post == null) {
            Map<Object, Map<Setable, Object>> niw = map == null ? null : map.removeKey(object);
            return niw == null || niw.isEmpty() ? root.emptyState() : new State(root, niw);
        } else {
            return new State(root, map == null ? Map.of(Entry.of(object, post)) : map.put(object, post));
        }
    }

    private static <X, Y> Map<X, Y> map(Map<X, Y> in) {
        return in == null ? Map.<X, Y> of() : in;
    }

    @SuppressWarnings("unchecked")
    private static <X, Y> Map<X, Y>[] map(Entry<?, Map<X, Y>>[] in) {
        Map<X, Y>[] r = new Map[in.length];
        for (int i = 0; i < in.length; i++) {
            r[i] = in[i] == null ? Map.<X, Y> of() : in[i].getValue();
        }
        return r;
    }

    private static <X, Y> Y val(Entry<X, Y> e) {
        return e == null ? null : e.getValue();
    }

    private static <X, Y> Y val(Setable<X, Y> p, Entry<?, Y> e) {
        return e == null ? p.getDefault() : e.getValue();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <X, Y> Y[] val(Y b, Setable<X, Y> p, Entry<Setable, Y>[] in) {
        Y[] r = b != null ? (Y[]) Array.newInstance(b.getClass(), in.length) : (Y[]) new Object[in.length];
        for (int i = 0; i < in.length; i++) {
            r[i] = in[i] == null ? p.getDefault() : in[i].getValue();
        }
        return r;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public State merge(TriConsumer<Map<Setable, Object>, Map<Setable, Object>, Map<Setable, Object>[]> change, State... branches) {
        Map<Object, Map<Setable, Object>>[] maps = new Map[branches.length];
        for (int i = 0; i < maps.length; i++) {
            maps[i] = map(branches[i].map);
        }
        Map<Object, Map<Setable, Object>> niw = map(map).merge((o, eps, epss) -> {
            Map<Setable, Object> ps = map(val(eps));
            Map<Setable, Object>[] pss = map(epss);
            Map<Setable, Object> props = ps.merge((p, ev, evs) -> {
                Object v = val(p, ev);
                Object[] vs = val(v, p, evs);
                if (v instanceof Mergeable) {
                    Object result = ((Mergeable<Object>) v).merge(vs);
                    return Objects.equals(result, p.getDefault()) ? null : Entry.of(p, result);
                } else {
                    Object result = null;
                    for (int i = 0; i < vs.length; i++) {
                        if (vs[i] != null) {
                            if (result != null) {
                                result = null;
                                break;
                            } else {
                                result = vs[i];
                            }
                        }
                    }
                    if (result == null) {
                        Object stv = v;
                        throw new ConcurrentModificationException(get(() -> o + "." + p + " = " + stv + " -> " + Arrays.toString(vs)));
                    } else {
                        return Entry.of(p, result);
                    }
                }
            }, pss);
            change.accept(ps, map(props), pss);
            return props == null || props.isEmpty() ? null : Entry.of(o, props);
        }, maps);
        return niw == null || niw.isEmpty() ? root.emptyState() : new State(root, niw);

    }

    @Override
    public String toString() {
        Optional<Root> findAny = getObjects(Root.class).findAny();
        return "State" + findAny.map(r -> "[" + r.getClass().getSimpleName() + properties(r).toString().substring(3) + "]").orElse("[]");
    }

    public String asString() {
        return get(() -> {
            return "State{" + map(map).sorted(COMPARATOR).reduce("", (s1, e1) -> s1 + "\n  " + StringUtil.toString(e1.getKey()) + //
            "{" + e1.getValue().sorted(COMPARATOR).reduce("", (s2, e2) -> s2 + "\n    " + StringUtil.toString(e2.getKey()) + "=" + //
            (e2.getValue() instanceof State ? "State{...}" : StringUtil.toString(e2.getValue())), (a2, b2) -> a2 + b2) + "}", //
                    (a1, b1) -> a1 + b1) + "}";
        });
    }

    public <R> R get(Supplier<R> supplier) {
        return ReadOnly.of(Pair.of(this, "getOnState"), root).get(supplier, this);
    }

    public void run(Runnable action) {
        ReadOnly.of(Pair.of(this, "runOnState"), root).run(action, this);
    }

    public <T> Collection<T> getObjects(Class<T> filter) {
        return map(map).toKeys().filter(filter);
    }

    public Collection<?> getObjects() {
        return getObjects(Object.class);
    }

    public int size() {
        return map == null ? 0 : map.size();
    }

    public boolean isEmpty() {
        return map == null;
    }

    @SuppressWarnings("rawtypes")
    public Collection<Entry<Object, Map<Setable, Pair<Object, Object>>>> diff(State other) {
        return diff(other, o -> true, s -> true);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Collection<Entry<Object, Map<Setable, Pair<Object, Object>>>> diff(State other, Predicate<Object> objectFilter, Predicate<Setable> setableFilter) {
        return map(map).diff(other.map).filter(d1 -> objectFilter.test(d1.getKey())).map(d2 -> {
            Map<Setable, Object> a = map(val(d2.getValue().a()));
            Map<Setable, Object> b = map(val(d2.getValue().b()));
            Map<Setable, Pair<Object, Object>> diff = a.diff(b).filter(d3 -> setableFilter.test(d3.getKey())).toMap(e -> {
                Object va = val(e.getKey(), e.getValue().a());
                Object vb = val(e.getKey(), e.getValue().b());
                return Entry.of(e.getKey(), Pair.of(va, vb));
            });
            return diff.isEmpty() ? null : Entry.of(d2.getKey(), diff);
        }).notNull();
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
        if (map != null) {
            map.forEach(e0 -> e0.getValue().forEach(e1 -> consumer.accept(e0.getKey(), e1.getKey(), e1.getValue())));
        }
    }

    @Override
    public int hashCode() {
        return map == null ? root.hashCode() : root.hashCode() + map.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof State)) {
            return false;
        } else if (!root.equals(((State) obj).root)) {
            return false;
        } else {
            return Objects.equals(map, ((State) obj).map);
        }
    }

    public Root root() {
        return root;
    }

}
