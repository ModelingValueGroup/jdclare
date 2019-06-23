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

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.modelingvalue.collections.impl.ListImpl;
import org.modelingvalue.collections.util.Mergeable;

public interface List<T> extends ContainingCollection<T>, Mergeable<List<T>> {

    @SafeVarargs
    @SuppressWarnings("unchecked")
    static <T> List<T> of(T... e) {
        return e.length == 0 ? ListImpl.EMPTY : new ListImpl<>(e);
    }

    @SuppressWarnings("unchecked")
    static <F, T> List<T> of(Function<F, T> function, F[] f) {
        T[] e = (T[]) new Object[f.length];
        for (int i = 0; i < e.length; i++) {
            e[i] = function.apply(f[i]);
        }
        return e.length == 0 ? ListImpl.EMPTY : new ListImpl<>(e);
    }

    @SuppressWarnings("unchecked")
    static <T> List<T> of(java.util.Collection<? extends T> coll) {
        return coll.isEmpty() ? ListImpl.EMPTY : new ListImpl<>(coll);
    }

    @Override
    T get(int index);

    T last();

    T first();

    T next(T e);

    T previous(T e);

    List<T> sublist(int beginIndex, int endIndex);

    List<T> append(T e);

    List<T> prepend(T e);

    List<T> appendList(List<? extends T> inserted);

    List<T> prependList(List<? extends T> inserted);

    List<T> removeFirst();

    List<T> removeLast();

    List<T> removeAllFirst(int length);

    List<T> removeAllLast(int length);

    List<T> insert(int position, T inserted);

    List<T> replace(int position, T inserted);

    List<T> remove(int position);

    List<T> insertList(int position, List<? extends T> inserted);

    List<T> replaceList(int begin, int end, List<? extends T> inserted);

    List<T> removeList(int begin, int end);

    Collection<Integer> indexesOf(int begin, int end, Object element);

    Collection<Integer> indexesOf(Object element);

    Collection<Integer> indexesOfList(int begin, int end, List<?> sublist);

    Collection<Integer> indexesOfList(List<?> sublist);

    int firstIndexOf(int begin, int end, Object element);

    int firstIndexOf(Object element);

    int firstIndexOfList(int begin, int end, List<?> sublist);

    int firstIndexOfList(List<?> sublist);

    int lastIndexOf(int begin, int end, Object element);

    int lastIndexOf(Object element);

    int lastIndexOfList(int begin, int end, List<?> sublist);

    int lastIndexOfList(List<?> sublist);

    @SuppressWarnings("unchecked")
    <B extends List<T>> StreamCollection<Object[]> compareAll(B... branches);

    @Override
    default List<T> toList() {
        return this;
    }

    @Override
    List<T> remove(Object e);

    @Override
    List<T> removeAll(Collection<?> e);

    @Override
    List<T> add(T e);

    @Override
    List<T> addAll(Collection<? extends T> e);

    <R> List<R> reuse(List<R> reused, BiFunction<R, T, Boolean> matcher, BiConsumer<R, T> changer, Function<R, Long> identity, BiFunction<R, T, Boolean> resuable, BiFunction<Long, T, R> constructor);

    @Override
    List<T> addUnique(T e);

    @Override
    List<T> addAllUnique(Collection<? extends T> es);

}
