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

public interface DPoint extends DStruct2<Double, Double> {

    DPoint NULL = dclare(DPoint.class, 0.0, 0.0);

    DPoint ONE  = dclare(DPoint.class, 1.0, 1.0);

    @Property(key = 0)
    double x();

    @Property(key = 1)
    double y();

    default DPoint normal() {
        return div(length());
    }

    default DPoint minus(DPoint p) {
        return p.equals(NULL) ? this : dclare(DPoint.class, x() - p.x(), y() - p.y());
    }

    default DPoint plus(DPoint p) {
        return equals(NULL) ? p : p.equals(NULL) ? this : dclare(DPoint.class, x() + p.x(), y() + p.y());
    }

    @Property
    default double length() {
        return Math.sqrt(Math.pow(x(), 2.0) + Math.pow(y(), 2.0));
    }

    default double dot(DPoint p) {
        return x() * p.x() + y() * p.y();
    }

    default DPoint mult(DPoint p) {
        return equals(NULL) ? NULL : dclare(DPoint.class, x() * p.x(), y() * p.y());
    }

    default DPoint mult(double d) {
        return equals(NULL) ? NULL : dclare(DPoint.class, x() * d, y() * d);
    }

    default DPoint div(double d) {
        return equals(NULL) ? NULL : dclare(DPoint.class, x() / d, (y() / d));
    }

    default boolean hasEqualAngle(DPoint p) {
        return p.mult(100.0 / p.length()).minus(mult(100.0 / length())).length() < 2.0;
    }

}
