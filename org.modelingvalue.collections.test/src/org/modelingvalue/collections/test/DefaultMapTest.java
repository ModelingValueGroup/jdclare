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

import org.junit.Test;
import org.modelingvalue.collections.DefaultMap;

public class DefaultMapTest {

    private static String reverse(String a) {
        return new StringBuilder(a).reverse().toString();
    }

    private static final String aap  = "aap";
    private static final String noot = "noot";
    private static final String mies = "mies";
    private static final String zus  = "zus";
    private static final String jet  = "jet";
    private static final String teun = "teun";

    @Test
    public void test() throws Exception {
        DefaultMap<String, String> dm = DefaultMap.<String, String> of((a) -> reverse(a)).//
                put(aap, aap).//
                put(noot, noot).//
                put(mies, mies).//
                put(zus, zus).//
                put(jet, jet);

        assertEquals("nuet", dm.get(teun));
        assertEquals(aap, dm.get(aap));

        dm = dm.removeKey(aap);
        assertEquals("paa", dm.get(aap));

        dm = dm.put(aap, aap);
        assertEquals(aap, dm.get(aap));

        dm = dm.add(noot, noot, (a, b) -> a + b);
        assertEquals("nootnoot", dm.get(noot));

        dm = dm.removeKey(noot);
        dm = dm.put(noot, noot);

        dm = dm.remove(noot, noot, (a, b) -> null);
        assertEquals(null, dm.get(noot));

        dm = dm.put(noot, noot);
        assertEquals(noot, dm.get(noot));

        dm = dm.remove(noot, noot, (a, b) -> a + b);
        assertEquals("nootnoot", dm.get(noot));

        dm = dm.put(noot, "toon");
        assertEquals("toon", dm.get(noot));

        dm = dm.add(noot, noot, (a, b) -> a + b);
        assertEquals(noot, dm.get(noot));
    }
}
