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

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DUniverse;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;

public interface Orchestra extends DUniverse {

    @Property(containment)
    default Set<Instrument> instruments() {
        return Set.of(//
                dclare(Piano.class, "p", set(Piano::size, 12), rule(Piano::big, p -> p.size() > 24)), //
                dclare(Grand.class, "g"), //
                dclare(Mega.class, "m"));
    }

    interface Grand extends Piano {
        @Override
        @Property(constant)
        default int size() {
            return 36;
        }

        @Override
        default boolean big() {
            return size() > 24;
        }

    }

    interface Mega extends Piano {

        @Rule
        default void sizeRule() {
            set(this, Piano::size, 60);
        }

        @Rule
        default void bigRule() {
            set(this, Piano::big, size() > 24);
        }

    }

    @Override
    default void init() {
        DUniverse.super.init();
        dClare().addNative("pianoNative", callNativesOfClass(PianoKey.class));
    }
}
