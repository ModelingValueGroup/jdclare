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

import java.awt.event.KeyEvent;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.Default;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;
import org.modelingvalue.jdclare.swing.InputDeviceData;
import org.modelingvalue.jdclare.swing.draw2d.DCircle;
import org.modelingvalue.jdclare.swing.draw2d.DPoint;
import org.modelingvalue.jdclare.swing.draw2d.DShape;

public interface Ball extends DCircle {

    @Default
    @Property
    default DPoint velocity() {
        return DPoint.NULL;
    }

    @Override
    default int radius() {
        return (int) table().ballRadius();
    }

    @Property
    default DPoint acceleration() {
        return pre(this, Ball::velocity).mult(-table().rollingResistance());
    }

    @Default
    @Property
    default DPoint solPosition() {
        return DPoint.NULL;
    }

    @Default
    @Property
    default DPoint solVelocity() {
        return DPoint.NULL;
    }

    @Rule
    default void setSolitaryVelocityAndPosition() {
        DPoint preVelocity = pre(this, Ball::velocity);
        DPoint acceleration = acceleration();
        if (!preVelocity.equals(DPoint.NULL) || !acceleration.equals(DPoint.NULL)) {
            double passTime = dUniverse().clock().passSeconds();
            DPoint preMovement = preVelocity.mult(passTime);
            DPoint movement = acceleration.mult(Math.pow(passTime, 2.0)).div(0.5);
            DPoint position = pre(this, DShape::position).plus(preMovement).plus(movement);
            DPoint velocity = preVelocity.plus(acceleration.mult(passTime));
            set(this, Ball::solPosition, position);
            set(this, Ball::solVelocity, velocity);
        } else {
            set(this, Ball::solVelocity, DPoint.NULL);
        }
    }

    @Rule
    default void setNonDraggingVelocityAndPosition() {
        if (!dragging() && !canvas().deviceInput().pressedKeys().contains(KeyEvent.VK_ESCAPE)) {
            set(this, DShape::position, solPosition().plus(cushionsPositionDelta()));
            DPoint collVelocity = collisionVelocity();
            DPoint velocity = collVelocity != null ? collVelocity : solVelocity();
            set(this, Ball::velocity, velocity.plus(cushionsVelocityDelta()));
        }
    }

    // While dragging

    @Rule
    default void setDraggingVelocity() {
        if (dragging()) {
            double passTime = dUniverse().clock().passSeconds();
            DPoint velocity = position().minus(pre(this, DShape::position)).div(passTime);
            DPoint movingAvarage = pre(this, Ball::velocity).mult(2.0).plus(velocity).div(3.0);
            set(this, Ball::velocity, movingAvarage);
        }
    }

    // Others Balls

    @Property
    default Set<Ball> otherBalls() {
        List<DShape> shapes = canvas().shapes();
        int next = shapes.firstIndexOf(this) + 1;
        return next < shapes.size() ? shapes.sublist(next, shapes.size()).filter(Ball.class).toSet() : Set.of();
    }

    @Property(optional)
    default DPoint collisionVelocity() {
        BallPair c = table().collision();
        return c == null ? null : equals(c.a()) ? c.aVelocity() : equals(c.b()) ? c.bVelocity() : null;
    }

    // Billiard

    default Table table() {
        return (Table) canvas();
    }

    @Property
    DPoint cushionsPositionDelta();

    @Property
    DPoint cushionsVelocityDelta();

    @Rule
    default void bounceToCushions() {
        Table table = table();
        DPoint min = table.cushionMinimum();
        DPoint max = table.cushionMaximum();
        DPoint solPosition = solPosition();
        DPoint solVelocity = solVelocity().mult(1.0 - table.cushionBouncingResistance());
        DPoint positionDelta = DPoint.NULL;
        DPoint velocityDelta = DPoint.NULL;
        if (solPosition.x() < min.x()) {
            positionDelta = positionDelta.plus(dclare(DPoint.class, 2.0 * (min.x() - solPosition.x()), 0.0));
            velocityDelta = velocityDelta.plus(dclare(DPoint.class, -2.0 * solVelocity.x(), 0.0));
        } else if (solPosition.x() > max.x()) {
            positionDelta = positionDelta.plus(dclare(DPoint.class, 2.0 * (max.x() - solPosition.x()), 0.0));
            velocityDelta = velocityDelta.plus(dclare(DPoint.class, -2.0 * solVelocity.x(), 0.0));
        }
        if (solPosition.y() < min.y()) {
            positionDelta = positionDelta.plus(dclare(DPoint.class, 0.0, 2.0 * (min.y() - solPosition.y())));
            velocityDelta = velocityDelta.plus(dclare(DPoint.class, 0.0, -2.0 * solVelocity.y()));
        } else if (solPosition.y() > max.y()) {
            positionDelta = positionDelta.plus(dclare(DPoint.class, 0.0, 2.0 * (max.y() - solPosition.y())));
            velocityDelta = velocityDelta.plus(dclare(DPoint.class, 0.0, -2.0 * solVelocity.y()));
        }
        set(this, Ball::cushionsPositionDelta, positionDelta);
        set(this, Ball::cushionsVelocityDelta, velocityDelta);
    }

    // Control

    @Rule
    default void freeze() {
        InputDeviceData di = table().deviceInput();
        if (di.pressedKeys().contains(KeyEvent.VK_ESCAPE)) {
            set(this, Ball::velocity, DPoint.NULL);
        }
    }

}
