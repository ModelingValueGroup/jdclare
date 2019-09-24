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

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Predicate;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.QualifiedDefaultSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Mergeables;
import org.modelingvalue.collections.util.QuadFunction;
import org.modelingvalue.collections.util.SerializableFunction;

@SuppressWarnings("serial")
public class QualifiedDefaultSetImpl<K, V> extends HashCollectionImpl<V> implements QualifiedDefaultSet<K, V> {

    private SerializableFunction<V, K> qualifier;
    private SerializableFunction<K, V> defaultFunction;

    public QualifiedDefaultSetImpl(SerializableFunction<V, K> qualifier, SerializableFunction<K, V> defaultFunction, V[] es) {
        this.qualifier = qualifier.of();
        this.defaultFunction = defaultFunction.of();
        this.value = es.length == 1 ? es[0] : addAll(null, key(), es);
    }

    public QualifiedDefaultSetImpl(SerializableFunction<V, K> qualifier, SerializableFunction<K, V> defaultFunction, java.util.Collection<? extends V> coll) {
        this.qualifier = qualifier.of();
        this.defaultFunction = defaultFunction.of();
        this.value = coll.size() == 1 ? coll.iterator().next() : addAll(null, key(), coll);
    }

    public QualifiedDefaultSetImpl(SerializableFunction<V, K> qualifier, SerializableFunction<K, V> defaultFunction, Object value) {
        this.qualifier = qualifier.of();
        this.defaultFunction = defaultFunction.of();
        this.value = value;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ qualifier.hashCode() ^ defaultFunction.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && qualifier.equals(((QualifiedDefaultSetImpl<?, ?>) obj).qualifier)//
                && defaultFunction.equals(((QualifiedDefaultSetImpl<?, ?>) obj).defaultFunction);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected final SerializableFunction<V, Object> key() {
        return (SerializableFunction) qualifier;
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.writeObject(qualifier.original());
        s.writeObject(defaultFunction.original());
        doSerialize(s);
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        qualifier = (SerializableFunction<V, K>) s.readObject();
        qualifier = qualifier.of();
        defaultFunction = (SerializableFunction<K, V>) s.readObject();
        defaultFunction = defaultFunction.of();
        doDeserialize(s);
    }

    @Override
    public Spliterator<V> spliterator() {
        return new DistinctCollectionSpliterator<V>(value, 0, length(value), size(value), false);
    }

    @Override
    public Spliterator<V> reverseSpliterator() {
        return new DistinctCollectionSpliterator<V>(value, 0, length(value), size(value), true);
    }

    @Override
    public QualifiedDefaultSet<K, V> add(V e) {
        return create(add(value, key(), e, key()));
    }

    @Override
    public QualifiedDefaultSet<K, V> put(V e) {
        return create(put(value, key(), e, key()));
    }

    @Override
    public QualifiedDefaultSet<K, V> removeKey(K k) {
        return create(remove(value, key(), k, identity()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public QualifiedDefaultSet<K, V> removeAllKey(Collection<K> c) {
        return create(remove(value, key(), ((SetImpl) c.toSet()).value, identity()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <X> QualifiedDefaultSet<K, V> removeAllKey(QualifiedDefaultSet<K, X> m) {
        return create(remove(value, key(), ((QualifiedDefaultSetImpl) m).value, key()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public QualifiedDefaultSet<K, V> addAll(QualifiedDefaultSet<? extends K, ? extends V> c) {
        return create(add(value, key(), ((QualifiedDefaultSetImpl) c).value, key()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void deduplicate(QualifiedDefaultSet<K, V> other) {
        deduplicate(value, key(), ((QualifiedDefaultSetImpl) other).value, key());
    }

    @Override
    public QualifiedDefaultSet<K, V> addAll(Collection<? extends V> e) {
        return addAll(e.toQualifiedDefaultSet(qualifier, defaultFunction));
    }

    @Override
    public Collection<K> toKeys() {
        return map(qualifier);
    }

    @Override
    public V get(K k) {
        return get(value, key(), k);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Collection<V> getAll(Set<K> keys) {
        return create(retain(value, key(), ((SetImpl) keys).value, identity()));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected QualifiedDefaultSetImpl<K, V> create(Object val) {
        return val != value ? new QualifiedDefaultSetImpl<>(qualifier, defaultFunction, val) : this;
    }

    @Override
    public QualifiedDefaultSet<K, V> merge(QualifiedDefaultSet<K, V>[] branches, int length) {
        return merge((k, v, vs, l) -> Mergeables.merge(v, vs, l), branches, length);
    }

    @Override
    public QualifiedDefaultSet<K, V> merge(QuadFunction<K, V, V[], Integer, V> merger, QualifiedDefaultSet<K, V>[] branches, int length) {
        return create(visit((a, l) -> {
            Object r = a[0];
            for (int i = 1; i < l; i++) {
                if (!Objects.equals(a[i], a[0]) && !Objects.equals(a[i], r)) {
                    if (!Objects.equals(a[0], r)) {
                        return merge(merger, a, l);
                    } else {
                        r = a[i];
                    }
                }
            }
            return r;
        }, branches, length));
    }

    @SuppressWarnings("unchecked")
    private Object merge(QuadFunction<K, V, V[], Integer, V> merger, Object[] es, int el) {
        K key = es[0] != null ? qualifier.apply((V) es[0]) : null;
        V v = (V) es[0];
        V[] vs = key != null ? (V[]) Array.newInstance(v.getClass(), el - 1) : null;
        for (int i = 1; i < el; i++) {
            if (es[i] != null) {
                if (key == null) {
                    key = qualifier.apply((V) es[i]);
                    vs = (V[]) Array.newInstance(es[i].getClass(), el - 1);
                }
                vs[i - 1] = (V) es[i];
            }
        }
        V result = merger.apply(key, v, vs, el - 1);
        if (result == null) {
            return null;
        } else {
            for (int i = 0; i < el; i++) {
                if (es[i] != null && Objects.equals(result, es[i])) {
                    return es[i];
                }
            }
            return result;
        }
    }

    @Override
    public QualifiedDefaultSet<K, V> getMerger() {
        return create(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public QualifiedDefaultSet<K, V> remove(Object e) {
        return removeKey(qualifier.apply((V) e));
    }

    @Override
    public QualifiedDefaultSet<K, V> removeAll(Collection<?> e) {
        @SuppressWarnings("resource")
        QualifiedDefaultSet<K, V> result = this;
        for (Object r : e) {
            result = result.remove(r);
        }
        return result;
    }

    @Override
    public SerializableFunction<V, K> qualifier() {
        return qualifier;
    }

    @Override
    public SerializableFunction<K, V> defaultFunction() {
        return defaultFunction;
    }

    @Override
    public QualifiedDefaultSet<K, V> filter(Predicate<? super K> keyPredicate, Predicate<? super V> valuePredicate) {
        return filter(v -> keyPredicate.test(qualifier.apply(v)) && valuePredicate.test(v)).toQualifiedDefaultSet(qualifier, defaultFunction);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean containsAll(Collection<?> c) {
        if (c instanceof QualifiedDefaultSetImpl) {
            return c.size() == size(retain(value, key(), ((QualifiedDefaultSetImpl) c).value, key()));
        } else {
            return containsAll(c.toQualifiedSet(qualifier));
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public QualifiedDefaultSet<K, V> exclusiveAll(Collection<? extends V> c) {
        if (c instanceof QualifiedDefaultSetImpl) {
            return create(exclusive(value, key(), ((QualifiedDefaultSetImpl) c).value, key()));
        } else {
            return exclusiveAll(c.toQualifiedSet(qualifier));
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public QualifiedDefaultSet<K, V> retainAll(Collection<?> c) {
        if (c instanceof QualifiedDefaultSetImpl) {
            return create(retain(value, key(), ((QualifiedDefaultSetImpl) c).value, key()));
        } else {
            return retainAll(c.toQualifiedSet(qualifier));
        }
    }

}
