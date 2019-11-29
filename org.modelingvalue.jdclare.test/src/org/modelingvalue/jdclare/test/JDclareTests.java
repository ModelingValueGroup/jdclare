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

package org.modelingvalue.jdclare.test;

import static org.junit.Assert.*;
import static org.modelingvalue.jdclare.DClare.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.dclare.Direction;
import org.modelingvalue.dclare.OutOfScopeException;
import org.modelingvalue.dclare.State;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DNamed;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct;
import org.modelingvalue.jdclare.DUniverse;
import org.modelingvalue.jdclare.test.PrioUniverse.Prio;

public class JDclareTests {

    private static final int     MANY_TIMES  = 100;
    private static final boolean DUMP        = Boolean.getBoolean("DUMP");
    private static final Clock   FIXED_CLOCK = Clock.fixed(Instant.EPOCH, ZoneId.systemDefault());

    @Test
    public void manyUniverse() {
        for (int i = 0; i < MANY_TIMES; i++) {
            DClare<DUniverse> dClare = of(DUniverse.class);
            State result = dClare.run();
            result.run(() -> check(result));
        }
    }

    @Test
    public void universe() {
        DClare<DUniverse> dClare = of(DUniverse.class);
        State result = dClare.run();
        if (DUMP) {
            System.err.println("***************************** Begin DUniverse ***********************************");
            System.err.println(result.asString());
            System.err.println("***************************** End DUniverse *************************************");
        }
        result.run(() -> {
            if (DUMP) {
                System.err.println("******************************Begin Universe*******************************");
                result.getObjects(DUniverse.class).findAny().ifPresent(u -> u.dDump(System.err));
                System.err.println("******************************End Universe*********************************");
            }
            check(result);
        });
    }

    @Test
    public void manyOrchestra() {
        State prev = null;
        for (int i = 0; i < MANY_TIMES; i++) {
            DClare<Orchestra> dClare = of(Orchestra.class, FIXED_CLOCK);
            State next = dClare.run();
            next.run(() -> {
                check(next);
                checkOrchestra(next);
            });
            if (prev != null && !prev.equals(next)) {
                String diff = prev.diffString(next);
                assertEquals("Diff: ", "", diff);
            }
            prev = next;
        }
    }

    @Test
    public void orchestra() {
        DClare<Orchestra> dClare = of(Orchestra.class);
        State result = dClare.run();
        result.run(() -> {
            check(result);
            checkOrchestra(result);
            if (DUMP) {
                System.err.println("******************************Begin Orchestra*******************************");
                result.getObjects(Piano.class).forEach(u -> u.dDump(System.err));
                System.err.println("******************************End Orchestra*********************************");
            }
        });
    }

    private void checkOrchestra(State result) {
        Set<Piano> pianos = result.getObjects(Piano.class).toSet();
        assertEquals("Unexpected Pianos: " + pianos, 3, pianos.size());
        pianos.forEach(p -> {
            assertTrue("No Keys", p.size() > 0);
            assertEquals(p.size(), p.keys().size());
            assertEquals(p.big(), p.size() > 24);
            assertEquals(p.size() * 5 / 12, p.keys().filter(k -> k.black()).size());
            p.keys().forEach(k -> {
                KeyNative n = dNative(k);
                assertTrue("No KeyNative", n != null);
                assertEquals("Init not 1:", 1, n.inits());
                assertEquals("Black Native not equal:", k.black(), n.black());
            });

        });
    }

