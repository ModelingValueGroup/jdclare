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

package org.modelingvalue.collections.impl;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.DefaultMap;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Mergeables;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.SerializableFunction;
import org.modelingvalue.collections.util.TriFunction;

public class DefaultMapImpl<K, V> extends HashCollectionImpl<Entry<K, V>> implements DefaultMap<K, V> {

    private static final long                    serialVersionUID = 2424304733060404412L;

    @SuppressWarnings("rawtypes")
    private static final Function<Entry, Object> KEY              = e -> e.getKey();

    private SerializableFunction<K, V>           defaultFunction;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public DefaultMapImpl(Entry[] es, SerializableFunction<K, V> defaultFunction) {
        this.value = es.length == 1 ? es[0] : putAll(null, key(), es);
        this.defaultFunction = defaultFunction;
    }

    protected DefaultMapImpl(Object value, SerializableFunction<K, V> defaultFunction) {
        this.value = value;
        this.defaultFunction = defaultFunction;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected final Function<Entry<K, V>, Object> key() {
        return (Function) KEY;
    }

    @Override
    public Spliterator<Entry<K, V>> spliterator() {
        return new DistinctCollectionSpliterator<Entry<K, V>>(value, 0, length(value), size(value), false);
    }

    @Override
    public Spliterator<Entry<K, V>> reverseSpliterator() {
        return new DistinctCollectionSpliterator<Entry<K, V>>(value, 0, length(value), size(value), true);
    }

    @Override
    public DefaultMap<K, V> put(K key, V val) {
        return Objects.equals(val, defaultFunction.apply(key)) ? removeKey(key) : create(put(value, key(), Entry.of(key, val), key()));
    }

    @Override
    public DefaultMap<K, V> put(Entry<K, V> entry) {
        return Objects.equals(entry.getValue(), defaultFunction.apply(entry.getKey())) ? removeKey(entry.getKey()) : create(put(value, key(), entry, key()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public DefaultMap<K, V> putAll(DefaultMap<? extends K, ? extends V> c) {
        return create(put(value, key(), ((DefaultMapImpl) c).value, key()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void deduplicate(DefaultMap<K, V> other) {
        deduplicate(value, key(), ((DefaultMapImpl) other).value, key());
    }

    @Override
    public DefaultMap<K, V> removeKey(K key) {
        return create(remove(value, key(), key, identity()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public DefaultMap<K, V> removeAllKey(Collection<?> c) {
        if (c instanceof SetImpl) {
            return create(remove(value, key(), ((SetImpl) c).value, identity()));
        } else {
            return removeAllKey(c.toSet());
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <X> DefaultMap<K, V> removeAllKey(DefaultMap<K, X> m) {
        return create(remove(value, key(), ((DefaultMapImpl) m).value, key()));
    }

    @Override
    public DefaultMap<K, V> add(K key, V val, BinaryOperator<V> merger) {
        return Objects.equals(val, defaultFunction.apply(key)) ? removeKey(key) : //
                create(add(value, key(), Entry.of(key, val), key(), (e1, e2) -> mergeEntry(create(e1), create(e2), merger)));
    }

    @Override
    public DefaultMap<K, V> add(Entry<K, V> entry, BinaryOperator<V> merger) {
        return Objects.equals(entry.getValue(), defaultFunction.apply(entry.getKey())) ? removeKey(entry.getKey()) : //
                create(add(value, key(), entry, key(), (e1, e2) -> mergeEntry(create(e1), create(e2), merger)));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public DefaultMap<K, V> addAll(DefaultMap<? extends K, ? extends V> c, BinaryOperator<V> merger) {
        return create(add(value, key(), ((DefaultMapImpl) c).value, key(), (e1, e2) -> mergeEntry(create(e1), create(e2), merger)));
    }

    @Override
    public DefaultMap<K, V> add(Entry<K, V> entry) {
        return Objects.equals(entry.getValue(), defaultFunction.apply(entry.getKey())) ? removeKey(entry.getKey()) : put(entry);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public DefaultMap<K, V> addAll(Collection<? extends Entry<K, V>> es) {
        return putAll(es instanceof DefaultMap ? (DefaultMap) es : es.toDefaultMap(defaultFunction, e -> e));
    }

    @SuppressWarnings("unchecked")
    @Override
    public DefaultMap<K, V> remove(Object e) {
        return e instanceof Entry ? removeKey(((Entry<K, V>) e).getKey()) : this;
    }

    @Override
    public DefaultMap<K, V> removeAll(Collection<?> e) {
        @SuppressWarnings("resource")
        DefaultMap<K, V> result = this;
        for (Object r : e) {
            result = result.remove(r);
        }
        return result;
    }

    @Override
    public DefaultMap<K, V> remove(K key, V val, BinaryOperator<V> merger) {
        return remove(Entry.of(key, val), merger);
    }

    @Override
    public DefaultMap<K, V> remove(Entry<K, V> entry, BinaryOperator<V> merger) {
        return create(remove(value, key(), entry, key(), (e1, e2) -> mergeEntry(create(e1), create(e2), merger)));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public DefaultMap<K, V> removeAll(DefaultMap<? extends K, ? extends V> c, BinaryOperator<V> merger) {
        return create(remove(value, key(), ((DefaultMapImpl) c).value, key(), (e1, e2) -> mergeEntry(create(e1), create(e2), merger)));
    }

    protected Object mergeEntry(DefaultMap<K, V> map1, DefaultMap<K, V> map2, BinaryOperator<V> merger) {
        return ((DefaultMapImpl<K, V>) map1.map(e1 -> {
            Entry<K, V> e2 = map2.getEntry(e1.getKey());
            V val = merger.apply(e1.getValue(), e2.getValue());
            return Objects.equals(val, defaultFunction.apply(e1.getKey())) ? null : //
            Objects.equals(val, e1.getValue()) ? e1 : Objects.equals(val, e2.getValue()) ? e2 : Entry.of(e1.getKey(), val);
        }).notNull().toDefaultMap(defaultFunction, e -> e)).value;
    }

    @Override
    public DefaultMap<K, V> merge(DefaultMap<K, V>[] branches) {
        return merge((k, v, vs) -> Mergeables.merge(v, vs, vs.length), branches);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DefaultMap<K, V> merge(TriFunction<K, Entry<K, V>, Entry<K, V>[], Entry<K, V>> merger, DefaultMap<K, V>... branches) {
        return create(visit(a -> Mergeables.merge(a[0], //
                (e, es) -> mergeEntry((Entry<K, V>) e, merger, es), a, a.length), branches));
    }

    @SuppressWarnings("unchecked")
    protected Entry<K, V> mergeEntry(Entry<K, V> e, TriFunction<K, Entry<K, V>, Entry<K, V>[], Entry<K, V>> merger, Object[] es) {
        Entry<K, V>[] vs = new Entry[es.length];
        System.arraycopy(es, 0, vs, 0, es.length);
        K key = e != null ? e.getKey() : null;
        for (int i = 0; key == null && i < es.length; i++) {
            if (vs[i] != null) {
                key = vs[i].getKey();
            }
        }
        Entry<K, V> result = merger.apply(key, e, vs);
        if (result == null || Objects.equals(result.getValue(), defaultFunction.apply(key))) {
            return null;
        } else if (Objects.equals(result, e)) {
            return e;
        } else {
            for (int i = 0; i < es.length; i++) {
                if (Objects.equals(result, es[i])) {
                    return (Entry<K, V>) es[i];
                }
            }
        }
        return result;
    }

    @Override
    public Collection<Entry<K, Pair<V, V>>> diff(DefaultMap<K, V> toCompare) {
        return compare(toCompare).<Entry<K, Pair<V, V>>> flatMap(a -> {
            if (a[0] == null) {
                return a[1].map(e -> Entry.of(e.getKey(), Pair.of(defaultFunction.apply(e.getKey()), a[1].get(e.getKey()))));
            } else if (a[1] == null) {
                return a[0].map(e -> Entry.of(e.getKey(), Pair.of(a[0].get(e.getKey()), defaultFunction.apply(e.getKey()))));
            } else {
                return a[0].toKeys().toSet().addAll(a[1].toKeys()).map(k -> Entry.of(k, Pair.of(a[0].get(k), a[1].get(k))));
            }
        });
    }

    @Override
    public Collection<K> toKeys() {
        return map(e -> e.getKey());
    }

    @Override
    public Collection<V> toValues() {
        return map(e -> e.getValue());
    }

    @Override
    public V get(K key) {
        Entry<K, V> result = getEntry(key);
        return result != null ? result.getValue() : defaultFunction.apply(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Entry<K, V> getEntry(K key) {
        return (Entry<K, V>) get(value, key(), key);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Collection<V> getAll(Set<K> keys) {
        return create(retain(value, key(), ((SetImpl) keys).value, identity())).map(e -> e.getValue());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected DefaultMapImpl<K, V> create(Object val) {
        return val != value ? new DefaultMapImpl<>(val, defaultFunction) : this;
    }

    @Override
    public DefaultMap<K, V> getMerger() {
        return create(null);
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.writeObject(defaultFunction);
        doSerialize(s);
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        defaultFunction = (SerializableFunction<K, V>) s.readObject();
        doDeserialize(s);
    }

    @Override
    public DefaultMap<K, V> filter(Predicate<? super K> keyPredicate, Predicate<? super V> valuePredicate) {
        return filter(e -> keyPredicate.test(e.getKey()) && valuePredicate.test(e.getValue())).toDefaultMap(defaultFunction, Function.identity());
    }

    @Override
    public SerializableFunction<K, V> defaultFunction() {
        return defaultFunction;
    }

}
