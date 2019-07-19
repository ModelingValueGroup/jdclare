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

import static org.junit.Assert.*;

import org.junit.Test;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Pair;

public class MapTest {

    @Test
    public void test() throws Exception {
        Map<String, String> map1 = Map.of(Entry.of("A", "xxxxx"), Entry.of("B", "yyyyy"), Entry.of("B", "bbbbb"), Entry.of("A", "aaaaa"));
        Map<String, String> map2 = Map.of(Entry.of("B", "bbbbb"), Entry.of("A", "aaaaa"), Entry.of("A", "xxxxx"), Entry.of("B", "yyyyy"));
        Map<String, String> map3 = map1.putAll(map2);
        Map<String, String> map4 = map1.put("B", "yyyyy").put("C", "zzzzz");
        Map<String, String> map5 = map4.removeAllKey(map1);
        Map<String, String> map6 = map4.removeAllKey(map1.toKeys());
        Set<String> set = Set.of("B", "A");
        assertEquals(2, map1.size());
        assertEquals(2, map2.size());
        assertEquals(1, map5.size());
        assertEquals("aaaaa", map1.get("A"));
        assertEquals("bbbbb", map1.get("B"));
        assertEquals("xxxxx", map2.get("A"));
        assertEquals("yyyyy", map2.get("B"));
        assertEquals("zzzzz", map5.get("C"));
        assertEquals(map2, map3);
        assertEquals(map5, map6);
        System.err.println(map1);
        System.err.println(map2);
        System.err.println(map3);
        System.err.println(map4);
        System.err.println(map5);
        System.err.println(map6);
        assertNotEquals(map1.hashCode(), map2.hashCode());
        assertNotEquals(map1, map2);
        assertNotEquals(map1, map4);
        assertNotEquals(map2, map4);
        Set<String> keySet1 = map1.toKeys().toSet();
        Set<String> keySet2 = map2.toKeys().toSet();
        assertEquals(keySet1, keySet2);
        assertEquals(set, keySet2);
        assertEquals(keySet2, set);
    }

    @Test
    public void compareTest() throws Exception {
        Map<String, String> map1 = Map.of(Entry.of("A", "xxxxx"), Entry.of("B", "yyyyy"), Entry.of("C", "ccccc"), Entry.of("D", "ddddd"));
        Map<String, String> map2 = Map.of(Entry.of("A", "aaaaa"), Entry.of("B", "bbbbb"), Entry.of("C", "ccccc"), Entry.of("D", "ddddd"));
        System.err.println(map1.compare(map2).map(a -> Pair.of(a[0], a[1])).toSet().toString());
        System.err.println(map1.diff(map2).toString());
    }

    @Test
    public void identityTest() throws Exception {
        Map<String, String> map = Map.of(Entry.of("a", "1"));
        assertTrue(map == map.removeKey("d"));
        assertTrue(map == map.put("a", "1"));
    }

    @Test
    public void nullValues() {
        Map<String, String> map0 = Map.of();
        Map<String, String> map1 = Map.of(Entry.of("AA", "aa"));
        Map<String, String> map2 = Map.of(Entry.of("AA", null));
        assertEquals(map2.size(), 1);
        assertEquals(map0.merge(map1, map2), map1);
    }

    @Test
    public void basicMapOperations() {
        Map<String, String> map1 = Map.of(Entry.of("AA", "aa"));
        assertEquals(1, map1.size());

        //Test map uses equals...
        String newAA = new String("AA");
        if (newAA != "AA") {
            map1 = map1.put(newAA, "bb");
        }
        assertEquals(1, map1.size());
        assertEquals("bb", map1.get(newAA));

        // is the null key allowed ?
        // Map<String, String> map2 = map1.put(null, "cc");
        // assertEquals("cc", map2.get(null));
    }

    @Test
    public void mapMerge() {
        Map<String, String> map1 = Map.of(Entry.of("AA", "aa"));
        Map<String, String> map2 = Map.of(Entry.of("AA", "aa"));
        Map<String, String> map3 = Map.of(Entry.of("AA", "bb"));

        Map<String, String> map4 = Map.of(Entry.of("AA", "aaaa"));
        Map<String, String> map5 = Map.of(Entry.of("AA", "aabb"));

        Map<String, String> map6 = map1.addAll(map2, (a, b) -> a + b);
        Map<String, String> map7 = map1.addAll(map3, (a, b) -> a + b);

        System.err.println(map1);
        System.err.println(map2);
        System.err.println(map3);
        System.err.println(map4);
        System.err.println(map5);
        System.err.println(map6);
        System.err.println(map7);

        assertEquals(map2, map1);
        assertEquals(map5, map7);

        assertEquals(map4, map6);

    }

    @Test
    public void simpleMerge() {
        Map<String, String> map1 = Map.of(Entry.of("AA", "aa"));
        Map<String, String> map2 = Map.of(Entry.of("AA", "aa"));
        Map<String, String> map3 = Map.of(Entry.of("AA", "bb"));

        Map<String, String> map4 = map1.merge(map2, map3);
        assertEquals(map4, map3);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void mapMergeEqualHashCodes() {
        Map<EqualHashCode, String> map1 = Map.of(Entry.of(new EqualHashCode(), "aa"));
        Map<EqualHashCode, String> map2 = Map.of(Entry.of(new EqualHashCode(), "bb"));
        Map<EqualHashCode, String> map3 = Map.of(Entry.of(new EqualHashCode(), "cc"));
        Map<EqualHashCode, String> map4 = Map.of(Entry.of(new EqualHashCode(), "dd"));
        Map<EqualHashCode, String> map5 = Map.of(Entry.of(new EqualHashCode(), "ee"));
        Map<EqualHashCode, String> map6 = Map.of(Entry.of(new EqualHashCode(), "ff"));
        Map<EqualHashCode, String> result = map1.merge((o, s, ss, l) -> {
            throw new Error();
        }, new Map[]{map2, map3, map4, map5, map6}, 5);

        assertEquals(result, map2.addAll(map3).addAll(map4).addAll(map5).addAll(map6));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void combineTest() throws Exception {
        Map<String, Map> map1 = Map.of(Entry.of("a", Map.of(Entry.of("x", "1"))));
        Map<String, Map> map2 = Map.of(Entry.of("a", Map.of(Entry.of("x", "1"))), Entry.of("b", Map.of(Entry.of("x", "1"))), Entry.of("c", Map.of(Entry.of("x", Set.of("1")))));
        Map<String, Map> map3 = Map.of(Entry.of("a", Map.of(Entry.of("x", "1"))), Entry.of("c", Map.of(Entry.of("x", Set.of("2")))));

        Map<String, Map> result = map1.merge(new Map[]{map2, map3});
        Map<String, Map> expect = Map.of(Entry.of("a", Map.of(Entry.of("x", "1"))), Entry.of("b", Map.of(Entry.of("x", "1"))), Entry.of("c", Map.of(Entry.of("x", Set.of("1", "2")))));
        System.err.println(result);
        System.err.println(expect);
        assertEquals(result, expect);
    }

    private final static class EqualHashCode {

        @Override
        public int hashCode() {
            return 1234;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public String toString() {
            return "EHC@" + System.identityHashCode(this);
        }

    }
}
