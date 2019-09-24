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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.modelingvalue.collections.impl.StreamCollectionImpl;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.SerializableFunction;
import org.modelingvalue.collections.util.TriConsumer;
import org.modelingvalue.collections.util.TriFunction;

public interface Collection<T> extends Stream<T>, Iterable<T>, Serializable {

    int PARALLELISM = Integer.getInteger("PARALLELISM", ForkJoinPool.getCommonPoolParallelism());

    @Override
    Spliterator<T> spliterator();

    @Override
    Iterator<T> iterator();

    int size();

    boolean isEmpty();

    <F extends T> Collection<F> filter(Class<F> type);

    Collection<T> notNull();

    @Override
    Collection<T> filter(Predicate<? super T> predicate);

    @Override
    <R> Collection<R> map(Function<? super T, ? extends R> mapper);

    @Override
    <R> Collection<R> flatMap(Function<? super T, ? extends java.util.stream.Stream<? extends R>> mapper);

    @Override
    Collection<T> distinct();

    @Override
    Collection<T> sorted();

    Collection<T> random();

    @Override
    Collection<T> sorted(Comparator<? super T> comparator);

    @Override
    Collection<T> peek(Consumer<? super T> action);

    @Override
    Collection<T> limit(long maxSize);

    @Override
    Collection<T> skip(long n);

    @Override
    Collection<T> sequential();

    @Override
    Collection<T> parallel();

    @Override
    Collection<T> unordered();

    @Override
    Collection<T> onClose(Runnable closeHandler);

    @Override
    void forEach(Consumer<? super T> action);

    default Set<T> toSet() {
        return reduce(Set.of(), (s, a) -> s.add(a), (a, b) -> a.addAll(b));
    }

    default List<T> toList() {
        return reduce(List.of(), (s, a) -> s.append(a), (a, b) -> a.appendList(b));
    }

    default <K, V> Map<K, V> toMap(Function<T, Entry<K, V>> entry) {
        return reduce(Map.<K, V> of(), (s, a) -> s.put(entry.apply(a)), (a, b) -> a.putAll(b));
    }

    default <K, V> DefaultMap<K, V> toDefaultMap(SerializableFunction<K, V> defaultFunction, Function<T, Entry<K, V>> entry) {
        return reduce(DefaultMap.<K, V> of(defaultFunction), (s, a) -> s.put(entry.apply(a)), (a, b) -> a.putAll(b));
    }

    @SuppressWarnings("unchecked")
    default <K, V> QualifiedSet<K, V> toQualifiedSet(SerializableFunction<V, K> qualifier) {
        return reduce(QualifiedSet.<K, V> of(qualifier), (s, a) -> s.add((V) a), (a, b) -> a.addAll(b));
    }

    @SuppressWarnings("unchecked")
    default <K, V> QualifiedDefaultSet<K, V> toQualifiedDefaultSet(SerializableFunction<V, K> qualifier, SerializableFunction<K, V> defaultFunction) {
        return reduce(QualifiedDefaultSet.<K, V> of(qualifier, defaultFunction), (s, a) -> s.add((V) a), (a, b) -> a.addAll(b));
    }

    @SuppressWarnings("rawtypes")
    static <T> Collection<T> of(BaseStream<T, ? extends BaseStream> base) {
        return new StreamCollectionImpl<T>(base);
    }

    static <T> Collection<T> of(Stream<T> base) {
        return base instanceof Collection ? (Collection<T>) base : new StreamCollectionImpl<T>(base);
    }

    static <T> Collection<T> of(Spliterator<T> base) {
        return new StreamCollectionImpl<T>(base);
    }

    static <T> Collection<T> of(Iterable<T> base) {
        return base instanceof Collection ? (Collection<T>) base : new StreamCollectionImpl<T>(base);
    }

    static <T> Collection<T> of(Supplier<T> base) {
        return new StreamCollectionImpl<T>(Stream.generate(base));
    }

    @SafeVarargs
    static <T> Collection<T> of(T... elements) {
        return new StreamCollectionImpl<T>(Stream.of(elements));
    }

    static Collection<Integer> range(int from, int to) {
        return of(IntStream.range(from, to));
    }

    static Collection<Integer> range(int size) {
        return range(0, size);
    }

    <U extends Mergeable<U>> U reduce(U identity, BiFunction<U, ? super T, U> accumulator);

    <R> Collection<R> linked(TriFunction<T, T, T, R> function);

    void linked(TriConsumer<T, T, T> consumer);

    <R> Collection<R> indexed(BiFunction<T, Integer, R> function);

    static <E> Collection<E> concat(Collection<? extends E> a, Collection<? extends E> b, Collection<? extends E> c, Collection<? extends E> d) {
        return Collection.of(Stream.concat(Stream.concat(Stream.concat(a, b), c), d));
    }

    static <E> Collection<E> concat(Collection<? extends E> a, Collection<? extends E> b, Collection<? extends E> c) {
        return Collection.of(Stream.concat(Stream.concat(a, b), c));
    }

    static <E> Collection<E> concat(Collection<? extends E> a, Collection<? extends E> b) {
        return Collection.of(Stream.concat(a, b));
    }

    static <E> Collection<E> concat(Collection<? extends E> a, E b) {
        return Collection.of(Stream.concat(a, Collection.of(b)));
    }

    static <E> Collection<E> concat(E a, Collection<? extends E> b) {
        return Collection.of(Stream.concat(Collection.of(a), b));
    }

}
