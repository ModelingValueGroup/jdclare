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

import org.modelingvalue.jdclare.DUniverse;
import org.modelingvalue.jdclare.IOString;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;

public interface FibonacciUniverse extends DUniverse {

    static void main(String[] args) {
        runAndRead(FibonacciUniverse.class);
    }

    @Property
    int nr();

    @Property
    default double fibonacci() {
        return dStruct(Fibonacci.class, nr()).fibonaci();
    }

    @Override
    default IOString output() {
        return IOString.of("nr=" + nr() + " fibonacci=" + fibonacci() + System.lineSeparator() + "> ");
    }

    @Rule
    default void readInput() {
        String input = input().string();
        if (input.equals("stop")) {
            set(this, DUniverse::stop, true);
        } else if (!input.isEmpty()) {
            try {
                set(this, FibonacciUniverse::nr, Integer.parseInt(input));
            } catch (NumberFormatException nfe) {
                set(this, DUniverse::error, IOString.ofln("Only a <int> or 'stop' allowed"));
            }
        }
        set(this, DUniverse::input, IOString.of(""));
    }

}