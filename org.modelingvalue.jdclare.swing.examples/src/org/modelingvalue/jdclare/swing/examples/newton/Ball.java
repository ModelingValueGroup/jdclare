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
    default DPoint solVelocity() {
        return DPoint.NULL;
    }

    @Default
    @Property
    default DPoint solPosition() {
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
            set(this, Ball::position, solPosition());
            CollisionPair c = table().collision();
            DPoint velocity = c != null ? c.velocity(this) : solVelocity();
            set(this, Ball::velocity, velocity);
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

    // Collision Pairs

    @Property
    default Set<CollisionPair> collisionPairs() {
        List<DShape> shapes = canvas().shapes();
        int next = shapes.firstIndexOf(this) + 1;
        Set<CollisionPair> set = Set.of();
        if (next < shapes.size()) {
            set = set.addAll(shapes.sublist(next, shapes.size()).filter(Ball.class).map(b -> dclare(BallBallPair.class, this, b)));
        }
        return set.addAll(Set.of(dclare(BallCushionPair.class, this, false, false), //
                dclare(BallCushionPair.class, this, false, true), //
                dclare(BallCushionPair.class, this, true, false), //
                dclare(BallCushionPair.class, this, true, true)));
    }

    // Billiard

    default Table table() {
        return (Table) canvas();
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
