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
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.ContainingCollection;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Mergeables;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.TriFunction;

public class MapImpl<K, V> extends HashCollectionImpl<Entry<K, V>> implements Map<K, V> {

    private static final long                    serialVersionUID = 7758359458143777562L;

    @SuppressWarnings("rawtypes")
    private static final Function<Entry, Object> KEY              = e -> e.getKey();

    @SuppressWarnings("rawtypes")
    public static final Map                      EMPTY            = new MapImpl((Object) null);

    public MapImpl(Entry<K, V>[] es) {
        this.value = es.length == 1 ? es[0] : putAll(null, key(), es);
    }

    protected MapImpl(Object value) {
        this.value = value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected final Function<Entry<K, V>, Object> key() {
        return (Function) KEY;
    }

    @Override
    public Spliterator<Entry<K, V>> spliterator() {
        return new DistinctCollectionSpliterator<Entry<K, V>>(value, 0, length(value), size(value));
    }

    @Override
    public Map<K, V> put(Entry<K, V> entry) {
        return create(put(value, key(), entry, key()));
    }

    @Override
    public Map<K, V> put(K key, V val) {
        return put(Entry.of(key, val));
    }

    @Override
    public Map<K, V> removeKey(K key) {
        return create(remove(value, key(), key, identity()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map<K, V> removeAllKey(Collection<?> c) {
        if (c instanceof SetImpl) {
            return create(remove(value, key(), ((SetImpl) c).value, identity()));
        } else {
            return removeAllKey(c.toSet());
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <X> Map<K, V> removeAllKey(Map<K, X> m) {
        return create(remove(value, key(), ((MapImpl) m).value, key()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map<K, V> putAll(Map<? extends K, ? extends V> c) {
        return create(put(value, key(), ((MapImpl) c).value, key()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Map<K, V> merge(K key, V val, UnaryOperator<Entry<K, V>> merger) {
        return create(merge(value, key(), Entry.of(key, val), key(), (e1, e2) -> mergeEntry((Entry) e1, (Entry) e2, key, merger)));
    }

    private static <K, V> Entry<K, V> mergeEntry(Entry<K, V> e1, Entry<K, V> e2, K key, UnaryOperator<Entry<K, V>> merger) {
        Entry<K, V> result = merger.apply(e2);
        return result == null ? null : Objects.equals(result, e1) ? e1 : Objects.equals(result, e2) ? e2 : result;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Map<K, V> mergeAll(Map<? extends K, ? extends V> c, TriFunction<K, Entry<K, V>, Entry<K, V>, Entry<K, V>> merger) {
        return create(merge(value, key(), ((MapImpl) c).value, key(), (e1, e2) -> mergeEntry((Entry) e1, (Entry) e2, merger)));
    }

    private static <K, V> Entry<K, V> mergeEntry(Entry<K, V> e1, Entry<K, V> e2, TriFunction<K, Entry<K, V>, Entry<K, V>, Entry<K, V>> merger) {
        Entry<K, V> result = merger.apply(e1 != null ? e1.getKey() : e2.getKey(), e1, e2);
        return result == null ? null : Objects.equals(result, e1) ? e1 : Objects.equals(result, e2) ? e2 : result;
    }

    @Override
    public Map<K, V> merge(Map<K, V>[] branches) {
        return merge((k, v, vs) -> Mergeables.merge(v, vs, vs.length), branches);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<K, V> merge(TriFunction<K, Entry<K, V>, Entry<K, V>[], Entry<K, V>> merger, Map<K, V>... branches) {
        return create(visit(a -> Mergeables.merge(a[0], //
                (e, es) -> mergeEntry((Entry<K, V>) e, merger, es), a, a.length), branches));
    }

    @Override
    public Collection<Entry<K, Pair<Entry<K, V>, Entry<K, V>>>> diff(Map<K, V> toCompare) {
        return compare(toCompare).<Entry<K, Pair<Entry<K, V>, Entry<K, V>>>> flatMap(a -> {
            if (a[0] == null) {
                return a[1].map(e -> Entry.of(e.getKey(), Pair.of(null, a[1].getEntry(e.getKey()))));
            } else if (a[1] == null) {
                return a[0].map(e -> Entry.of(e.getKey(), Pair.of(a[0].getEntry(e.getKey()), null)));
            } else {
                return a[0].toKeys().toSet().addAll(a[1].toKeys()).map(k -> Entry.of(k, Pair.of(a[0].getEntry(k), a[1].getEntry(k))));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Entry<K, V> mergeEntry(Entry<K, V> e, TriFunction<K, Entry<K, V>, Entry<K, V>[], Entry<K, V>> merger, Object[] es) {
        Entry<K, V>[] vs = new Entry[es.length];
        System.arraycopy(es, 0, vs, 0, es.length);
        K key = e != null ? e.getKey() : null;
        for (int i = 0; key == null && i < es.length; i++) {
            if (vs[i] != null) {
                key = vs[i].getKey();
            }
        }
        Entry<K, V> result = merger.apply(key, e, vs);
        if (result == null) {
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
        return result != null ? result.getValue() : null;
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
    protected MapImpl<K, V> create(Object val) {
        return val != value ? (val == null ? (MapImpl<K, V>) EMPTY : new MapImpl<K, V>(val)) : this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<K, V> getMerger() {
        return EMPTY;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<K, V> remove(Object e) {
        return e instanceof Entry ? removeKey(((Entry<K, V>) e).getKey()) : this;
    }

    @Override
    public Map<K, V> removeAll(Collection<?> e) {
        @SuppressWarnings("resource")
        Map<K, V> result = this;
        for (Object r : e) {
            result = result.remove(r);
        }
        return result;
    }

    @Override
    public Map<K, V> add(Entry<K, V> e) {
        return put(e);
    }

    @Override
    public ContainingCollection<Entry<K, V>> addAll(Collection<? extends Entry<K, V>> es) {
        return putAll(es.toMap(e -> e));
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        doSerialize(s);
    }

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        doDeserialize(s);
    }
}
