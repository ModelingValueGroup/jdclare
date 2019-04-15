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

package org.modelingvalue.transactions.test;

import org.junit.Assert;
import org.junit.Test;
import org.modelingvalue.collections.util.ContextThread;
import org.modelingvalue.collections.util.ContextThread.ContextPool;
import org.modelingvalue.transactions.Compound;
import org.modelingvalue.transactions.Observed;
import org.modelingvalue.transactions.Observer;
import org.modelingvalue.transactions.Root;
import org.modelingvalue.transactions.Rule;
import org.modelingvalue.transactions.State;

public class TransactionsTests {

    static final Observed<Root, Long> TIME_MILLIS = Observed.of("timeMillis", System.currentTimeMillis());
    static final ContextPool          THE_POOL    = ContextThread.createPool();

    @Test
    public void test0() throws Exception {
        Observed<String, Integer> A = Observed.of("A", 0);
        String obj = "o";
        Root root = Root.of("R", THE_POOL, 100);
        Rule rule = Rule.of("X", o -> {
            A.get(obj);
            A.set(obj, 10);
        });
        root.put("P", () -> Observer.of(rule, root).trigger());
        root.stop();
        State result = root.waitForEnd();
        System.err.println("********************************************************************");
        System.err.println(result.asString());
        System.err.println("********************************************************************");
    }

    @Test
    public void test1() throws Exception {
        long begin = System.currentTimeMillis();
        Root root = Root.of("R", THE_POOL, 100, r -> TIME_MILLIS.set(r, System.currentTimeMillis()));
        root.put("test1", () -> {
            for (int io = 0; io < 8; io++) {
                Compound o = Compound.of("O" + io, root);
                for (int il = 0; il < 8; il++) {
                    Observer.of(Rule.of("L" + il, x -> {
                        long time = TIME_MILLIS.get(root);
                        System.err.println("TIME: " + time);
                        if (time - begin > 1000) {
                            Root.STOPPED.set(root, true);
                        }
                    }), o).trigger();
                }

            }
        });
        State result = root.waitForEnd();
        System.err.println("********************************************************************");
        System.err.println(result.asString());
        System.err.println("********************************************************************");
        long end = result.get(root, TIME_MILLIS);
        long duration = (end - begin) / 1000;
        System.err.println(duration + " s");
        System.err.println("********************************************************************");
    }

    @Test
    public void test2() throws Exception {
        Observed<Compound, Integer> NR = Observed.of("nr", 0);
        Observed<Compound, Integer> TOT = Observed.of("tot", 0);
        int depth = 100;
        Root root = Root.of("R", THE_POOL, 100);
        Compound[] last = new Compound[1];
        root.put("test2", () -> {
            for (int io = 0; io < depth; io++) {
                Compound o = Compound.of("O" + io, root);
                Compound p = last[0];
                Observer.of(Rule.of("C" + io, x -> {
                    TOT.set(o, NR.get(o) + (p != null ? TOT.get(p) : 0));
                }), o).trigger();
                NR.set(o, 1);
                last[0] = o;
            }
        });
        root.stop();
        State result = root.waitForEnd();
        System.err.println("********************************************************************");
        System.err.println(result.asString());
        System.err.println("********************************************************************");
        Assert.assertEquals(depth, (int) result.get(last[0], TOT));
    }

}
