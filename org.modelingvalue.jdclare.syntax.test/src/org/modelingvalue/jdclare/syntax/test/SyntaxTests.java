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

package org.modelingvalue.jdclare.syntax.test;

import static org.junit.Assert.*;
import static org.modelingvalue.jdclare.DClare.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.Test;
import org.modelingvalue.collections.List;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DUniverse;
import org.modelingvalue.jdclare.syntax.Text;
import org.modelingvalue.jdclare.syntax.meta.GrammarClass;
import org.modelingvalue.jdclare.syntax.test.MySyntax.Unit;
import org.modelingvalue.transactions.State;

public class SyntaxTests {

    private static final int     MANY_TIMES  = 100;
    private static final boolean DUMP        = Boolean.getBoolean("DUMP");
    private static final Clock   FIXED_CLOCK = Clock.fixed(Instant.EPOCH, ZoneId.systemDefault());

    @Test
    public void manySyntax() {
        State prev = null;
        for (int i = 0; i < MANY_TIMES; i++) {
            State next = doit();
            next.run(() -> {
                test(next);
            });
            if (prev != null && !prev.equals(next)) {
                String diff = prev.diffString(next);
                assertEquals("Diff: ", "", diff);
            }
            prev = next;
        }
    }

    private State doit() {
        DClare<TextUniverse> dClare = of(TextUniverse.class, FIXED_CLOCK);
        dClare.start();
        dClare.put("change0", () -> {
            DClare.set(dClare.universe().text(), Text::string, STRING);
        });
        dClare.put("change1", () -> {
            DClare.set(dClare.universe().text(), Text::string, (s, e) -> s.replace("low", e), "lowest");
        });
        dClare.put("change2", () -> {
            DClare.set(dClare.universe().text(), Text::string, (s, e) -> s.replace("aaa", e), "bbb");
        });
        dClare.stop();
        return dClare.waitForEnd();
    }

    private static String STRING = List.of(//
            "package jdclare.test;", //
            "class Upper {", //
            "   Number low = 1000;", //
            "   String name = \"upper\";", //
            "   Lower aaa;", //
            "   Upper me = aaa.bbb;", //
            "}", //
            "class Lower {", //
            "   Number high = 10;", //
            "   String name = \"lower\";", //
            "   Upper bbb;", //
            "   Lower me = bbb.aaa;", //
            "}"//
    ).reduce("", (a, b) -> a.length() == 0 || b.length() == 0 ? a + b : a + "\n" + b);

    @Test
    public void oneSyntax() {
        State result = doit();
        result.run(() -> {
            if (DUMP) {
                System.err.println("******************************Begin Syntax************************************");
                result.getObjects(GrammarClass.class).filter(s -> s.jClass() == MySyntax.class).forEach(u -> u.dDump(System.err));
                System.err.println("******************************End Syntax**************************************");
                System.err.println("******************************Begin Text**************************************");
                result.getObjects(Text.class).forEach(u -> u.dDump(System.err));
                System.err.println("******************************End Text****************************************");
                System.err.println("******************************Begin Root**********************************");
                result.getObjects(Unit.class).forEach(u -> u.dDump(System.err));
                System.err.println("******************************End Root************************************");
                System.err.println("******************************Begin Problems**********************************");
                result.getObjects(DUniverse.class).forEach(u -> u.dAllProblems().forEach(p -> System.err.println(p)));
                System.err.println("******************************End Problems************************************");
            }
            test(result);
        });
    }

    private void test(State result) {
        // assertEquals("No Transaction:", Set.of(), result.getObjects(DObject.class).filter(o -> DClare.TRANSACTION.get(o) == null).toSet());
        assertTrue("No Root", result.getObjects(TextUniverse.class).allMatch(t -> t.text().root() != null));
        assertTrue("Problems", result.getObjects(TextUniverse.class).allMatch(t -> t.dAllProblems().isEmpty()));
    }

}
