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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import org.junit.Test;
import org.modelingvalue.collections.DefaultMap;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.impl.ListImpl;
import org.modelingvalue.collections.impl.SetImpl;

public class SerializeTest {

    static class NotSerializable {
        public String nonsense;

        public NotSerializable() {

        }
    }

    static class AnObject implements Serializable {

        private static final long serialVersionUID = 1636023751463490430L;
        private int               i;

        public AnObject(int i) {
            this.i = i;
        }

        @Override
        public String toString() {
            return "#" + i + "#";
        }
    }

    static class AnObjectWithEquals implements Serializable {

        private static final long serialVersionUID = 1636023751463490430L;
        private String            s;

        public AnObjectWithEquals(int i) {
            this.s = Integer.toString(i);
        }

        @Override
        public int hashCode() {
            return s.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof AnObjectWithEquals) {
                return ((AnObjectWithEquals) o).s.equals(s);
            }
            return false;
        }

        @Override
        public String toString() {
            return "#" + s + "#";
        }
    }

    @Test
    public void serializeQualifiedSet() {
        QualifiedSet<String, String> qset = QualifiedSet.of(s -> s);
        Set<String> set = Set.of("a", "b", "c", "d", "e");
        qset = qset.addAll(set);
        testSerializability(qset);
    }

    @Test
    public void serializeDefaultMap() {
        DefaultMap<String, String> dmap = DefaultMap.of(s -> s);
        Set<String> set = Set.of("a", "b", "c", "d", "e");
        dmap = dmap.addAll(set.map(s -> Entry.of(s, s)));
        testSerializability(dmap);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void serializeList() {
        List<Integer> listOfInt = List.of(1, 2, 3, 4, 5);
        testSerializability(listOfInt);

        List<Integer> listOfInt1000 = ListImpl.EMPTY;
        for (int i = 0; i < 1000; i++) {
            listOfInt1000 = listOfInt1000.add(i);
        }
        testSerializability(listOfInt);
    }

    @Test
    public void serializeMap() {
        Map<String, String> i = Map.of();

        i = i.put("a", "0");
        i = i.put("b", "1");
        i = i.put("c", "2");
        i = i.put("d", "3");
        i = i.put("e", "4");

        testSerializability(i);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void serializeSet() {
        Set<Integer> setOfInt = Set.of(1, 2, 3, 4, 5);
        testSerializability(setOfInt);

        Set<Integer> setOfInt1000 = SetImpl.EMPTY;
        for (int i = 0; i < 1000; i++) {
            setOfInt1000 = setOfInt1000.add(i);
        }
        testSerializability(setOfInt1000);

        NotSerializable not = new NotSerializable();
        Set<NotSerializable> setOfNots = Set.of(not);
        byte[] bytes = writeObject(setOfNots);
        assertNull(bytes);

        Set<AnObject> setOfAnObjects = SetImpl.EMPTY;

        java.util.Map<Integer, AnObject> elements = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            AnObject e = new AnObject(i);
            setOfAnObjects = setOfAnObjects.add(e);
            elements.put(i, e);
        }
        bytes = writeObject(setOfAnObjects);
        Set<AnObject> deserializedSet = (Set<AnObject>) readObject(bytes);

        if (deserializedSet.size() != setOfAnObjects.size()) {
            fail();
        }

        for (AnObject a : deserializedSet) {
            if (elements.remove(a.i) == null) {
                fail("Failed: " + a.i + " not found?");
            }
        }
        if (elements.size() > 0) {
            fail();
        }

        Set<AnObjectWithEquals> setOfAnObjectsWE = SetImpl.EMPTY;
        for (int i = 0; i < 100; i++) {
            AnObjectWithEquals e = new AnObjectWithEquals(i);
            setOfAnObjectsWE = setOfAnObjectsWE.add(e);
        }
        testSerializability(setOfAnObjectsWE);
    }

    @SuppressWarnings("unchecked")
    private <T extends Serializable> void testSerializability(T toTest) {
        byte[] bytes = writeObject(toTest);
        assertNotNull(bytes);

        T t = (T) readObject(bytes);
        assertNotNull(t);
        assertEquals(toTest, t);
    }

    private static byte[] writeObject(Serializable s) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(s);
            out.flush();
            byte[] bytes = bos.toByteArray();
            return bytes;
        } catch (IOException ex) {
            // System.err.println(ex);
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return null;
    }

    private static Object readObject(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            Object o = in.readObject();
            return o;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return null;
    }
}
