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

import org.modelingvalue.collections.impl.QualifiedDefaultSetImpl;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.QuadFunction;
import org.modelingvalue.collections.util.SerializableFunction;

public interface QualifiedDefaultSet<K, V> extends ContainingCollection<V>, Mergeable<QualifiedDefaultSet<K, V>> {
    @SafeVarargs
    static <K, V> QualifiedDefaultSet<K, V> of(SerializableFunction<V, K> qualifier, SerializableFunction<K, V> defaultFunction, V... e) {
        if (e.length == 0) {
            return new QualifiedDefaultSetImpl<>(qualifier, defaultFunction, (Object) null);
        } else {
            return new QualifiedDefaultSetImpl<>(qualifier, defaultFunction, e);
        }
    }

    static <K, V> QualifiedDefaultSet<K, V> of(SerializableFunction<V, K> qualifier, SerializableFunction<K, V> defaultFunction, java.util.Collection<? extends V> coll) {
        if (coll.isEmpty()) {
            return new QualifiedDefaultSetImpl<>(qualifier, defaultFunction, (Object) null);
        } else {
            return new QualifiedDefaultSetImpl<>(qualifier, defaultFunction, coll);
        }
    }

    V get(K key);

    Collection<V> getAll(Set<K> keys);

    @Override
    QualifiedDefaultSet<K, V> add(V value);

    QualifiedDefaultSet<K, V> put(V value);

    QualifiedDefaultSet<K, V> removeKey(K key);

    QualifiedDefaultSet<K, V> removeAllKey(Collection<K> c);

    <V2> QualifiedDefaultSet<K, V> removeAllKey(QualifiedDefaultSet<K, V2> m);

    QualifiedDefaultSet<K, V> addAll(QualifiedDefaultSet<? extends K, ? extends V> c);

    @Override
    QualifiedDefaultSet<K, V> addAll(Collection<? extends V> e);

    Collection<K> toKeys();

    QualifiedDefaultSet<K, V> merge(QuadFunction<K, V, V[], Integer, V> merger, QualifiedDefaultSet<K, V>[] branches, int length);

    @Override
    QualifiedDefaultSet<K, V> remove(Object e);

    boolean containsAll(Collection<?> c);

    QualifiedDefaultSet<K, V> exclusiveAll(Collection<? extends V> c);

    QualifiedDefaultSet<K, V> retainAll(Collection<?> c);

    void deduplicate(QualifiedDefaultSet<K, V> other);

    SerializableFunction<V, K> qualifier();

    SerializableFunction<K, V> defaultFunction();

    QualifiedDefaultSet<K, V> filter(Predicate<? super K> keyPredicate, Predicate<? super V> valuePredicate);

}
