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
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.ContextThread;
import org.modelingvalue.collections.util.ContextThread.ContextPool;
import org.modelingvalue.transactions.Mutable;
import org.modelingvalue.transactions.Observed;
import org.modelingvalue.transactions.Observer;
import org.modelingvalue.transactions.Setable;
import org.modelingvalue.transactions.State;
import org.modelingvalue.transactions.UniverseTransaction;

public class TransactionsTests {

    static final ContextPool THE_POOL = ContextThread.createPool();

    @Test
    public void source2target() throws Exception {
        Observed<DUniverse, DObject> child = Observed.of("child", null, true);
        Observed<DObject, Integer> source = Observed.of("source", 0);
        Setable<DObject, Integer> target = Setable.of("target", 0);
        DUniverse universe = DUniverse.of("universe", DClass.of("Universe"));
        DClass dClass = DClass.of("Object", Observer.of("observer", o -> target.set(o, source.get(o))));
        DObject object = DObject.of("object", dClass);
        UniverseTransaction universeTransaction = UniverseTransaction.of(universe, THE_POOL);
        universeTransaction.put("step1", () -> child.set(universe, object));
        universeTransaction.put("step2", () -> source.set(object, 10));
        universeTransaction.stop();
        State result = universeTransaction.waitForEnd();
        Assert.assertEquals(10, (int) result.get(object, target));
        System.err.println("********************************************************************");
        System.err.println(result.asString());
        System.err.println("********************************************************************");
    }

    @Test
    public void cycle1second() throws Exception {
        Observed<DUniverse, Long> currentTime = Observed.of("time", System.currentTimeMillis());
        long begin = System.currentTimeMillis();
        Observed<DUniverse, Set<Mutable>> children = Observed.of("children", Set.of(), true);
        DUniverse universe = DUniverse.of("universe", DClass.of("Universe"));
        UniverseTransaction universeTransaction = UniverseTransaction.of(universe, THE_POOL, 100, r -> currentTime.set(universe, System.currentTimeMillis()));
        DClass dClass = DClass.of("Object", Observer.of("observer", o -> {
            long time = currentTime.get(universe);
            if (time - begin > 1000) {
                UniverseTransaction.STOPPED.set(universe, true);
            }
        }));
        universeTransaction.put("step1", () -> {
            for (int io = 0; io < 8; io++) {
                children.set(universe, Set::add, DObject.of(io, dClass));
            }
        });
        State result = universeTransaction.waitForEnd();
        System.err.println("********************************************************************");
        System.err.println(result.asString());
        System.err.println("********************************************************************");
        long end = result.get(universe, currentTime);
        long duration = (end - begin) / 1000;
        System.err.println(duration + " s");
        System.err.println("********************************************************************");
    }

    @Test
    public void derivationChain() throws Exception {
        Observed<DUniverse, Set<Mutable>> children = Observed.of("children", Set.of(), true);
        Observed<DObject, Integer> number = Observed.of("number", 0);
        Observed<DObject, Integer> total = Observed.of("total", 0);
        int length = 30;
        DUniverse universe = DUniverse.of("universe", DClass.of("Universe"));
        UniverseTransaction universeTransaction = UniverseTransaction.of(universe, THE_POOL);
        DClass dClass = DClass.of("Object", Observer.of("observer", o -> {
            int i = (int) o.id();
            total.set(o, number.get(o) + (i > 0 ? total.get(DObject.of(i - 1, o.dClass())) : 0));
        }));
        universeTransaction.put("step1", () -> {
            for (int io = 0; io < length; io++) {
                children.set(universe, Set::add, DObject.of(io, dClass));
            }
        });
        universeTransaction.put("step2", () -> {
            for (int io = 0; io < length; io++) {
                number.set(DObject.of(io, dClass), 1);
            }
        });
        universeTransaction.stop();
        State result = universeTransaction.waitForEnd();
        System.err.println("********************************************************************");
        System.err.println(result.asString());
        System.err.println("********************************************************************");
        Assert.assertEquals(length, (int) result.get(DObject.of(length - 1, dClass), total));
    }

    @Test
    public void moveAndRemove() throws Exception {
        Observed<DObject, Set<DObject>> children = Observed.of("children", Set.of(), true);
        Observed<DObject, String> name = Observed.of("name", null);
        Observed<DObject, String> qualifiedName = Observed.of("qualifiedName", null);
        DClass dClass = DClass.of("Object", //
                Observer.of("qualifiedName", o -> qualifiedName.set(o, qualifiedName.get(o.dParent(DObject.class)) + "." + name.get(o))), //
                Observer.of("name", o -> name.set(o, (String) o.id())));
        DObject c1 = DObject.of("c1", dClass);
        DObject c2 = DObject.of("c2", dClass);
        DObject gc1 = DObject.of("gc1", dClass);
        DObject gc2 = DObject.of("gc2", dClass);
        DObject gc3 = DObject.of("gc3", dClass);
        DObject gc4 = DObject.of("gc4", dClass);
        DObject ggc1 = DObject.of("ggc1", dClass);
        DObject ggc2 = DObject.of("ggc2", dClass);
        DObject ggc3 = DObject.of("ggc3", dClass);
        DObject ggc4 = DObject.of("ggc4", dClass);
        DObject ggc5 = DObject.of("ggc5", dClass);
        DObject ggc6 = DObject.of("ggc6", dClass);
        DObject ggc7 = DObject.of("ggc7", dClass);
        DObject ggc8 = DObject.of("ggc8", dClass);
        DUniverse universe = DUniverse.of("universe", DClass.of("Universe"));
        UniverseTransaction universeTransaction = UniverseTransaction.of(universe, THE_POOL);
        universeTransaction.put("step1", () -> {
            qualifiedName.set(universe, "u");
            children.set(universe, Set.of(c1, c2));
            children.set(c1, Set.of(gc1, gc2));
            children.set(c2, Set.of(gc3, gc4));
            children.set(gc1, Set.of(ggc1, ggc2));
            children.set(gc2, Set.of(ggc3, ggc4));
            children.set(gc3, Set.of(ggc5, ggc6));
            children.set(gc4, Set.of(ggc7, ggc8));
        });
        universeTransaction.put("step2", () -> {
            children.set(c2, Set.of(gc1, gc2));
        });
        universeTransaction.stop();
        State result = universeTransaction.waitForEnd();
        System.err.println("********************************************************************");
        System.err.println(result.asString());
        System.err.println("********************************************************************");
        Assert.assertEquals(Set.of(), result.get(c1, children));
        Assert.assertEquals(null, result.get(ggc6, qualifiedName));
        Assert.assertEquals("u.c2.gc2.ggc3", result.get(ggc3, qualifiedName));
    }

}
