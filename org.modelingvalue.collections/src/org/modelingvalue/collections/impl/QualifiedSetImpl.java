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

import java.util.Spliterator;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.SerializableFunction;

@SuppressWarnings("serial")
public class QualifiedSetImpl<K, V> extends HashCollectionImpl<V> implements QualifiedSet<K, V> {

    private SerializableFunction<V, K> key;

    public QualifiedSetImpl(SerializableFunction<V, K> key, V[] es) {
        this.key = key;
        this.value = es.length == 1 ? es[0] : addAll(null, key(), es);
    }

    public QualifiedSetImpl(SerializableFunction<V, K> key, java.util.Collection<? extends V> coll) {
        this.key = key;
        this.value = coll.size() == 1 ? coll.iterator().next() : addAll(null, key(), coll);
    }

    public QualifiedSetImpl(SerializableFunction<V, K> key, Object value) {
        this.key = key;
        this.value = value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected final SerializableFunction<V, Object> key() {
        return (SerializableFunction) key;
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.writeObject(key);
        doSerialize(s);
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        key = (SerializableFunction<V, K>) s.readObject();
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
    public QualifiedSet<K, V> add(V e) {
        return create(add(value, key(), e, key()));
    }

    @Override
    public QualifiedSet<K, V> put(V e) {
        return create(put(value, key(), e, key()));
    }

    @Override
    public QualifiedSet<K, V> removeKey(K k) {
        return create(remove(value, key(), k, identity()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public QualifiedSet<K, V> removeAllKey(Collection<K> c) {
        if (c instanceof SetImpl) {
            return create(remove(value, key(), ((SetImpl) c).value, identity()));
        } else {
            return removeAllKey(c.toSet());
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <X> QualifiedSet<K, V> removeAllKey(QualifiedSet<K, X> m) {
        return create(remove(value, key(), ((QualifiedSetImpl) m).value, key()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public QualifiedSet<K, V> addAll(QualifiedSet<? extends K, ? extends V> c) {
        return create(add(value, key(), ((QualifiedSetImpl) c).value, key()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void deduplicate(QualifiedSet<K, V> other) {
        deduplicate(value, key(), ((QualifiedSetImpl) other).value, key());
    }

    @SuppressWarnings("unchecked")
    @Override
    public QualifiedSet<K, V> addAll(Collection<? extends V> e) {
        return addAll((QualifiedSet<K, V>) e.toQualifiedSet(key()));
    }

    @Override
    public Collection<K> toKeys() {
        return map(key);
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
    protected QualifiedSetImpl<K, V> create(Object val) {
        return val != value ? new QualifiedSetImpl<>(key, val) : this;
    }

    @Override
    public QualifiedSet<K, V> merge(QualifiedSet<K, V>[] branches) {
        return hashMerge(branches);
    }

    @Override
    public QualifiedSet<K, V> getMerger() {
        return create(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public QualifiedSet<K, V> remove(Object e) {
        return removeKey(key.apply((V) e));
    }

    @Override
    public QualifiedSet<K, V> removeAll(Collection<?> e) {
        @SuppressWarnings("resource")
        QualifiedSet<K, V> result = this;
        for (Object r : e) {
            result = result.remove(r);
        }
        return result;
    }

}
