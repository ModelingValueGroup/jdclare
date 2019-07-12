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

import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.ContainingCollection;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.StreamCollection;
import org.modelingvalue.collections.util.Pair;

public class ListImpl<T> extends TreeCollectionImpl<T> implements List<T> {

    private static final long serialVersionUID     = -8377429516360861865L;
    private static final int  MULTI_MAX_LENGTH     = Integer.getInteger("LIST_MULTI_MAX_LENGTH", 32);
    private static final int  HALF_MAX_LENGTH      = MULTI_MAX_LENGTH / 2;
    private static final int  UNBALANCE_TOLERATION = Integer.getInteger("LIST_UNBALANCE_TOLERATION", 10);

    private static final class OrderedCollectionSpliterator<T> extends CollectionSpliterator<T> {

        private static final int ORDERED_CHARACTERISTICS = Spliterator.ORDERED | CHARACTERISTICS;

        private OrderedCollectionSpliterator(Object value, int min, int max, int size, boolean reverse) {
            super(value, min, max, size, reverse);
        }

        @Override
        protected Spliterator<T> split(Object value, int min, int max, int size, boolean reverse) {
            return new OrderedCollectionSpliterator<>(value, min, max, size, reverse);
        }

        @Override
        public int characteristics() {
            return ORDERED_CHARACTERISTICS;
        }

    }

