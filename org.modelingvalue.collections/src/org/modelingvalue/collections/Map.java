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

import java.util.function.UnaryOperator;

import org.modelingvalue.collections.impl.MapImpl;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.TriFunction;

public interface Map<K, V> extends ContainingCollection<Entry<K, V>>, Mergeable<Map<K, V>> {

    @SafeVarargs
    @SuppressWarnings("unchecked")
    static <K, V> Map<K, V> of(Entry<K, V>... e) {
        if (e.length == 0) {
            return MapImpl.EMPTY;
        } else {
            return new MapImpl<K, V>(e);
        }
    }

    V get(K key);

    Entry<K, V> getEntry(K key);

    Collection<V> getAll(Set<K> keys);

    Map<K, V> put(Entry<K, V> entry);

    Map<K, V> put(K key, V value);

    Map<K, V> merge(K key, V value, UnaryOperator<V> merger);

    Map<K, V> mergeAll(Map<? extends K, ? extends V> c, TriFunction<K, V, V, V> merger);

    Map<K, V> removeKey(K key);

    Map<K, V> removeAllKey(Collection<?> c);

    <V2> Map<K, V> removeAllKey(Map<K, V2> m);

    Map<K, V> putAll(Map<? extends K, ? extends V> c);

    Collection<K> toKeys();

    Collection<V> toValues();

    @SuppressWarnings("unchecked")
    Map<K, V> merge(TriFunction<K, V, V[], V> merger, Map<K, V>... branches);

    @Override
    Map<K, V> remove(Object e);

    @Override
    Map<K, V> add(Entry<K, V> e);

    Collection<Entry<K, Pair<V, V>>> diff(Map<K, V> other);

}
