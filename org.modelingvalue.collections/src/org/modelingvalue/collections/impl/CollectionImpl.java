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

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.util.ContextThread;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.StringUtil;

public abstract class CollectionImpl<T> implements Collection<T> {

    private static final long                       serialVersionUID     = -7907028171250958851L;
    protected static final boolean                  PARALLEL_COLLECTIONS = Boolean.parseBoolean(System.getProperty("PARALLEL_COLLECTIONS", "true"));
    protected static final Function<Object, Object> NULL_FUNCTION        = x -> null;
    protected static final Function<Object, Object> IDENTITY             = Function.identity();

    @SuppressWarnings("rawtypes")
    private static final Predicate                  NOT_NULL             = x -> x != null;

    @Override
    public boolean isParallel() {
        return PARALLEL_COLLECTIONS;
    }

    @SuppressWarnings("unchecked")
    protected static <E> Predicate<E> notNullFunction() {
        return NOT_NULL;
    }

    @Override
    public Collection<T> notNull() {
        return filter(notNullFunction());
    }

    @SuppressWarnings("unchecked")
    protected static <T> Function<Object, T> cast(Class<T> type) {
        return x -> type.isInstance(x) ? (T) x : null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Function<T, Object> identity() {
        return (Function) IDENTITY;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Function<T, Object> nullFunction() {
        return (Function) NULL_FUNCTION;
    }

    protected abstract Stream<T> baseStream();

    @Override
    public <F extends T> Collection<F> filter(Class<F> type) {
        return map(cast(type)).notNull();
    }

    private static <I> Consumer<I> wrap(boolean parallel, Consumer<I> func) {
        if (parallel) {
            Object[] ctx = ContextThread.getContext();
            return i -> {
                Object[] old = ContextThread.setIncrement(ctx);
                try {
                    func.accept(i);
                } finally {
                    ContextThread.setDecrement(old);
                }
            };
        } else {
            return func;
        }
    }

    private static <I, R> Function<I, R> wrap(boolean parallel, Function<I, R> func) {
        if (parallel) {
            Object[] ctx = ContextThread.getContext();
            return i -> {
                Object[] old = ContextThread.setIncrement(ctx);
                try {
                    return func.apply(i);
                } finally {
                    ContextThread.setDecrement(old);
                }
            };
        } else {
            return func;
        }
    }

    private static <I> IntFunction<I> wrap(boolean parallel, IntFunction<I> func) {
        if (parallel) {
            Object[] ctx = ContextThread.getContext();
            return i -> {
                Object[] old = ContextThread.setIncrement(ctx);
                try {
                    return func.apply(i);
                } finally {
                    ContextThread.setDecrement(old);
                }
            };
        } else {
            return func;
        }
    }

    private static <I> ToIntFunction<I> wrap(boolean parallel, ToIntFunction<I> func) {
        if (parallel) {
            Object[] ctx = ContextThread.getContext();
            return i -> {
                Object[] old = ContextThread.setIncrement(ctx);
                try {
                    return func.applyAsInt(i);
                } finally {
                    ContextThread.setDecrement(old);
                }
            };
        } else {
            return func;
        }
    }

    private static <I> ToLongFunction<I> wrap(boolean parallel, ToLongFunction<I> func) {
        if (parallel) {
            Object[] ctx = ContextThread.getContext();
            return i -> {
                Object[] old = ContextThread.setIncrement(ctx);
                try {
                    return func.applyAsLong(i);
                } finally {
                    ContextThread.setDecrement(old);
                }
            };
        } else {
            return func;
        }
    }

    private static <I> ToDoubleFunction<I> wrap(boolean parallel, ToDoubleFunction<I> func) {
        if (parallel) {
            Object[] ctx = ContextThread.getContext();
            return i -> {
                Object[] old = ContextThread.setIncrement(ctx);
                try {
                    return func.applyAsDouble(i);
                } finally {
                    ContextThread.setDecrement(old);
                }
            };
        } else {
            return func;
        }
    }

    private static <A, B> BiConsumer<A, B> wrap(boolean parallel, BiConsumer<A, B> func) {
        if (parallel) {
            Object[] ctx = ContextThread.getContext();
            return (a, b) -> {
                Object[] old = ContextThread.setIncrement(ctx);
                try {
                    func.accept(a, b);
                } finally {
                    ContextThread.setDecrement(old);
                }
            };
        } else {
            return func;
        }
    }

    private static <A, B, R> BiFunction<A, B, R> wrap(boolean parallel, BiFunction<A, B, R> func) {
        if (parallel) {
            Object[] ctx = ContextThread.getContext();
            return (a, b) -> {
                Object[] old = ContextThread.setIncrement(ctx);
                try {
                    return func.apply(a, b);
                } finally {
                    ContextThread.setDecrement(old);
                }
            };
        } else {
            return func;
        }
    }

    private static <A> BinaryOperator<A> wrap(boolean parallel, BinaryOperator<A> func) {
        if (parallel) {
            Object[] ctx = ContextThread.getContext();
            return (a, b) -> {
                Object[] old = ContextThread.setIncrement(ctx);
                try {
                    return func.apply(a, b);
                } finally {
                    ContextThread.setDecrement(old);
                }
            };
        } else {
            return func;
        }
    }

    private static <A> Comparator<A> wrap(boolean parallel, Comparator<A> func) {
        if (parallel) {
            Object[] ctx = ContextThread.getContext();
            return (a, b) -> {
                Object[] old = ContextThread.setIncrement(ctx);
                try {
                    return func.compare(a, b);
                } finally {
                    ContextThread.setDecrement(old);
                }
            };
        } else {
            return func;
        }
    }

    private static <A> Predicate<A> wrap(boolean parallel, Predicate<A> func) {
        if (parallel) {
            Object[] ctx = ContextThread.getContext();
            return a -> {
                Object[] old = ContextThread.setIncrement(ctx);
                try {
                    return func.test(a);
                } finally {
                    ContextThread.setDecrement(old);
                }
            };
        } else {
            return func;
        }
    }

    private static Runnable wrap(boolean parallel, Runnable func) {
        if (parallel) {
            Object[] ctx = ContextThread.getContext();
            return () -> {
                Object[] old = ContextThread.setIncrement(ctx);
                try {
                    func.run();
                } finally {
                    ContextThread.setDecrement(old);
                }
            };
        } else {
            return func;
        }
    }

    private static <R> Supplier<R> wrap(boolean parallel, Supplier<R> func) {
        if (parallel) {
            Object[] ctx = ContextThread.getContext();
            return () -> {
                Object[] old = ContextThread.setIncrement(ctx);
                try {
                    return func.get();
                } finally {
                    ContextThread.setDecrement(old);
                }
            };
        } else {
            return func;
        }
    }

    private static <T, A, R> Collector<T, A, R> wrap(boolean parallel, Collector<T, A, R> func) {
        if (parallel) {
            return new Collector<T, A, R>() {

                private final BiConsumer<A, T>  accumulator = wrap(parallel, func.accumulator());
                private final BinaryOperator<A> combiner    = wrap(parallel, func.combiner());
                private final Function<A, R>    finisher    = wrap(parallel, func.finisher());
                private final Supplier<A>       supplier    = wrap(parallel, func.supplier());

                @Override
                public BiConsumer<A, T> accumulator() {
                    return accumulator;
                }

                @Override
                public Set<Characteristics> characteristics() {
                    return func.characteristics();
                }

                @Override
                public BinaryOperator<A> combiner() {
                    return combiner;
                }

                @Override
                public Function<A, R> finisher() {
                    return finisher;
                }

                @Override
                public Supplier<A> supplier() {
                    return supplier;
                }
            };
        } else {
            return func;
        }
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return baseStream().mapToInt(wrap(isParallel(), mapper));
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return baseStream().mapToLong(wrap(isParallel(), mapper));
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return baseStream().mapToDouble(wrap(isParallel(), mapper));
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return baseStream().flatMapToInt(wrap(isParallel(), mapper));
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return baseStream().flatMapToLong(wrap(isParallel(), mapper));
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return baseStream().flatMapToDouble(wrap(isParallel(), mapper));
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        baseStream().sequential().forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        baseStream().sequential().forEachOrdered(action);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return baseStream().reduce(identity, wrap(isParallel(), accumulator));
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return baseStream().reduce(wrap(isParallel(), accumulator));
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return baseStream().reduce(identity, wrap(isParallel(), accumulator), wrap(isParallel(), combiner));
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return baseStream().collect(wrap(isParallel(), supplier), wrap(isParallel(), accumulator), wrap(isParallel(), combiner));
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return baseStream().collect(wrap(isParallel(), collector));
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return baseStream().min(wrap(isParallel(), comparator));
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return baseStream().max(wrap(isParallel(), comparator));
    }

    @Override
    public long count() {
        return baseStream().count();
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return baseStream().anyMatch(wrap(isParallel(), predicate));
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return baseStream().allMatch(wrap(isParallel(), predicate));
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return baseStream().noneMatch(wrap(isParallel(), predicate));
    }

    @Override
    public Optional<T> findFirst() {
        return baseStream().findFirst();
    }

    @Override
    public Optional<T> findAny() {
        return baseStream().findAny();
    }

    @Override
    public void close() {
        baseStream().close();
    }

    @Override
    public Collection<T> filter(Predicate<? super T> predicate) {
        return new StreamCollectionImpl<T>(baseStream().filter(wrap(isParallel(), predicate)));
    }

    @Override
    public <R> Collection<R> map(Function<? super T, ? extends R> mapper) {
        return new StreamCollectionImpl<R>(baseStream().map(wrap(isParallel(), mapper)));
    }

    @Override
    public <R> Collection<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return new StreamCollectionImpl<R>(baseStream().flatMap(wrap(isParallel(), mapper)));
    }

    @Override
    public Collection<T> distinct() {
        return new StreamCollectionImpl<T>(baseStream().distinct());
    }

    @Override
    public Collection<T> sorted() {
        return new StreamCollectionImpl<T>(baseStream().sorted());
    }

    @Override
    public Collection<T> sorted(Comparator<? super T> comparator) {
        return new StreamCollectionImpl<T>(baseStream().sorted(wrap(isParallel(), comparator)));
    }

    @Override
    public Collection<T> peek(Consumer<? super T> action) {
        return new StreamCollectionImpl<T>(baseStream().peek(wrap(isParallel(), action)));
    }

    @Override
    public Collection<T> limit(long maxSize) {
        return new StreamCollectionImpl<T>(baseStream().limit(maxSize));
    }

    @Override
    public Collection<T> skip(long n) {
        return new StreamCollectionImpl<T>(baseStream().skip(n));
    }

    @Override
    public Collection<T> sequential() {
        return new StreamCollectionImpl<T>(baseStream().sequential());
    }

    @Override
    public Collection<T> parallel() {
        return new StreamCollectionImpl<T>(baseStream().parallel());
    }

    @Override
    public Collection<T> unordered() {
        return new StreamCollectionImpl<T>(baseStream().unordered());
    }

    @Override
    public Collection<T> onClose(Runnable closeHandler) {
        return new StreamCollectionImpl<T>(baseStream().onClose(wrap(isParallel(), closeHandler)));
    }

    @Override
    public Object[] toArray() {
        return baseStream().toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return baseStream().toArray(wrap(isParallel(), generator));
    }

    @Override
    public String toString() {
        String type = getClass().getSimpleName();
        return type.substring(0, type.length() - 4) + "[" + sequential().reduce("", (s, e) -> s.length() > 0 ? (s + "," + StringUtil.toString(e)) : StringUtil.toString(e), (a, b) -> a + "," + b) + "]";
    }

    @Override
    public <U extends Mergeable<U>> U reduce(U identity, BiFunction<U, ? super T, U> accumulator) {
        return reduce(identity, accumulator, (a, b) -> identity.merge2(a, b));
    }

}