    private void check(State result) {
        assertEquals("No Parent:", Set.of(), result.getObjects(DObject.class).filter(o -> !(o instanceof DUniverse) && o.dParent() == null).toSet());
        assertEquals("No dStructType:", Set.of(), result.getObjects(DStruct.class).filter(o -> o.dStructClass() == null).toSet());
        assertEquals("No dClass:", Set.of(), result.getObjects(DObject.class).filter(o -> o.dClass() == null).toSet());
        assertEquals("No name:", Set.of(), result.getObjects(DNamed.class).filter(o -> o.name() == null).toSet());
        assertEquals("Problems:", Set.of(), result.getObjects(DUniverse.class).flatMap(t -> t.dAllProblems()).toSet());
        assertEquals("ToDo:", Set.of(), result.getObjects(DObject.class).map(o -> Pair.of(o, Collection.concat( //
                Collection.concat(Direction.forward.depth.get(o), Direction.backward.depth.get(o), Direction.scheduled.depth.get(o)), //
                Collection.concat(Direction.forward.preDepth.get(o), Direction.backward.preDepth.get(o), Direction.scheduled.preDepth.get(o)), //
                Collection.concat(Direction.forward.postDepth.get(o), Direction.backward.postDepth.get(o), Direction.scheduled.postDepth.get(o))).//
                toSet())).filter(p -> !p.b().isEmpty()).toSet());
    }

    @Test
    public void testReparents() {
        DClare<Reparents> dClare = of(Reparents.class);
        State result = dClare.run();
        result.run(() -> {
            check(result);
        });
        if (DUMP) {
            result.run(() -> {
                Optional<Reparents> r = result.getObjects(Reparents.class).findAny();
                r.ifPresent(u -> System.err.println(u.tree()));
                System.err.println("******************************Begin Reparents************************************");
                r.ifPresent(u -> u.tree().forEach(v -> v.dDump(System.err)));
                System.err.println("******************************End Reparents**************************************");
            });
        }
    }

    @Test
    public void testPriorities() {
        DClare<PrioUniverse> dClare = of(PrioUniverse.class);
        State result = dClare.run();
        result.run(() -> {
            check(result);
            checkPriorities(result);
        });
        if (DUMP) {
            result.run(() -> {
                System.err.println("******************************Begin Priorities************************************");
                for (Prio prio : result.getObjects(Prio.class)) {
                    prio.dDump(System.err);
                }
                System.err.println("******************************End Priorities**************************************");
            });
        }
    }

    @Test
    public void manyPriorities() {
        State prev = null;
        for (int i = 0; i < MANY_TIMES; i++) {
            DClare<PrioUniverse> dClare = of(PrioUniverse.class, FIXED_CLOCK);
            State next = dClare.run();
            next.run(() -> {
                check(next);
                checkPriorities(next);
            });
            if (prev != null && !prev.equals(next)) {
                String diff = prev.diffString(next);
                assertEquals("Diff: ", "", diff);
            }
            prev = next;
        }
    }

    private void checkPriorities(State result) {
        Set<Prio> prios = result.getObjects(Prio.class).toSet();
        assertEquals("Unexpected Priorities: " + prios, 3, prios.size());
        prios.forEach(p -> {
            String target = p.dClass().name();
            target = target.substring(target.length() - 1);
            assertEquals(target, p.x());
            assertEquals(target, p.y());
        });
    }

    @Test
    public void testScrum() {
        DClare<Scrum> dClare = of(Scrum.class);
        dClare.start();
        dClare.put("company", () -> {
            dClare.universe().initTest(dClare);
        });
        dClare.stop();
        State result = dClare.waitForEnd();
        result.run(() -> {
            assertFalse("Wim is has no Problems!", result.getObjects(Team.class).flatMap(t -> t.problems()).isEmpty());
            if (DUMP) {
                result.getObjects(Team.class).forEach(v -> v.dDump(System.err));
            }
        });
    }

    @Test
    public void testScopeProblem() {
        try {
            DClare<Scrum> dClare = of(Scrum.class);
            dClare.start();
            dClare.put("company", () -> {
                dClare.universe().initScopeProblem(dClare);
            });
            dClare.stop();
            dClare.waitForEnd();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, OutOfScopeException.class, java.util.regex.Pattern.quote("The value 'Set[Pieter Puk]' of 'developers' of object 'DClare' is out of scope 'Set[]'"));
        }
    }

    private Throwable getCause(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    private void assertThrowable(Throwable cause, Class<? extends Throwable> throwable, String regex) {
        assertEquals(throwable, cause.getClass());
        assertTrue(cause.getMessage() + " != " + regex, cause.getMessage().matches(regex));
    }

}
