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

package org.modelingvalue.jdclare.swing.examples.newton;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DClock;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;
import org.modelingvalue.jdclare.swing.draw2d.DCanvas;
import org.modelingvalue.jdclare.swing.draw2d.DPoint;

public interface Table extends DCanvas {

    @Property(constant)
    default double rollingResistance() {
        return 0.2;
    }

    @Property(constant)
    default double cushionBouncingResistance() {
        return 0.1;
    }

    @Property(constant)
    default double ballsBouncingResistance() {
        return 0.1;
    }

    @Property(constant)
    default double ballRadius() {
        return 20.0;
    }

    @Property(constant)
    default double ballRadiusPow() {
        return Math.pow(ballRadius(), 2.0);
    }

    @Property(constant)
    default DPoint cushionMinimum() {
        return dclare(DPoint.class, ballRadius(), ballRadius());
    }

    @Property
    default DPoint cushionMaximum() {
        return size().toPoint().minus(cushionMinimum());
    }

    @Property(containment)
    default Set<BallPair> pairs() {
        return shapes().filter(Ball.class).flatMap(a -> a.otherBalls().map(b -> dclare(BallPair.class, a, b))).toSet();
    }

    @Property(optional)
    BallPair collision();

    @Rule
    default void setCollisionTimeAndPair() {
        Optional<BallPair> first = pairs().sorted((a, b) -> Double.compare(a.collisionTime(), b.collisionTime())).findFirst();
        first.ifPresentOrElse(f -> {
            double ct = f.collisionTime();
            DClock clock = dUniverse().clock();
            double pass = clock.passSeconds();
            if (ct < pass) {
                Instant pt = pre(clock, DClock::time);
                set(this, Table::collision, f);
                set(clock, DClock::time, pt.plus((long) (ct * DClock.BILLION), ChronoUnit.NANOS));
            } else if (ct > pass + 0.00001) {
                set(this, Table::collision, null);
            }
        }, () -> {
            set(this, Table::collision, null);
        });
    }

}
