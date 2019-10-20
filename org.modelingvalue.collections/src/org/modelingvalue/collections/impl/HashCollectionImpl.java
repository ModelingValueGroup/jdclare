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
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.ContainingCollection;
import org.modelingvalue.collections.StreamCollection;
import org.modelingvalue.collections.util.Age;
import org.modelingvalue.collections.util.Concurrent;
import org.modelingvalue.collections.util.ContextThread;
import org.modelingvalue.collections.util.Reusable;
import org.modelingvalue.collections.util.StringUtil;

public abstract class HashCollectionImpl<T> extends TreeCollectionImpl<T> {

    private static final long                     serialVersionUID             = 3453919290764033219L;

    private static final int                      EQUAL_HASHCODE_WARNING_LEVEL = Integer.getInteger("EQUAL_HASHCODE_WARNING_LEVEL", 16);

    @SuppressWarnings("rawtypes")
    private static final BiFunction               RETURN_2                     = (v1, v2) -> v1.equals(v2) ? v1 : v2;
    @SuppressWarnings("rawtypes")
    private static final BiFunction               RETURN_1                     = (v1, v2) -> v1;
    @SuppressWarnings("rawtypes")
    private static final BiFunction               RETURN_NULL                  = (v1, v2) -> null;

    @SuppressWarnings("rawtypes")
    private static final BiFunction               PRUNE                        = (v1, v2) -> {
                                                                                   v1.equals(v2);
                                                                                   return null;
                                                                               };

    private static final int                      PART_SIZE                    = Integer.getInteger("HASH_PARTITION_SIZE", 6);
    private static final int                      PART_REST                    = Integer.SIZE % PART_SIZE == 0 ? 0 : PART_SIZE - Integer.SIZE % PART_SIZE;
    private static final byte                     NR_OF_PARTS                  = (byte) (Integer.SIZE / PART_SIZE + (PART_REST == 0 ? 0 : 1));
    private static final int[]                    PART_MASKS                   = new int[NR_OF_PARTS];
    private static final int[]                    INDEX_MASKS                  = new int[NR_OF_PARTS];
    private static final int[]                    PART_SHIFTS                  = new int[NR_OF_PARTS];

    private static final int                      COMPARE_MAX                  = Integer.getInteger("COMPARE_MAX", ContextThread.POOL_SIZE * 2);
    private static final HashMultiValue           DUMMY                        = new HashMultiValue(new Object[0], 0, 0, (byte) 1, 0, (byte) 0, 0);
    private static Object[][]                     SINGLES                      = new Object[COMPARE_MAX][COMPARE_MAX];

    private static final Concurrent<CompareSates> COMPARE_STATES               = Concurrent.of(() -> new CompareSates());

