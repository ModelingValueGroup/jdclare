package org.modelingvalue.jdclare.swing.examples.newton;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.awt.event.KeyEvent;

import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct2;
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

    @Property
    default DPoint acceleration() {
        return pre(this, Ball::velocity).mult(-billiard().rollingResistance());
    }

    @Property
    DPoint solPosition(); // solitary position

    @Property
    DPoint solVelocity(); // solitary velocity

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
        }
    }

    @Rule
    default void setNonDraggingVelocityAndPosition() {
        if (!dragging() && !canvas().deviceInput().pressedKeys().contains(KeyEvent.VK_ESCAPE)) {
            set(this, DShape::position, solPosition().plus(billiardPositionDelta()).plus(otherBallsPositionDelta()));
            set(this, Ball::velocity, solVelocity().plus(billiardVelocityDelta()).plus(otherBallsVelocityDelta()));
        }
    }

    // While dragging

    @Rule
    default void setDraggingVelocity() {
        if (dragging()) {
            double passTime = dUniverse().clock().passSeconds();
            DPoint velocity = position().minus(pre(this, DShape::position)).div(passTime);
            DPoint movingAvarage = pre(this, Ball::velocity).plus(velocity).div(2.0);
            set(this, Ball::velocity, movingAvarage);
        }
    }

    // Billiard

    default Billiard billiard() {
        return (Billiard) canvas();
    }

    @Property
    DPoint billiardPositionDelta();

    @Property
    DPoint billiardVelocityDelta();

    @Rule
    default void bounceToFrame() {
        Billiard billiard = billiard();
        DPoint min = billiard.minimum();
        DPoint max = billiard.maximum();
        DPoint solPosition = solPosition();
        DPoint solVelocity = solVelocity();
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
        set(this, Ball::billiardPositionDelta, positionDelta);
        set(this, Ball::billiardVelocityDelta, velocityDelta);
    }

    // Others Balls

    @Property(containment)
    default Set<Pair> pairs() {
        return canvas().shapes().filter(Ball.class).filter(b -> !equals(b)).map(b -> dclare(Pair.class, this, b)).toSet();
    }

    interface Pair extends DStruct2<Ball, Ball>, DObject {
        @Property(key = 0)
        Ball a();

        @Property(key = 1)
        Ball b();

        @Property
        default DPoint positionDelta() {
            return DPoint.NULL;
        }

        @Property
        default DPoint velocityDelta() {
            return DPoint.NULL;
        }
    }

    @Property
    default DPoint otherBallsPositionDelta() {
        return pairs().map(Pair::positionDelta).reduce((a, b) -> a.plus(b)).orElse(DPoint.NULL);
    }

    @Property
    default DPoint otherBallsVelocityDelta() {
        return pairs().map(Pair::velocityDelta).reduce((a, b) -> a.plus(b)).orElse(DPoint.NULL);
    }

    // Control

    @Rule
    default void setStill() {
        InputDeviceData di = canvas().deviceInput();
        if (di.pressedKeys().contains(KeyEvent.VK_ESCAPE)) {
            set(this, Ball::velocity, DPoint.NULL);
        }
    }

}
