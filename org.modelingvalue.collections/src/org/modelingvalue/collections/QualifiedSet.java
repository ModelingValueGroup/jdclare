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

package org.modelingvalue.collections;

import java.util.function.Predicate;

import org.modelingvalue.collections.impl.QualifiedSetImpl;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.QuadFunction;
import org.modelingvalue.collections.util.SerializableFunction;

public interface QualifiedSet<K, V> extends ContainingCollection<V>, Mergeable<QualifiedSet<K, V>> {
    @SafeVarargs
    static <K, V> QualifiedSet<K, V> of(SerializableFunction<V, K> qualifier, V... e) {
        if (e.length == 0) {
            return new QualifiedSetImpl<>(qualifier, (Object) null);
        } else {
            return new QualifiedSetImpl<>(qualifier, e);
        }
    }

    static <K, V> QualifiedSet<K, V> of(SerializableFunction<V, K> qualifier, java.util.Collection<? extends V> coll) {
        if (coll.isEmpty()) {
            return new QualifiedSetImpl<>(qualifier, (Object) null);
        } else {
            return new QualifiedSetImpl<>(qualifier, coll);
        }
    }

    V get(K key);

    Collection<V> getAll(Set<K> keys);

    @Override
    QualifiedSet<K, V> add(V value);

    QualifiedSet<K, V> put(V value);

    QualifiedSet<K, V> removeKey(K key);

    QualifiedSet<K, V> removeAllKey(Collection<K> c);

    <V2> QualifiedSet<K, V> removeAllKey(QualifiedSet<K, V2> m);

    QualifiedSet<K, V> addAll(QualifiedSet<? extends K, ? extends V> c);

    @Override
    QualifiedSet<K, V> addAll(Collection<? extends V> e);

    Collection<K> toKeys();

    QualifiedSet<K, V> merge(QuadFunction<K, V, V[], Integer, V> merger, QualifiedSet<K, V>[] branches, int length);

    @Override
    QualifiedSet<K, V> remove(Object e);

    boolean containsAll(Collection<?> c);

    QualifiedSet<K, V> exclusiveAll(Collection<? extends V> c);

    QualifiedSet<K, V> retainAll(Collection<?> c);

    void deduplicate(QualifiedSet<K, V> other);

    SerializableFunction<V, K> qualifier();

    QualifiedSet<K, V> filter(Predicate<? super K> keyPredicate, Predicate<? super V> valuePredicate);

}