    static {
        int normal = Integer.MAX_VALUE << (Integer.SIZE - PART_SIZE);
        int small = normal << 1;
        for (int i = 0; i < NR_OF_PARTS; i++) {
            if (i < PART_REST) {
                PART_MASKS[i] = small >>> (i * (PART_SIZE - 1));
            } else {
                PART_MASKS[i] = normal >>> (i * PART_SIZE - PART_REST);
            }
            INDEX_MASKS[i] = i > 0 ? (INDEX_MASKS[i - 1] | PART_MASKS[i]) : PART_MASKS[i];
            PART_SHIFTS[i] = Integer.numberOfTrailingZeros(PART_MASKS[i]);
        }
        if (PART_SIZE < 2 || PART_SIZE > 6) {
            throw new Error("HASH_PARTITION_SIZE must be 2, 3, 4, 5 or 6");
        }
        for (int i = 0; i < COMPARE_MAX; i++) {
            SINGLES[i][i] = DUMMY;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected static int index(Object v, Function key) {
        return v == null ? 0 : v instanceof HashMultiValue ? ((HashMultiValue) v).index : key.apply(v).hashCode();
    }

    protected static byte level(Object v) {
        return v instanceof HashMultiValue ? ((HashMultiValue) v).level : NR_OF_PARTS;
    }

    protected static long mask(Object v, int id, int level) {
        if (v instanceof HashMultiValue && ((HashMultiValue) v).level == level) {
            return ((HashMultiValue) v).mask;
        } else {
            return 1L << ((id & PART_MASKS[level]) >>> PART_SHIFTS[level]);
        }
    }

    protected static Object get(Object v, int level, int i) {
        if (v instanceof HashMultiValue && ((HashMultiValue) v).level == level) {
            return ((HashMultiValue) v).values[i];
        } else {
            return v;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected static Object key(Object value, Function key) {
        return value instanceof HashMultiValue ? value : key.apply(value);
    }

    protected abstract Function<T, Object> key();

    protected static final class DistinctCollectionSpliterator<T> extends CollectionSpliterator<T> {

        private static final int DISTINCT_CHARACTERISTICS = Spliterator.DISTINCT | CHARACTERISTICS;

        public DistinctCollectionSpliterator(Object value, int min, int max, int size, boolean reverse) {
            super(value, min, max, size, reverse);
        }

        @Override
        protected Spliterator<T> split(Object value, int min, int max, int size, boolean reverse) {
            return new DistinctCollectionSpliterator<>(value, min, max, size, reverse);
        }

        @Override
        public int characteristics() {
            return DISTINCT_CHARACTERISTICS;
        }

    }

    private static final class HashMultiValue extends MultiValue {
        private static final long serialVersionUID = 3238646981697101095L;
        private final int         index;
        private final byte        level;
        private final long        mask;

        private HashMultiValue(Object[] values, int size, int hash, byte depth, int index, byte level, long mask) {
            super(values, size, hash, depth);
            this.index = index;
            this.level = level;
            this.mask = level < NR_OF_PARTS ? mask : 0;
        }

        @Override
        public int hashCode() {
            return super.hashCode() + size + index + level + depth;
        }

        @Override
        protected boolean equalsWithStop(Object obj, boolean[] stop) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (!(obj instanceof HashMultiValue)) {
                return false;
            }
            HashMultiValue other = (HashMultiValue) obj;
            if (hash != other.hash || index != other.index || level != other.level || size != other.size || depth != other.depth || mask != other.mask) {
                return false;
            } else if (level == NR_OF_PARTS) {
                outer:
                for (int ia = 0; ia < values.length; ia++) {
                    for (int ib = 0; ib < values.length; ib++) {
                        if (stop[0]) {
                            return false;
                        } else if (values[ia] == other.values[ib]) {
                            continue outer;
                        } else if (Objects.equals(values[ia], other.values[ib])) {
                            if (Age.age(values[ia]) > Age.age(other.values[ib])) {
                                other.values[ib] = values[ia];
                            } else {
                                values[ia] = other.values[ib];
                            }
                            continue outer;
                        }
                    }
                    stop[0] = true;
                    return false;
                }
                return true;
            } else {
                return getIntStream(0, values.length, stop, size).allMatch(i -> {
                    if (stop[0]) {
                        return false;
                    } else if (values[i] == other.values[i]) {
                        return true;
                    } else if (!TreeCollectionImpl.equalsWithStop(values[i], other.values[i], stop)) {
                        stop[0] = true;
                        return false;
                    } else if (Age.age(values[i]) > Age.age(other.values[i])) {
                        other.values[i] = values[i];
                        return true;
                    } else {
                        values[i] = other.values[i];
                        return true;
                    }
                });
            }
        }

        private Object set(int i, Object niw, int oldPos, long newMask, int newLen) {
            Object old = oldPos >= 0 ? values[oldPos] : null;
            if (Objects.equals(old, niw)) {
                return this;
            } else {
                byte d = depth(niw);
                if (d < depth - 1) {
                    if (old != null && depth == depth(old) + 1) {
                        for (int ii = 0; ii < values.length; ii++) {
                            if (ii != oldPos) {
                                d = max(d, depth(values[ii]));
                            }
                        }
                    } else {
                        d = (byte) (depth - 1);
                    }
                }
                int newPos = getIt(newMask, i);
                assert newPos >= 0 || oldPos >= 0;
                Object[] result = new Object[newLen];
                System.arraycopy(values, 0, result, 0, old != null ? oldPos : newPos);
                oldPos = old != null ? oldPos + 1 : newPos;
                if (niw != null) {
                    System.arraycopy(values, oldPos, result, newPos + 1, values.length - oldPos);
                    result[newPos] = niw;
                } else {
                    assert old != null;
                    System.arraycopy(values, oldPos, result, oldPos - 1, values.length - oldPos);
                }
                return new HashMultiValue(result, size + size(niw) - size(old), hash + hash(niw) - hash(old), (byte) (d + 1), index, level, newMask);
            }
        }

        private static Object of(Object v1, Object v2, int index, byte level, long newMask) {
            return new HashMultiValue(new Object[]{v1, v2}, size(v1) + size(v2), hash(v1) + hash(v2), (byte) (max(depth(v1), depth(v2)) + 1), index, level, newMask);
        }

        private static HashMultiValue of(Object v1, Object v2, int index) {
            return new HashMultiValue(new Object[]{v1, v2}, 2, index * 2, (byte) 2, index, NR_OF_PARTS, 3);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private Object set(Function key, Object find, Object set) {
            assert (level == NR_OF_PARTS);
            int si = values.length;
            for (int i = 0; i < values.length; i++) {
                if (key.apply(values[i]).equals(find)) {
                    si = i;
                    break;
                }
            }
            if (set == null) {
                if (si == values.length) {
                    return this;
                } else if (values.length == 2) {
                    return si == 0 ? values[1] : values[0];
                } else {
                    Object[] result = new Object[values.length - 1];
                    System.arraycopy(values, 0, result, 0, si);
                    System.arraycopy(values, si + 1, result, si, values.length - si - 1);
                    return new HashMultiValue(result, result.length, hash - index, (byte) 2, index, NR_OF_PARTS, mask & ~(1L << values.length));
                }
            } else if (si != values.length && values[si].equals(set)) {
                return this;
            } else {
                Object[] result = new Object[si == values.length ? values.length + 1 : values.length];
                System.arraycopy(values, 0, result, 0, values.length);
                result[si] = set;
                if (result.length > EQUAL_HASHCODE_WARNING_LEVEL) {
                    System.err.println("WARNING: " + result.length + " non equal objects with equal hashcode " + //
                            StringUtil.toString(Arrays.copyOf(result, EQUAL_HASHCODE_WARNING_LEVEL)));
                }
                return new HashMultiValue(result, result.length, result.length * index, (byte) 2, index, NR_OF_PARTS, si == values.length ? mask | 1L << si : mask);
            }
        }
    }

    private static int getIt(long mask, int idx) {
        return (mask & 1L << idx) == 0 ? -1 : idx == 0 ? 0 : Long.bitCount(mask << (Long.SIZE - idx));
    }

    protected static <T> Object addAll(Object value, Function<T, Object> key, T[] adds) {
        for (T added : adds) {
            value = add(value, key, added, key);
        }
        return value;
    }

    protected static <T> Object addAll(Object value, Function<T, Object> key, java.util.Collection<? extends T> adds) {
        for (T added : adds) {
            value = add(value, key, added, key);
        }
        return value;
    }

    protected static <T> Object putAll(Object value, Function<T, Object> key, T[] adds) {
        for (T added : adds) {
            value = put(value, key, added, key);
        }
        return value;
    }

    protected static <T> Object putAll(Object value, Function<T, Object> key, java.util.Collection<? extends T> adds) {
        for (T added : adds) {
            value = put(value, key, added, key);
        }
        return value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static <T> T get(Object v, Function key, Object find) {
        if (v == null) {
            return null;
        } else {
            int id = find.hashCode(), it;
            byte level = -1;
            while (v instanceof HashMultiValue) {
                HashMultiValue mv = (HashMultiValue) v;
                if (mv.level == level + 1 || (id & INDEX_MASKS[mv.level - 1]) == mv.index) {
                    if (mv.level == NR_OF_PARTS) {
                        for (it = 0; it < mv.values.length; it++) {
                            v = mv.values[it];
                            if (key.apply(v).equals(find)) {
                                return (T) v;
                            }
                        }
                        return null;
                    } else {
                        it = getIt(mv.mask, (id & PART_MASKS[mv.level]) >>> PART_SHIFTS[mv.level]);
                        if (it >= 0) {
                            level = mv.level;
                            v = mv.values[it];
                        } else {
                            return null;
                        }
                    }
                } else {
                    return null;
                }
            }
            return key.apply(v).equals(find) ? (T) v : null;
        }
    }

    @SuppressWarnings("rawtypes")
    protected static Object set(Object val1, Function key1, Function set1, Object val2, Function key2, Function set2, BiFunction set12) {
        return set(val1, key1, index(val1, key1), set1, val2, key2, index(val2, key2), set2, (byte) 0, 0, set12, false);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object set(Object val1, Function key1, int id1, Function set1, Object val2, Function key2, int id2, Function set2, byte lev, int idx, BiFunction set12, boolean flip) {
        if (val1 == null && val2 == null) {
            return null;
        } else if (val2 == null) {
            return set1.apply(val1);
        } else if (val1 == null) {
            return set2.apply(val2);
        } else {
            int i1 = -1, i2 = -1;
            for (int max = Math.min(level(val1), level(val2)); lev < max; lev++) {
                i1 = id1 & PART_MASKS[lev];
                i2 = id2 & PART_MASKS[lev];
                if (i2 != i1) {
                    break;
                } else {
                    idx |= i1;
                }
            }
            if (i2 == i1 && key(val1, key1).equals(key(val2, key2))) {
                return flip ? set12.apply(val2, val1) : set12.apply(val1, val2);
            } else if (lev == NR_OF_PARTS) {
                return setEqualHashes(val1, key1, set1, val2, key2, set2, idx, set12, flip);
            } else if (val1 instanceof HashMultiValue && ((HashMultiValue) val1).level == lev && val2 instanceof HashMultiValue && ((HashMultiValue) val2).level == lev) {
                return setMultiMulti((HashMultiValue) val1, key1, set1, (HashMultiValue) val2, key2, set2, lev, idx, set12, flip);
            } else if (val1 instanceof HashMultiValue && ((HashMultiValue) val1).level == lev) {
                return setMultiOne((HashMultiValue) val1, key1, set1, val2, key2, id2, set2, lev, idx, set12, flip);
            } else if (val2 instanceof HashMultiValue && ((HashMultiValue) val2).level == lev) {
                return setMultiOne((HashMultiValue) val2, key2, set2, val1, key1, id1, set1, lev, idx, set12, !flip);
            } else {
                val1 = set1.apply(val1);
                val2 = set2.apply(val2);
                if (val1 == null) {
                    return val2;
                } else if (val2 == null) {
                    return val1;
                } else {
                    i2 >>>= PART_SHIFTS[lev];
                    i1 >>>= PART_SHIFTS[lev];
                    long downMask = 1L << i1 | 1L << i2;
                    if (i1 < i2) {
                        return HashMultiValue.of(val1, val2, idx, lev, downMask);
                    } else {
                        return HashMultiValue.of(val2, val1, idx, lev, downMask);
                    }
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object setMultiOne(HashMultiValue mv1, Function key1, Function set1, Object val2, Function key2, int id2, Function set2, byte lev, int idx, BiFunction set12, boolean flip) {
        int p2 = id2 & PART_MASKS[lev], i = p2 >>> PART_SHIFTS[lev], it1 = getIt(mv1.mask, i);
        Object val = it1 >= 0 ? mv1.values[it1] : null;
        val = set(val, key1, index(val, key1), set1, val2, key2, id2, set2, (byte) (lev + 1), idx | p2, set12, flip);
        if (set1 == nullFunction()) {
            return val;
        } else if (set1 == identity()) {
            long downMask = val == null ? (mv1.mask & ~(1L << i)) : (mv1.mask | 1L << i);
            int newLength = Long.bitCount(downMask);
            if (newLength == 1) {
                return mv1.values[getIt(mv1.mask, Long.numberOfTrailingZeros(downMask))];
            } else {
                return mv1.set(i, val, it1, downMask, newLength);
            }
        } else {
            Object[] result = null;
            int len = 0, hash = 0, size = 0;
            byte depth = 0;
            long mask = mv1.mask;
            boolean eq = true;
            Object v, e;
            for (int it = 0; it < mv1.values.length; it++) {
                v = mv1.values[it];
                e = it == it1 ? val : set1.apply(v);
                if (e != v) {
                    eq = false;
                }
                if (e != null) {
                    if (result == null) {
                        result = new Object[Long.bitCount(mask)];
                    }
                    result[len++] = e;
                    hash += hash(e);
                    size += size(e);
                    depth = max(depth, depth(e));
                } else {
                    mask &= ~(1L << i);
                }
            }
            if (len == 0) {
                return null;
            } else if (len == 1) {
                return result[0];
            } else if (eq) {
                return mv1;
            } else {
                result = len == result.length ? result : Arrays.copyOf(result, len);
                return new HashMultiValue(result, size, hash, (byte) (depth + 1), idx, lev, mask);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static Object setMultiMulti(HashMultiValue mv1, Function key1, Function set1, HashMultiValue mv2, Function key2, Function set2, byte lev, int idx, BiFunction set12, boolean flip) {
        long mask = (mv1.mask & mv2.mask) | (set1 != nullFunction() ? mv1.mask : 0L) | (set2 != nullFunction() ? mv2.mask : 0L);
        Object[] result = null;
        int len = 0, hash = 0, size = 0, i, i1, i2, l;
        byte depth = 0;
        boolean eq1 = (mask & mv1.mask) == mv1.mask, eq2 = (mask & mv2.mask) == mv2.mask;
        if (set1 == identity()) {
            size += mv1.size;
            hash += mv1.hash;
        }
        if (set2 == identity()) {
            size += mv2.size;
            hash += mv2.hash;
        }
        Object e, e1, e2;
        for (i = Long.numberOfTrailingZeros(mask); i < Long.SIZE; i += Long.numberOfTrailingZeros(mask >>> i)) {
            i1 = getIt(mv1.mask, i);
            i2 = getIt(mv2.mask, i);
            if (i1 >= 0 && i2 < 0) {
                if (set1 == identity()) {
                    l = Math.min(Long.SIZE - i, Math.min(Long.numberOfTrailingZeros(~mv1.mask >>> i), Long.numberOfTrailingZeros(mv2.mask >>> i)));
                    if (result == null) {
                        result = new Object[Long.bitCount(mask)];
                    }
                    System.arraycopy(mv1.values, i1, result, len, l);
                    len += l;
                    i += l;
                    eq2 = false;
                    continue;
                } else {
                    eq1 = false;
                }
            }
            if (i2 >= 0 && i1 < 0) {
                if (set2 == identity()) {
                    l = Math.min(Long.SIZE - i, Math.min(Long.numberOfTrailingZeros(~mv2.mask >>> i), Long.numberOfTrailingZeros(mv1.mask >>> i)));
                    if (result == null) {
                        result = new Object[Long.bitCount(mask)];
                    }
                    System.arraycopy(mv2.values, i2, result, len, l);
                    len += l;
                    i += l;
                    eq1 = false;
                    continue;
                } else {
                    eq2 = false;
                }
            }
            e1 = i1 >= 0 ? mv1.values[i1] : null;
            e2 = i2 >= 0 ? mv2.values[i2] : null;
            if (e1 != null && set1 == identity()) {
                hash -= hash(e1);
                size -= size(e1);
            }
            if (e2 != null && set2 == identity()) {
                hash -= hash(e2);
                size -= size(e2);
            }
            e = set(e1, key1, index(e1, key1), set1, e2, key2, index(e2, key2), set2, (byte) (lev + 1), idx | (i << PART_SHIFTS[lev]), set12, flip);
            if (e != e1) {
                eq1 = false;
            }
            if (e != e2) {
                eq2 = false;
            }
            if (e != null) {
                if (result == null) {
                    result = new Object[Long.bitCount(mask)];
                }
                result[len++] = e;
                hash += hash(e);
                size += size(e);
                depth = max(depth, depth(e));
            } else {
                mask &= ~(1L << i);
            }
            i++;
        }
        if (len == 0) {
            return null;
        } else if (len == 1) {
            return result[0];
        } else if (eq1) {
            return mv1;
        } else if (eq2) {
            return mv2;
        } else {
            result = len == result.length ? result : Arrays.copyOf(result, len);
            if (depth < max(mv1.depth, mv2.depth) - 1) {
                depth = 0;
                for (i = 0; i < result.length; i++) {
                    depth = max(depth, depth(result[i]));
                }
            }
            return new HashMultiValue(result, size, hash, (byte) (depth + 1), idx, lev, mask);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object setEqualHashes(Object val1, Function key1, Function set1, Object val2, Function key2, Function set2, int idx, BiFunction set12, boolean flip) {
        int len1 = length(val1), len2 = length(val2);
        if (len1 + len2 > 2) {
            Object e1, k1, e2, k2, e;
            Object[] result = null;
            int len = 0, i1, i2;
            boolean eq1 = true, eq2 = true;
            next1:
            for (i1 = 0; i1 < len1; i1++) {
                e1 = get(val1, i1);
                k1 = key1.apply(e1);
                for (i2 = 0; i2 < len2; i2++) {
                    e2 = get(val2, i2);
                    k2 = key2.apply(e2);
                    if (k1.equals(k2)) {
                        e = flip ? set12.apply(e2, e1) : set12.apply(e1, e2);
                        if (e != e1) {
                            eq1 = false;
                        }
                        if (e != e2) {
                            eq2 = false;
                        }
                        if (e != null) {
                            if (result == null) {
                                result = new Object[len1 + len2];
                            }
                            result[len++] = e;
                        }
                        continue next1;
                    }
                }
                e = set1.apply(e1);
                if (e != e1) {
                    eq1 = false;
                }
                eq2 = false;
                if (e != null) {
                    if (result == null) {
                        result = new Object[len1 + len2];
                    }
                    result[len++] = e;
                }
            }
            if (set2 != nullFunction()) {
                next2:
                for (i2 = 0; i2 < len2; i2++) {
                    e2 = get(val2, i2);
                    k2 = key2.apply(e2);
                    for (i1 = 0; i1 < len1; i1++) {
                        k1 = key1.apply(get(val1, i1));
                        if (k1.equals(k2)) {
                            continue next2;
                        }
                    }
                    e = set2.apply(e2);
                    eq1 = false;
                    if (e != e2) {
                        eq2 = false;
                    }
                    if (e != null) {
                        if (result == null) {
                            result = new Object[len1 + len2];
                        }
                        result[len++] = e;
                    }
                }
            }
            if (len == 0) {
                return null;
            } else if (len == 1) {
                return result[0];
            } else if (eq1) {
                return val1;
            } else if (eq2) {
                return val2;
            } else {
                result = len == result.length ? result : Arrays.copyOf(result, len);
                if (len > EQUAL_HASHCODE_WARNING_LEVEL) {
                    System.err.println("WARNING: " + len + " non equal objects with equal hashcode " + //
                            StringUtil.toString(Arrays.copyOf(result, EQUAL_HASHCODE_WARNING_LEVEL)));
                }
                return new HashMultiValue(result, len, len * idx, (byte) 2, idx, NR_OF_PARTS, 0);
            }
        } else {
            val1 = set1.apply(val1);
            val2 = set2.apply(val2);
            if (val1 == null) {
                return val2;
            } else if (val2 == null) {
                return val1;
            } else {
                return HashMultiValue.of(val1, val2, idx);
            }
        }
    }

    protected static <T1, T2> Object add(Object value, Function<T1, Object> key1, Object added, Function<T2, Object> key2) {
        return set(value, key1, identity(), added, key2, identity(), RETURN_1);
    }

    protected static <T1, T2> Object put(Object value, Function<T1, Object> key1, Object added, Function<T2, Object> key2) {
        return set(value, key1, identity(), added, key2, identity(), RETURN_2);
    }

    protected static <T1, T2> Object remove(Object value, Function<T1, Object> key1, Object removed, Function<T2, Object> key2) {
        return set(value, key1, identity(), removed, key2, nullFunction(), RETURN_NULL);
    }

    protected static <T1, T2> Object retain(Object value, Function<T1, Object> key1, Object retained, Function<T2, Object> key2) {
        return set(value, key1, nullFunction(), retained, key2, nullFunction(), RETURN_1);
    }

    protected static <T1, T2> void deduplicate(Object value, Function<T1, Object> key1, Object retained, Function<T2, Object> key2) {
        set(value, key1, nullFunction(), retained, key2, nullFunction(), PRUNE);
    }

    protected static <T1, T2> Object exclusive(Object value, Function<T1, Object> key1, Object excl, Function<T2, Object> key2) {
        return set(value, key1, identity(), excl, key2, identity(), RETURN_NULL);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected static <T1, T2> Object add(Object value, Function<T1, Object> key1, Object merged, Function<T2, Object> key2, BinaryOperator merger) {
        return set(value, key1, identity(), merged, key2, identity(), merger::apply);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected static <T1, T2> Object remove(Object value, Function<T1, Object> key1, Object merged, Function<T2, Object> key2, BinaryOperator merger) {
        return set(value, key1, identity(), merged, key2, nullFunction(), merger::apply);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected StreamCollection<Object[]> getCompareStream(ContainingCollection<? extends T> toCompare) {
        HashCollectionImpl<T> other = (HashCollectionImpl<T>) toCompare;
        return new StreamCollectionImpl<>(new Comparer(key(), value, other.key(), other.value, size() + other.size()), false);
    }

    private static final class Comparer implements Spliterator<Object[]> {
        private static final int VISIT_CHARACTERISTICS = Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL;

        @SuppressWarnings("rawtypes")
        private final Function   key1, key2;
        private final Object     val1, val2;
        private final int        total;

        @SuppressWarnings("rawtypes")
        private Comparer(Function key1, Object val1, Function key2, Object val2, int total) {
            this.key1 = key1;
            this.key2 = key2;
            this.val1 = val1;
            this.val2 = val2;
            this.total = total;
        }

        @Override
        public void forEachRemaining(Consumer<? super Object[]> visitor) {
            Object[] pair = new Object[2];
            set(val1, key1, index(val1, key1), e1 -> {
                pair[0] = e1;
                pair[1] = null;
                visitor.accept(pair);
                return null;
            }, val2, key2, index(val2, key2), e2 -> {
                pair[0] = null;
                pair[1] = e2;
                visitor.accept(pair);
                return null;
            }, (byte) 0, 0, (v1, v2) -> {
                if ((key1 != identity() || key2 != identity()) && !Objects.equals(v1, v2)) {
                    pair[0] = v1;
                    pair[1] = v2;
                    visitor.accept(pair);
                }
                return null;
            }, false);
        }

        @Override
        public boolean tryAdvance(Consumer<? super Object[]> action) {
            throw new UnsupportedOperationException();
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
            return VISIT_CHARACTERISTICS;
        }
    }

    @Override
    public boolean contains(Object e) {
        return get(value, key(), e) != null;
    }

    @Override
    public ContainingCollection<T> addUnique(T e) {
        return add(e);
    }

    @Override
    public ContainingCollection<T> addAllUnique(Collection<? extends T> e) {
        return addAll(e);
    }

    @SuppressWarnings("unchecked")
    protected final Object visit(BiFunction<? super Object[], Integer, Object> visitor, ContainingCollection<? extends T>[] others, int len) {
        len++;
        CompareSates css = COMPARE_STATES.get();
        CompareSate cs = css.open(null, len);
        try {
            cs.values[0][0] = value;
            cs.keys[0][0] = key();
            cs.ids[0][0] = index(value, key());
            byte maxLevel = level(value);
            cs.keep[0][0] = visitor.apply(SINGLES[0], len) == DUMMY;
            for (int i = 1; i < len; i++) {
                HashCollectionImpl<T> other = (HashCollectionImpl<T>) others[i - 1];
                cs.values[0][i] = other.value;
                cs.keys[0][i] = other.key();
                cs.ids[0][i] = index(other.value, other.key());
                maxLevel = min(maxLevel, level(other.value));
                cs.keep[0][i] = visitor.apply(SINGLES[i], len) == DUMMY;
            }
            return cs.visit(visitor, maxLevel, (byte) 0, 0, len, (byte) 0);
        } finally {
            css.close(cs);
        }
    }

    private static final class CompareSates extends Reusable<Object, Object, CompareSate, Integer> {

        private static final long serialVersionUID = 4743227838928248552L;

        public CompareSates() {
            super(null, (c, u) -> new CompareSate(), (cs, c, l) -> cs.open(l), CompareSate::close, CompareSate::isOpen);
        }

    }

    @SuppressWarnings("rawtypes")
    private static final class CompareSate {
        private Object[][]   values;
        private Function[][] keys;
        private boolean[][]  keep;
        private int[][]      ids;
        private long[][]     masks;

        private int          length = -1;

        private CompareSate() {
            values = new Object[NR_OF_PARTS + 2][COMPARE_MAX];
            keys = new Function[NR_OF_PARTS + 2][COMPARE_MAX];
            keep = new boolean[NR_OF_PARTS + 2][COMPARE_MAX];
            ids = new int[NR_OF_PARTS + 2][COMPARE_MAX];
            masks = new long[NR_OF_PARTS + 2][COMPARE_MAX];
        }

        private void open(int length) {
            if (length > values[0].length) {
                values = new Object[NR_OF_PARTS + 2][length];
                keys = new Function[NR_OF_PARTS + 2][length];
                keep = new boolean[NR_OF_PARTS + 2][length];
                ids = new int[NR_OF_PARTS + 2][length];
                masks = new long[NR_OF_PARTS + 2][length];
                if (length > SINGLES.length) {
                    Object[][] singles = new Object[length][length];
                    for (int i = 0; i < length; i++) {
                        singles[i][i] = DUMMY;
                    }
                    SINGLES = singles;
                }
            }
            this.length = length;
        }

        private void close() {
            for (int i = 0; i < values.length; i++) {
                Arrays.fill(values[i], 0, length, null);
                Arrays.fill(keys[i], 0, length, null);
            }
            length = -1;
        }

        private boolean isOpen() {
            return length >= 0;
        }

        private Object visit(BiFunction<? super Object[], Integer, Object> visitor, byte maxLevel, byte level, int index, int len, byte dep) {
            int newLen = equalKeys(len, dep);
            if (newLen >= 0) {
                return visitor.apply(values[dep], newLen);
            } else {
                newLen = -newLen;
                stop:
                for (int cnt = 0, idx, newIndex = index; level < maxLevel; cnt = 0, index = newIndex, level++) {
                    for (int i = 0; i < newLen; i++) {
                        if (values[dep][i] != null) {
                            idx = index | (ids[dep][i] & PART_MASKS[level]);
                            if (cnt++ > 0) {
                                if (idx != newIndex) {
                                    break stop;
                                }
                            } else {
                                newIndex = idx;
                            }
                        }
                    }
                }
                if (level == NR_OF_PARTS) {
                    return visitEqualHashes(visitor, index, newLen, dep);
                } else {
                    return visitUnequalHashes(visitor, level, index, newLen, dep);
                }
            }
        }

        private Object visitUnequalHashes(BiFunction<? super Object[], Integer, Object> visitor, byte level, int index, int len, byte dep) {
            Object result = null, val;
            int resultIdx = -1, idx, prev = 0;
            byte it, maxLevel = -1;
            long downMask, mask = 0;
            for (it = 0; it < len; it++) {
                if (keep[dep][it]) {
                    val = values[dep][it];
                    // use 'idx' for current length
                    idx = val instanceof HashMultiValue && ((HashMultiValue) val).level == level ? ((HashMultiValue) val).values.length : val != null ? 1 : 0;
                    if (idx > prev) {
                        prev = idx;
                        // use 'maxLevel' for selected init value
                        maxLevel = it;
                        resultIdx = (ids[dep][it] & PART_MASKS[level]) >>> PART_SHIFTS[level];
                        result = val;
                    }
                }
            }
            for (it = 0; it < len; it++) {
                downMask = mask(values[dep][it], ids[dep][it], level);
                masks[dep][it] = downMask;
                if (it != maxLevel) {
                    mask |= downMask;
                }
            }
            for (idx = Long.numberOfTrailingZeros(mask); idx < Long.SIZE; idx++, idx += Long.numberOfTrailingZeros(mask >>> idx)) {
                maxLevel = NR_OF_PARTS;
                for (it = 0; it < len; it++) {
                    keep[dep + 1][it] = keep[dep][it];
                    keys[dep + 1][it] = keys[dep][it];
                    // use 'prev' as it
                    prev = getIt(masks[dep][it], idx);
                    if (prev >= 0) {
                        values[dep + 1][it] = get(values[dep][it], level, prev);
                        ids[dep + 1][it] = values[dep + 1][it] == values[dep][it] ? ids[dep][it] : index(values[dep + 1][it], keys[dep][it]);
                        maxLevel = min(level(values[dep + 1][it]), maxLevel);
                    } else {
                        values[dep + 1][it] = null;
                        ids[dep + 1][it] = 0;
                    }
                }
                val = visit(visitor, maxLevel, level, index, len, (byte) (dep + 1));
                if (result == null) {
                    resultIdx = idx;
                    result = val;
                } else if (result instanceof HashMultiValue && ((HashMultiValue) result).level == level && index == ((HashMultiValue) result).index) {
                    downMask = val == null ? (((HashMultiValue) result).mask & ~(1L << idx)) : (((HashMultiValue) result).mask | 1L << idx);
                    // use 'maxlevel' as new length
                    maxLevel = (byte) Long.bitCount(downMask);
                    if (maxLevel == 1) {
                        resultIdx = Long.numberOfTrailingZeros(downMask);
                        result = ((HashMultiValue) result).values[getIt(((HashMultiValue) result).mask, resultIdx)];
                    } else {
                        resultIdx = -1;
                        result = ((HashMultiValue) result).set(idx, val, getIt(((HashMultiValue) result).mask, idx), downMask, maxLevel);
                    }
                } else if (idx == resultIdx) {
                    result = val;
                } else if (val != null) {
                    downMask = 1L << resultIdx | 1L << idx;
                    if (resultIdx < idx) {
                        result = HashMultiValue.of(result, val, index, level, downMask);
                    } else {
                        result = HashMultiValue.of(val, result, index, level, downMask);
                    }
                    resultIdx = -1;
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        private Object visitEqualHashes(BiFunction<? super Object[], Integer, Object> visitor, int index, int len, byte dep) {
            Object obj, other, key, result = null;
            int it, length, base = -1, prev = -1;
            for (it = 0; it < len; it++) {
                if (keep[dep][it]) {
                    obj = values[dep][it];
                    length = obj instanceof HashMultiValue && ((HashMultiValue) obj).level == NR_OF_PARTS ? ((HashMultiValue) obj).values.length : obj != null ? 1 : 0;
                    if (length > prev) {
                        base = it;
                        prev = length;
                        result = values[dep][it];
                    }
                }
            }
            for (int i = 0; i < len; i++) {
                if (i != base) {
                    length = length(values[dep][i]);
                    next:
                    for (int ii = 0; ii < length; ii++) {
                        values[dep + 1][i] = get(values[dep][i], ii);
                        key = keys[dep][i].apply(values[dep + 1][i]);
                        for (int iii = 0; iii < len; iii++) {
                            if (iii != i) {
                                values[dep + 1][iii] = null;
                                it = length(values[dep][iii]);
                                for (int iiii = 0; iiii < it; iiii++) {
                                    other = get(values[dep][iii], iiii);
                                    if (key.equals(keys[dep][iii].apply(other))) {
                                        if (base != iii && iii < i) {
                                            // already done
                                            continue next;
                                        } else {
                                            values[dep + 1][iii] = other;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        obj = visitor.apply(values[dep + 1], len);
                        if (result == null) {
                            result = obj;
                        } else if (result instanceof HashMultiValue && ((HashMultiValue) result).level == NR_OF_PARTS) {
                            assert index == ((HashMultiValue) result).index;
                            result = ((HashMultiValue) result).set(keys[dep][0], key, obj);
                        } else if (!(result instanceof HashMultiValue) && keys[dep][0].apply(result).equals(key)) {
                            result = obj;
                        } else if (obj != null) {
                            result = HashMultiValue.of(result, obj, index);
                        }
                    }
                }
            }
            return result;

        }

        private int equalKeys(int len, byte dep) {
            Object prev = null;
            int l = 0, i, ii;
            boolean equal = true;
            for (i = 0; i < len; i++) {
                for (ii = 0; ii < l; ii++) {
                    if (keep[dep][i] == keep[dep][ii] && ids[dep][i] == ids[dep][ii] && //
                            keys[dep][i] == keys[dep][ii] && values[dep][i] == values[dep][ii]) {
                        break;
                    }
                }
                if (ii == l) {
                    keep[dep][l] = keep[dep][i];
                    ids[dep][l] = ids[dep][i];
                    keys[dep][l] = keys[dep][i];
                    values[dep][l] = values[dep][i];
                    if (equal && values[dep][l] != null) {
                        Object key = key(values[dep][l], keys[dep][l]);
                        if (prev != null) {
                            equal = prev.equals(key);
                        } else {
                            prev = key;
                        }
                    }
                    l++;
                }
            }
            return equal ? l : -l;
        }
    }

}