    private static Object getAllDeep(Object obj, int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException();
        } else if (endIndex < beginIndex) {
            throw new IllegalArgumentException();
        } else if (obj instanceof ListMultivalue) {
            return ((ListMultivalue) obj).getAllDeep(beginIndex, endIndex);
        } else if (beginIndex == 0 && endIndex == 0) {
            return null;
        } else if (obj != null && beginIndex == 0 && endIndex == 1) {
            return obj;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    private static Object getAllDeep(Object[] values, int beginIndex, int endIndex) {
        if (beginIndex == endIndex) {
            return null;
        } else {
            int len = 0;
            Object begin = null;
            int ib = -1;
            for (int i = 0; i < values.length; i++) {
                Object val = values[i];
                int valSize = size(val);
                int total = len + valSize;
                if (total >= endIndex && len <= beginIndex) {
                    return ListImpl.getAllDeep(val, beginIndex - len, endIndex - len);
                } else if (len <= beginIndex && total > beginIndex) {
                    ib = i;
                    begin = ListImpl.getAllDeep(val, beginIndex - len, valSize);
                } else if (total >= endIndex) {
                    Object[] result = new Object[i - ib + 1];
                    result[0] = begin;
                    System.arraycopy(values, ib + 1, result, 1, result.length - 2);
                    result[result.length - 1] = ListImpl.getAllDeep(val, 0, endIndex - len);
                    return ListMultivalue.of(result);
                }
                len = total;
            }
            throw new IndexOutOfBoundsException();
        }
    }

    private static Object removeAllDeep(Object obj, int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException();
        } else if (endIndex < beginIndex) {
            throw new IllegalArgumentException();
        } else if (obj instanceof ListMultivalue) {
            if (beginIndex != 0 && endIndex != size(obj)) {
                Object before = ((ListMultivalue) obj).getAllDeep(0, beginIndex);
                Object after = ((ListMultivalue) obj).getAllDeep(endIndex, size(obj));
                return ListMultivalue.createPair(before, after);
            } else if (beginIndex == 0) {
                return ((ListMultivalue) obj).getAllDeep(endIndex, size(obj));
            } else { // endIndex == size(obj)
                return ((ListMultivalue) obj).getAllDeep(0, beginIndex);
            }
        } else if (beginIndex == 0 && endIndex == 0) {
            return obj;
        } else if (obj != null && beginIndex == 0 && endIndex == 1) {
            return null;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    private static Object insert(Object obj, Object inserted, int index) {
        Objects.requireNonNull(inserted);
        if (obj instanceof ListMultivalue) {
            return ((ListMultivalue) obj).insert(inserted, index);
        } else if (obj != null) {
            if (index == 0) {
                if (inserted instanceof ListMultivalue) {
                    return ((ListMultivalue) inserted).insert(obj, ((ListMultivalue) inserted).size);
                } else {
                    return ListMultivalue.createPair(inserted, obj);
                }
            } else if (index == 1) {
                if (inserted instanceof ListMultivalue) {
                    return ((ListMultivalue) inserted).insert(obj, 0);
                } else {
                    return ListMultivalue.createPair(obj, inserted);
                }
            }
        } else if (index == 0) {
            return inserted;
        }
        throw new IndexOutOfBoundsException();
    }

    private static final class ListMultivalue extends MultiValue {

        private static final long serialVersionUID = -1093279559842921979L;

        private static ListMultivalue of(Object[] values) {
            int hash = 0;
            byte depth = 0;
            int len = 0;
            int size = 0;
            for (Object e : values) {
                int s = size(e);
                for (int i = 0; i < s; i++) {
                    hash *= 31;
                }
                hash += hash(e);
                size += s;
                len += length(e);
                depth = max(depth, depth(e));
            }
            if (len <= MULTI_MAX_LENGTH) {
                Object[] flattened = new Object[len];
                int i = 0;
                depth = 0;
                for (Object e : values) {
                    if (e instanceof ListMultivalue) {
                        ListMultivalue mv = (ListMultivalue) e;
                        System.arraycopy(mv.values, 0, flattened, i, mv.values.length);
                        i += mv.values.length;
                        depth = max(depth, (byte) (mv.depth - 1));
                    } else if (e != null) {
                        flattened[i++] = e;
                        depth = max(depth, (byte) 1);
                    }
                }
                values = flattened;
            } else if (Math.pow(MULTI_MAX_LENGTH, Math.max(depth - UNBALANCE_TOLERATION, 0)) > size) {
                Object[] balanced = new Object[MULTI_MAX_LENGTH];
                int step = size / MULTI_MAX_LENGTH;
                int rest = size % MULTI_MAX_LENGTH;
                int oldidx = 0;
                depth = 0;
                for (int i = 0; i < MULTI_MAX_LENGTH; i++) {
                    int newidx = oldidx + step + (i == HALF_MAX_LENGTH ? rest : 0);
                    balanced[i] = ListImpl.getAllDeep(values, oldidx, newidx);
                    oldidx = newidx;
                    depth = max(depth, depth(balanced[i]));
                }
                values = balanced;
            }
            return new ListMultivalue(values, size, hash, (byte) (depth + 1));
        }

        private ListMultivalue(Object[] values, int size, int hash, byte depth) {
            super(values, size, hash, depth);
        }

        private Object insert(Object inserted, int idx) {
            Object[] result = null;
            int oldLen = 0;
            if (idx == 0) {
                if (values.length < MULTI_MAX_LENGTH) {
                    result = new Object[values.length + 1];
                    System.arraycopy(values, 0, result, 1, values.length);
                    result[0] = inserted;
                } else {
                    result = insert(0, values[0], inserted, 0);
                }
            } else if (idx == size) {
                if (values.length < MULTI_MAX_LENGTH) {
                    result = new Object[values.length + 1];
                    System.arraycopy(values, 0, result, 0, values.length);
                    result[values.length] = inserted;
                } else {
                    int i = values.length - 1;
                    Object val = values[i];
                    result = insert(i, val, inserted, size(val));
                }
            }
            for (int i = 0; result == null && i < values.length; i++) {
                Object val = values[i];
                int valSize = size(val);
                int newLen = oldLen + valSize;
                if (newLen > idx && oldLen < idx) {
                    result = insert(i, val, inserted, idx - oldLen);
                } else if (newLen == idx) {
                    if (values.length < MULTI_MAX_LENGTH) {
                        result = new Object[values.length + 1];
                        System.arraycopy(values, 0, result, 0, ++i);
                        System.arraycopy(values, i, result, i + 1, values.length - i);
                        result[i] = inserted;
                    } else {
                        Object val2 = values[i + 1];
                        if (valSize < size(val2)) {
                            result = insert(i, val, inserted, valSize);
                        } else {
                            result = insert(i + 1, val2, inserted, 0);
                        }
                    }
                }
                oldLen = newLen;
            }
            if (result != null) {
                return ListMultivalue.of(result);
            }
            throw new IndexOutOfBoundsException();
        }

        private Object[] insert(int i, Object val, Object inserted, int idx) {
            Object[] result = Arrays.copyOf(values, values.length);
            result[i] = ListImpl.insert(val, inserted, idx);
            return result;
        }

        public Object getAllDeep(int beginIndex, int endIndex) {
            if (beginIndex == 0 && endIndex == size) {
                return this;
            } else if (beginIndex < size) {
                return ListImpl.getAllDeep(values, beginIndex, endIndex);
            }
            throw new IndexOutOfBoundsException();
        }

        @Override
        protected boolean equalsWithStop(Object obj, boolean[] stop) {
            return size(obj) == size && equalsWithStop(obj, 0, stop);
        }

        private boolean equalsWithStop(Object other, int min, boolean[] stop) {
            int len[] = new int[values.length];
            for (int i = 0; i < values.length; i++) {
                len[i] = size(values[i]) + prev(len, i);
            }
            return getIntStream(0, values.length, stop, size).allMatch(i -> {
                if (stop[0]) {
                    return false;
                }
                Object val = values[i];
                int pos = min + prev(len, i);
                if (val instanceof ListMultivalue ? ((ListMultivalue) val).equalsWithStop(other, pos, stop) : val.equals(TreeCollectionImpl.getDeep(other, pos))) {
                    return true;
                }
                stop[0] = true;
                return false;
            });
        }

        private static Object createPair(Object first, Object last) {
            Object[] values = new Object[2];
            values[0] = first;
            values[1] = last;
            return ListMultivalue.of(values);
        }

        private static int prev(int[] len, int i) {
            return i > 0 ? len[i - 1] : 0;
        }
    }

    @SuppressWarnings("rawtypes")
    public static final List EMPTY = new ListImpl((Object) null);

    public ListImpl(T[] es) {
        if (es.length > MULTI_MAX_LENGTH) {
            for (int i = 0; i < es.length; i++) {
                value = insert(value, es[i], i);
            }
        } else {
            value = es.length == 1 ? es[0] : ListMultivalue.of(Arrays.copyOf(es, es.length, Object[].class));
        }
    }

    public ListImpl(java.util.Collection<? extends T> coll) {
        if (coll.size() > MULTI_MAX_LENGTH) {
            int i = 0;
            for (T e : coll) {
                value = insert(value, e, i++);
            }
        } else {
            Object[] es = coll.toArray();
            value = es.length == 1 ? es[0] : ListMultivalue.of(es);
        }
    }

    private ListImpl(Object value) {
        this.value = value;
    }

    @Override
    public Spliterator<T> spliterator() {
        return new OrderedCollectionSpliterator<T>(value, 0, length(value), size(value), false);
    }

    @Override
    public Spliterator<T> reverseSpliterator() {
        return new OrderedCollectionSpliterator<T>(value, 0, length(value), size(value), true);
    }

    @Override
    public List<T> append(T e) {
        return new ListImpl<>(insert(value, e, size(value)));
    }

    @Override
    public List<T> prepend(T e) {
        return new ListImpl<>(insert(value, e, 0));
    }

    @Override
    public List<T> insert(int position, T inserted) {
        return new ListImpl<>(insert(value, inserted, position));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<T> appendList(List<? extends T> inserted) {
        return inserted.isEmpty() ? this : new ListImpl<>(insert(value, ((ListImpl) inserted).value, size(value)));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<T> prependList(List<? extends T> inserted) {
        return inserted.isEmpty() ? this : new ListImpl<>(insert(value, ((ListImpl) inserted).value, 0));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<T> insertList(int position, List<? extends T> inserted) {
        return inserted.isEmpty() ? this : new ListImpl<>(insert(value, ((ListImpl) inserted).value, position));
    }

    @Override
    public List<T> sublist(int beginIndex, int endIndex) {
        return new ListImpl<>(getAllDeep(value, beginIndex, endIndex));
    }

    @Override
    public List<T> removeFirst() {
        return removeAllFirst(1);
    }

    @Override
    public List<T> removeLast() {
        return removeAllLast(1);
    }

    @Override
    public List<T> removeAllFirst(int length) {
        return removeList(0, length);
    }

    @Override
    public List<T> removeAllLast(int length) {
        return removeList(size() - length, size());
    }

    @Override
    public List<T> remove(int position) {
        return removeList(position, position + 1);
    }

    @Override
    public List<T> removeList(int begin, int end) {
        return end - begin == 0 ? this : new ListImpl<>(removeAllDeep(value, begin, end));
    }

    @Override
    public List<T> replace(int position, T replacement) {
        return new ListImpl<>(insert(removeAllDeep(value, position, position + 1), replacement, position));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<T> replaceList(int begin, int end, List<? extends T> replacement) {
        if (end - begin == 0 && replacement.isEmpty()) {
            return this;
        } else {
            Object removed = end - begin == 0 ? value : removeAllDeep(value, begin, end);
            return new ListImpl<>(replacement.isEmpty() ? removed : insert(removed, ((ListImpl) replacement).value, begin));
        }
    }

    @Override
    public int firstIndexOf(int begin, int end, Object element) {
        for (int i = begin; i < end; i++) {
            if (get(i).equals(element)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int firstIndexOf(Object element) {
        return firstIndexOf(0, size(), element);
    }

    @Override
    public int firstIndexOfList(int begin, int end, List<?> sublist) {
        if (sublist.isEmpty()) {
            return 0;
        } else {
            end = end - sublist.size() + 1;
            for (int i = begin; i < end; i++) {
                if (sublist(i, i + sublist.size()).equals(sublist)) {
                    return i;
                }
            }
            return -1;
        }
    }

    @Override
    public int firstIndexOfList(List<?> sublist) {
        return firstIndexOfList(0, size(), sublist);
    }

    @Override
    public int lastIndexOf(int begin, int end, Object element) {
        for (int i = end - 1; i >= 0; i--) {
            if (get(i).equals(element)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object element) {
        return lastIndexOf(0, size(), element);
    }

    @Override
    public int lastIndexOfList(int begin, int end, List<?> sublist) {
        if (sublist.isEmpty()) {
            return size();
        } else {
            end = end - sublist.size() + 1;
            for (int i = end - 1; i >= 0; i--) {
                if (sublist(i, i + sublist.size()).equals(sublist)) {
                    return i;
                }
            }
            return -1;
        }
    }

    @Override
    public T last() {
        return isEmpty() ? null : get(size() - 1);
    }

    @Override
    public T first() {
        return isEmpty() ? null : get(0);
    }

    @Override
    public int lastIndexOfList(List<?> sublist) {
        return lastIndexOfList(0, size(), sublist);
    }

    @Override
    public Collection<Integer> indexesOf(int begin, int end, Object element) {
        return Collection.of(getIntStream(begin, end, new boolean[1], end - begin).mapToObj(i -> get(i).equals(element) ? i : null).filter(notNullFunction()));
    }

    @Override
    public Collection<Integer> indexesOf(Object element) {
        return indexesOf(0, size(), element);
    }

    @Override
    public Collection<Integer> indexesOfList(int begin, int end, List<?> sublist) {
        if (sublist.isEmpty()) {
            return Collection.of(getIntStream(begin, end + 1, new boolean[1], end - begin).mapToObj(i -> i));
        } else {
            return Collection.of(getIntStream(begin, end - sublist.size() + 1, new boolean[1], end - begin).mapToObj(i -> sublist(i, i + sublist.size()).equals(sublist) ? i : null).filter(notNullFunction()));
        }
    }

    @Override
    public Collection<Integer> indexesOfList(List<?> sublist) {
        return indexesOfList(0, size(), sublist);
    }

    @SuppressWarnings("unchecked")
    protected StreamCollection<Object[]> getCompareStream(List<? extends T>... collections) {
        Object[] values = new Object[collections.length + 1];
        values[0] = value;
        for (int i = 1; i < values.length; i++) {
            ListImpl<T> hashCollectionImpl = (ListImpl<T>) collections[i - 1];
            values[i] = hashCollectionImpl.value;
        }
        return new StreamCollectionImpl<>(new CompareSpliterator(values), isParallel());
    }

    private static final class CompareSpliterator implements Spliterator<Object[]> {
        private static final int COMPARE_CHARACTERISTICS = Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL;

        private final static class ListState {
            private final int[]    is;
            private final Object[] vs;
            private final Object   val;
            private int            depth;

            private ListState(int depth, Object value) {
                is = new int[depth - 1];
                vs = new Object[depth];
                this.val = value;
                vs[0] = value;
            }

            private Object next(int max) {
                Object value = vs[depth];
                if (length(value) > 1 && depth < max) {
                    vs[++depth] = get(vs[depth - 1], 0);
                } else {
                    while (depth > 0 && ++is[depth - 1] >= length(vs[depth - 1])) {
                        vs[depth] = null;
                        is[--depth] = 0;
                    }
                    if (depth > 0) {
                        vs[depth] = get(vs[depth - 1], is[depth - 1]);
                    } else if (vs[0] != null) {
                        vs[0] = null;
                    } else {
                        vs[0] = val;
                    }
                }
                return value;
            }
        }

        private final Object[]    result;
        private final ListState[] states;
        private final int         total;
        private final int         maxDepth;
        private int               coll;
        private int               depth;

        private CompareSpliterator(Object[] values) {
            this.result = new Object[values.length];
            this.states = new ListState[values.length];
            int tot = 0;
            int max = 0;
            for (int c = 0; c < values.length; c++) {
                int d = depth(values[c]);
                max = Math.max(max, d);
                states[c] = new ListState(d, values[c]);
                tot += length(values[c]);
            }
            this.maxDepth = max;
            this.total = tot;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Object[]> action) {
            boolean[] onLevel = new boolean[states.length];
            do {
                do {
                    onLevel[coll] = states[coll].depth == depth;
                    result[coll] = states[coll].next(depth);
                    while (result[coll] != null && coll < states.length - 1) {
                        onLevel[++coll] = states[coll].depth == depth;
                        result[coll] = states[coll].next(depth);
                    }
                    if (result[coll] != null) {
                        for (boolean b : onLevel) {
                            if (b) {
                                action.accept(result);
                                return true;
                            }
                        }
                    } else {
                        coll--;
                    }
                } while (coll >= 0);
                coll = 0;
            } while (++depth < maxDepth);
            return false;
        }

        @Override
        public Spliterator<Object[]> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return total;
        }

        @Override
        public int characteristics() {
            return COMPARE_CHARACTERISTICS;
        }

    }

    @Override
    public List<T> remove(Object e) {
        int pos = firstIndexOf(e);
        return pos >= 0 ? remove(pos) : this;
    }

    @Override
    public List<T> removeAll(Collection<?> e) {
        @SuppressWarnings("resource")
        List<T> result = this;
        for (Object r : e) {
            result = result.remove(r);
        }
        return result;
    }

    @Override
    public List<T> add(T e) {
        return append(e);
    }

    @Override
    public List<T> addAll(Collection<? extends T> e) {
        return appendList(e.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ListImpl<T> create(Object val) {
        return val != value ? (val == null ? (ListImpl<T>) EMPTY : new ListImpl<T>(val)) : this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <B extends List<T>> StreamCollection<Object[]> compareAll(B... branches) {
        return getCompareStream(branches);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected StreamCollection<Object[]> getCompareStream(ContainingCollection<? extends T> toCompare) {
        return (StreamCollection<Object[]>) toSet().compare((ContainingCollection<T>) toCompare.toSet()).map(a -> {
            Object[] b = new Object[a.length];
            for (int i = 0; i < a.length; i++) {
                if (a[i] != null) {
                    b[i] = ((ListImpl<T>) a[i].toList()).value;
                } else {
                    b[i] = null;
                }
            }
            return b;
        });
    }

    @SuppressWarnings("resource")
    @Override
    public List<T> merge(List<T>[] branches, int length) {
        int biggest = -1;
        int size = -1;
        for (int i = 0; i < length; i++) {
            if (branches[i].size() > size) {
                size = branches[i].size();
                biggest = i;
            }
        }
        List<T> result = biggest >= 0 ? branches[biggest] : this;
        for (int i = 0; i < length; i++) {
            if (i != biggest) {
                for (T t : this) {
                    if (!branches[i].contains(t)) {
                        result = result.remove(t);
                    }
                }
                for (int eb = 0; eb < branches[i].size(); eb++) {
                    T t = branches[i].get(eb);
                    if (!contains(t) && !result.contains(t)) {
                        result = result.insert(Math.min(result.size(), eb), t);
                    }
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> getMerger() {
        return EMPTY;
    }

    @Override
    public boolean contains(Object e) {
        return firstIndexOf(e) >= 0;
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        doSerialize(s);
    }

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        doDeserialize(s);
    }

    @Override
    public T next(T e) {
        int i = firstIndexOf(e) + 1;
        return i > 0 && i < size() ? get(i) : null;
    }

    @Override
    public T previous(T e) {
        int i = lastIndexOf(e) - 1;
        return i >= 0 && i < size() - 1 ? get(i) : null;
    }

    @SuppressWarnings("resource")
    @Override
    public <R> List<R> reuse(List<R> reused, BiFunction<R, T, Boolean> matcher, BiConsumer<R, T> changer, Function<R, Long> identity, BiFunction<R, T, Boolean> reusable, BiFunction<Long, T, R> constructor) {
        List<R> result = List.of();
        int begin = 0;
        long id = -1;
        for (T target : this) {
            R source = reused.first();
            if (source != null && matcher.apply(source, target)) {
                id = Math.max(id, identity.apply(source));
                reused = reused.remove(0);
                changer.accept(source, target);
                result = result.append(source);
            } else {
                break;
            }
            begin++;
        }
        int end = size();
        for (; end > begin; end--) {
            T target = get(end - 1);
            R source = reused.last();
            if (source != null && matcher.apply(source, target)) {
                id = Math.max(id, identity.apply(source));
                reused = reused.remove(reused.size() - 1);
                changer.accept(source, target);
                result = result.insert(begin, source);
            } else {
                break;
            }
        }
        List<Pair<Integer, T>> todo = List.of();
        for (int i = begin; i < end; i++) {
            T target = get(i);
            R source = reused.filter(o -> matcher.apply(o, target)).findFirst().orElse(null);
            if (source != null) {
                id = Math.max(id, identity.apply(source));
                reused = reused.remove(source);
                changer.accept(source, target);
                result = result.insert(result.size() - size() + end, source);
            } else {
                todo = todo.append(Pair.of(i, target));
            }
        }
        for (R source : reused) {
            id = Math.max(id, identity.apply(source));
        }
        for (Pair<Integer, T> target : todo) {
            R source = reused.filter(o -> reusable.apply(o, target.b())).findFirst().orElse(null);
            if (source != null) {
                reused = reused.remove(source);
            } else {
                source = constructor.apply(++id, target.b());
            }
            changer.accept(source, target.b());
            result = result.insert(target.a(), source);
        }
        return result;
    }

    @Override
    public List<T> addUnique(T e) {
        return contains(e) ? this : add(e);
    }

    @SuppressWarnings("resource")
    @Override
    public List<T> addAllUnique(Collection<? extends T> es) {
        List<T> result = this;
        for (T e : es) {
            result = result.addUnique(e);
        }
        return result;
    }

}
