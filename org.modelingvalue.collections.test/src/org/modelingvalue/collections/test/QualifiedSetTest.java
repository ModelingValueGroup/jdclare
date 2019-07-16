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

package org.modelingvalue.collections.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.junit.Test;
import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Context;
import org.modelingvalue.collections.util.ContextThread;
import org.modelingvalue.collections.util.SerializableFunction;
//import org.modelingvalue.jdclare.DNamed;

public class QualifiedSetTest {

    private static final Context<Object> CONTEXT = Context.of();

    private static class O {
        public String k, v;

        private O(String a, String b) {
            k = a;
            v = b;
        }

        public static O of(String str) {
            return new O("k" + str, str);
        }
    }

    @Test
    public void test() throws Exception {
        Set<O> set1 = Set.of(O.of("noot"), O.of("mies"), O.of("teun"), O.of("mies"), O.of("jet"), O.of("aap"));
        assertEquals(6, set1.size());
        Set<O> set2 = Set.of(O.of("aap"), O.of("jet"), O.of("mies"), O.of("noot"), O.of("noot"), O.of("noot"), O.of("teun"));
        assertEquals(7, set2.size());

        QualifiedSet<String, O> qset3 = QualifiedSet.of(o -> o.k, O.of("noot"), O.of("mies"), O.of("teun"), O.of("mies"), O.of("jet"), O.of("aap"));
        assertEquals(5, qset3.size());
        QualifiedSet<String, O> qset4 = QualifiedSet.of(o -> o.k, O.of("aap"), O.of("jet"), O.of("mies"), O.of("noot"), O.of("noot"), O.of("noot"), O.of("teun"));
        assertEquals(5, qset4.size());
        assertTrue(qset3.containsAll(qset4));
        assertTrue(qset4.containsAll(qset3));
        qset3.forEach(obj -> assertTrue(qset4.map(a -> a.v).sequential().toSet().contains(obj.v)));

        qset4.forEach(obj -> qset3.contains(obj));
        String expected = "kaap" + "kjet" + "kmies" + "knoot" + "kteun";

        String reduce1 = qset3.map(o -> o.k).sequential().reduce("", (a, b) -> a + b);
        String reduce2 = qset4.map(o -> o.k).sequential().reduce("", (a, b) -> a + b);

        assertEquals(expected.length(), reduce1.length());
        assertEquals(expected.length(), reduce2.length());
        assertEquals(expected, reduce1);
        assertEquals(expected, reduce2);

        expected = "aap" + "jet" + "mies" + "noot" + "teun";

        String reduce3 = qset3.map(o -> o.v).sequential().reduce("", (a, b) -> a + b);
        String reduce4 = qset4.map(o -> o.v).sequential().reduce("", (a, b) -> a + b);

        assertEquals(expected.length(), reduce3.length());
        assertEquals(expected.length(), reduce4.length());
        assertEquals(expected, reduce3);
        assertEquals(expected, reduce4);

    }

    @SuppressWarnings("serial")
    @Test
    public void bigTest() throws Exception {
        ContextThread.createPool().invoke(new RecursiveAction() {
            @Override
            protected void compute() {
                Object ctx = new Object();
                CONTEXT.set(ctx);
                QualifiedSet<String, Long> set = QualifiedSet.of(l -> l.toString(), Collection.of(LongStream.range(Long.MAX_VALUE - 10_000_000, Long.MAX_VALUE)).collect(Collectors.toSet()));
                Double sum = set.toSet().reduce(0d, (s, e) -> {
                    assertEquals(ctx, CONTEXT.get());
                    return s + e;
                }, (s1, s2) -> {
                    assertEquals(ctx, CONTEXT.get());
                    return s1 + s2;
                });
                assertEquals(ctx, CONTEXT.get());
                System.err.println(sum + " / " + set.size() + " = " + (sum / set.size()));
            }
        });
    }

    @Test
    public void equalTest() throws Exception {
        java.util.Collection<O> collection = Set.of(O.of("noot"), O.of("mies"), O.of("teun"), O.of("mies"), O.of("jet"), O.of("aap")).collect(Collectors.toSet());

        SerializableFunction<O, String> f = o -> o.k;
        QualifiedSet<String, O> qset1 = QualifiedSet.of(f, collection);
        QualifiedSet<String, O> qset2 = QualifiedSet.of(f, collection);
        assertEquals(qset1, qset2);

        QualifiedSet<String, O> qset3 = QualifiedSet.of(QualifiedSetTest::k, collection);
        QualifiedSet<String, O> qset4 = QualifiedSet.of(QualifiedSetTest::k, collection);
        assertEquals(qset3, qset4);

    }

