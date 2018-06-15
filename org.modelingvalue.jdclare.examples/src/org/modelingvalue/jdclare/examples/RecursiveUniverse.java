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

package org.modelingvalue.jdclare.examples;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.DUniverse;
import org.modelingvalue.jdclare.Default;
import org.modelingvalue.jdclare.IOString;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;

public interface RecursiveUniverse extends DUniverse {

    static void main(String[] args) {
        runAndRead(RecursiveUniverse.class);
    }

    @Default
    @Property
    default int depth() {
        return 3;
    }

    @Property({containment, constant})
    default Element element() {
        return dclare(Element.class, 0);
    }

    interface Element extends DObject, DStruct1<Integer> {
        @Property(key = 0)
        int nr();

        @Property({containment, optional})
        default Element child() {
            return nr() < dAncestor(RecursiveUniverse.class).depth() ? dclare(Element.class, nr() + 1) : null;
        }
    }

    @Override
    default IOString output() {
        return IOString.of(element().dString());
    }

    @Rule
    default void readInput() {
        String input = input().string().replaceAll("\\s+", "");
        if (input.equals("stop")) {
            set(this, DUniverse::stop, true);
        } else if (!input.isEmpty()) {
            try {
                set(this, RecursiveUniverse::depth, Integer.parseInt(input));
            } catch (NumberFormatException nfe) {
                set(this, DUniverse::error, IOString.ofln("Only integer or 'stop' allowed"));
            }
        }
        set(this, DUniverse::input, IOString.of(""));
    }

}
