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

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DClock;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;
import org.modelingvalue.jdclare.swing.draw2d.DCanvas;
import org.modelingvalue.jdclare.swing.draw2d.DPoint;

public interface Table extends DCanvas {

    @Property()
    default List<Ball> balls() {
        return shapes().filter(Ball.class).toList();
    }

    @Property(constant)
    default double gravity() {
        return 9.80665;
    }

    @Property(constant)
    default double rollingResistance() {
        return 0.8;
    }

    @Property(constant)
    default double friction() {
        return gravity() * rollingResistance();
    }

    @Property
    default double passSeconds() {
        if (balls().anyMatch(b -> !pre(b, Ball::velocity).equals(DPoint.NULL))) {
            return dUniverse().clock().passSeconds();
        } else {
            return 1.0;
        }
    }

    @Property
    default double velocityDelta() {
        return friction() * passSeconds();
    }

    @Property
    default double positionDelta() {
        return 0.5 * velocityDelta() * passSeconds();
    }

    @Property(constant)
    default double cushionBouncingResistance() {
        return 0.2;
    }

    @Property(constant)
    default double ballsBouncingResistance() {
        return 0.2;
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
    default Set<CollisionPair> collisionPairs() {
        return balls().flatMap(b -> b.collisionPairs()).toSet();
    }

    @Property(optional)
    default CollisionPair collision() {
        return collisionPairs().filter(c -> c.distance() < 0.1).//
                sorted((a, b) -> Double.compare(a.distance(), b.distance())).findFirst().orElse(null);
    }

    @Rule
    default void setCollisionTime() {
        if (collision() == null) {
            collisionPairs().filter(c -> c.collisionTime() > 0.0).//
                    sorted((a, b) -> Double.compare(a.collisionTime(), b.collisionTime())).findFirst().ifPresent(c -> {
                        DClock clock = dUniverse().clock();
                        Instant cTime = pre(clock, DClock::time).plus((long) (c.collisionTime() * DClock.BILLION), ChronoUnit.NANOS);
                        if (cTime.isBefore(clock.time())) {
                            set(clock, DClock::time, cTime);
                        }
                    });
        }
    }

}
