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

import java.util.function.BinaryOperator;
import java.util.function.Predicate;

import org.modelingvalue.collections.impl.DefaultMapImpl;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.QuadFunction;
import org.modelingvalue.collections.util.SerializableFunction;

public interface DefaultMap<K, V> extends ContainingCollection<Entry<K, V>>, Mergeable<DefaultMap<K, V>> {

    @SafeVarargs
    static <K, V> DefaultMap<K, V> of(SerializableFunction<K, V> defaultFunction, Entry<K, V>... e) {
        return new DefaultMapImpl<K, V>(e, defaultFunction);
    }

    V get(K key);

    Entry<K, V> getEntry(K key);

    Collection<V> getAll(Set<K> keys);

    DefaultMap<K, V> put(Entry<K, V> entry);

    DefaultMap<K, V> put(K key, V value);

    DefaultMap<K, V> putAll(DefaultMap<? extends K, ? extends V> c);

    DefaultMap<K, V> add(Entry<K, V> entry, BinaryOperator<V> merger);

    DefaultMap<K, V> add(K key, V value, BinaryOperator<V> merger);

    DefaultMap<K, V> addAll(DefaultMap<? extends K, ? extends V> c, BinaryOperator<V> merger);

    DefaultMap<K, V> remove(Entry<K, V> entry, BinaryOperator<V> merger);

    DefaultMap<K, V> remove(K key, V value, BinaryOperator<V> merger);

    DefaultMap<K, V> removeAll(DefaultMap<? extends K, ? extends V> c, BinaryOperator<V> merger);

    DefaultMap<K, V> removeKey(K key);

    DefaultMap<K, V> removeAllKey(Collection<?> c);

    DefaultMap<K, V> filter(Predicate<? super K> keyPredicate, Predicate<? super V> valuePredicate);

    <V2> DefaultMap<K, V> removeAllKey(DefaultMap<K, V2> m);

    void deduplicate(DefaultMap<K, V> other);

    Collection<K> toKeys();

    Collection<V> toValues();

    DefaultMap<K, V> merge(QuadFunction<K, V, V[], Integer, V> merger, DefaultMap<K, V>[] branches, int length);

    @Override
    DefaultMap<K, V> remove(Object e);

    @Override
    DefaultMap<K, V> add(Entry<K, V> e);

    @Override
    DefaultMap<K, V> addAll(Collection<? extends Entry<K, V>> es);

    Collection<Entry<K, Pair<V, V>>> diff(DefaultMap<K, V> other);

    SerializableFunction<K, V> defaultFunction();

}