    private static String k(O o) {
        return o.k;
    }

    @Test
    public void subsetTest() throws Exception {
        int max = 500_000;
        Set<Integer> set0 = Collection.of(IntStream.range(0, max * 2)).toSet();
        SerializableFunction<Integer, String> f = i -> i.toString();
        QualifiedSet<String, Integer> qset0 = QualifiedSet.of(f, set0.collect(Collectors.toSet()));

        Set<Integer> set1 = Collection.of(IntStream.range(0, max).map(i -> i * 2)).toSet();
        Set<Integer> set2 = Collection.of(IntStream.range(0, max).map(i -> i * 2 + 1)).toSet();
        Set<Integer> set3 = Collection.of(IntStream.range(0, max)).toSet();
        Set<Integer> set4 = Collection.of(IntStream.range(max, max * 2)).toSet();

        QualifiedSet<String, Integer> qset1 = QualifiedSet.of(f, set1.collect(Collectors.toSet()));
        QualifiedSet<String, Integer> qset2 = QualifiedSet.of(f, set2.collect(Collectors.toSet()));
        QualifiedSet<String, Integer> qset3 = QualifiedSet.of(f, set3.collect(Collectors.toSet()));
        QualifiedSet<String, Integer> qset4 = QualifiedSet.of(f, set4.collect(Collectors.toSet()));

        assertEquals(qset1.size() * 2, qset0.size());
        assertEquals(qset1.size(), qset2.size());
        assertEquals(qset2.size(), qset3.size());
        assertEquals(qset3.size(), qset4.size());

        assertNotEquals(qset1, qset2);
        assertNotEquals(qset2, qset1);
        assertNotEquals(qset3, qset4);
        assertNotEquals(qset4, qset3);
        assertNotEquals(qset0, qset1);
        assertNotEquals(qset0, qset2);
        assertNotEquals(qset0, qset3);
        assertNotEquals(qset0, qset4);
    }
    //
    //    @Test
    //    public void merge() throws Exception {
    //
    //        Set<Integer> setx = Set.of(-2000, -1900, -1800);
    //        Set<Integer> sety = Set.of(2000, 1900, 1800);
    //        System.err.println();
    //        setx.compare(sety).forEach(c -> System.err.println(Arrays.deepToString(c)));
    //
    //        Set<Integer> seta = Set.of(-20, -19, 20);
    //        Set<Integer> setb = Set.of(-20, -19);
    //        System.err.println();
    //        seta.compare(setb).forEach(c -> System.err.println(Arrays.deepToString(c)));
    //
    //        Set<Integer> set0 = Set.of(-20, -19, 0, 30, 40);
    //        Set<Integer> set1 = Set.of(-100, -99, -98, -70, -20, -19, 10);
    //        Set<Integer> set2 = Set.of(-20, -19, 10, 40, 70, 98, 99, 100);
    //
    //        System.err.println();
    //        set0.compare(set1).forEach(c -> System.err.println(Arrays.deepToString(c)));
    //
    //        System.err.println();
    //        set0.compare(set2).forEach(c -> System.err.println(Arrays.deepToString(c)));
    //
    //        Set<Integer> merged = set0.merge2(set1, set2);
    //
    //        Set<Integer> set12 = set1.addAll(set2).remove(40);
    //        assertEquals(set12, merged);
    //
    //        Set<Integer> all = set1.addAll(set0).addAll(set2);
    //        assertNotEquals(all, merged);
    //
    //        Set<Long> lset0 = Collection.of(LongStream.range(20, 100)).toSet();
    //        Set<Long> lset1 = Collection.of(LongStream.range(40, 120)).toSet();
    //
    //        System.err.println();
    //        lset0.compare(lset1).forEach(c -> System.err.println(Arrays.deepToString(c)));
    //
    //        Set<Integer> setA = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);
    //        Set<Integer> setB = Set.of(10);
    //        System.err.println();
    //        setA.compare(setB).forEach(c -> System.err.println(Arrays.deepToString(c)));
    //    }

}
