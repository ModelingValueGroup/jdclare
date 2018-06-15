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

package org.modelingvalue.jdclare.swing.draw2d;

import static org.modelingvalue.jdclare.DClare.*;

import org.modelingvalue.jdclare.DStruct2;
import org.modelingvalue.jdclare.Property;

public interface DPoint extends DStruct2<Integer, Integer> {

    DPoint NULL = dclare(DPoint.class, 0, 0);

    @Property(key = 0)
    int x();

    @Property(key = 1)
    int y();

    default DPoint minus(DPoint minus) {
        return dclare(DPoint.class, x() - minus.x(), y() - minus.y());
    }

    default DPoint plus(DPoint plus) {
        return dclare(DPoint.class, x() + plus.x(), y() + plus.y());
    }

    @Property
    default double length() {
        return Math.sqrt(Math.pow(x(), 2.0d) + Math.pow(y(), 2.0d));
    }

    default DPoint mult(double d) {
        return dclare(DPoint.class, (int) (x() * d), (int) (y() * d));
    }

    default DPoint div(double d) {
        return dclare(DPoint.class, (int) (x() / d), (int) (y() / d));
    }

    default boolean hasEqualAngle(DPoint p) {
        return p.mult(100.0d / p.length()).minus(mult(100.0d / length())).length() < 2.0d;
    }

}
