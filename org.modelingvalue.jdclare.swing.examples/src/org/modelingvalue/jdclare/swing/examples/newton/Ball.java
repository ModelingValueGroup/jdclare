package org.modelingvalue.jdclare.swing.examples.newton;

import static org.modelingvalue.jdclare.DClare.*;

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

    @Property
    default double mass() { // amount of dots
        return Math.PI * Math.pow(radius(), 3.0) * 4.0 / 3.0;
    }

    @Default
    @Property
    default DPoint velocity() { // dots per second
        return DPoint.NULL;
    }

    @Property
    default double frictionForce() {
        return frame().rollingResistanceCoefficient() * mass();
    }

    @Property
    default DPoint force() { // Newton
        DPoint preVelocity = pre(this, Ball::velocity);
        return !preVelocity.equals(DPoint.NULL) ? preVelocity.normal().mult(-frictionForce()) : DPoint.NULL;
    }

    @Property
    default DPoint acceleration() { // dots per sqrt second
        double mass = mass();
        return mass > 0.0 ? force().div(mass) : DPoint.NULL;
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
            set(this, DShape::position, solPosition().plus(positionDelta()).plus(//
                    pairs().map(Pair::positionDelta).reduce((a, b) -> a.plus(b)).orElse(DPoint.NULL)));
            set(this, Ball::velocity, solVelocity().dot(velocityDelta()).dot(//
                    pairs().map(Pair::velocityDelta).reduce((a, b) -> a.dot(b)).orElse(DPoint.ONE)));
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

    // Frame

    default Billiard frame() {
        return (Billiard) canvas();
    }

    @Property
    default DPoint minimum() {
        double radius = radius();
        return dclare(DPoint.class, radius, radius);
    }

    @Property
    default DPoint maximum() {
        return frame().size().toPoint().minus(minimum());
    }

    @Property
    DPoint positionDelta();

    @Property
    DPoint velocityDelta();

    @Rule
    default void bounceToFrame() {
        DPoint min = minimum();
        DPoint max = maximum();
        DPoint solPosition = solPosition();
        DPoint positionDelta = DPoint.NULL;
        DPoint velocityDelta = DPoint.ONE;
        if (solPosition.x() < min.x()) {
            positionDelta = positionDelta.plus(dclare(DPoint.class, 2.0 * (min.x() - solPosition.x()), 0.0));
            velocityDelta = velocityDelta.dot(dclare(DPoint.class, -1.0, 1.0));
        } else if (solPosition.x() > max.x()) {
            positionDelta = positionDelta.plus(dclare(DPoint.class, 2.0 * (max.x() - solPosition.x()), 0.0));
            velocityDelta = velocityDelta.dot(dclare(DPoint.class, -1.0, 1.0));
        }
        if (solPosition.y() < min.y()) {
            positionDelta = positionDelta.plus(dclare(DPoint.class, 0.0, 2.0 * (min.y() - solPosition.y())));
            velocityDelta = velocityDelta.dot(dclare(DPoint.class, 1.0, -1.0));
        } else if (solPosition.y() > max.y()) {
            positionDelta = positionDelta.plus(dclare(DPoint.class, 0.0, 2.0 * (max.y() - solPosition.y())));
            velocityDelta = velocityDelta.dot(dclare(DPoint.class, 1.0, -1.0));
        }
        set(this, Ball::positionDelta, positionDelta);
        set(this, Ball::velocityDelta, velocityDelta);
    }

    // Others Balls

    @Property
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
            return DPoint.ONE;
        }
    }

    // Control

    @Override
    default int radius() {
        if (selected()) {
            Set<Integer> pressed = canvas().deviceInput().pressedKeys();
            int preRadius = pre(this, DCircle::radius);
            return pressed.contains(KeyEvent.VK_SHIFT) && pressed.contains(KeyEvent.VK_EQUALS) ? preRadius + 10 : //
                    !pressed.contains(KeyEvent.VK_SHIFT) && pressed.contains(KeyEvent.VK_MINUS) && preRadius > 10 ? preRadius - 10 : //
                            preRadius;
        } else {
            return radius();
        }
    }

    @Rule
    default void setStill() {
        InputDeviceData di = canvas().deviceInput();
        if (di.pressedKeys().contains(KeyEvent.VK_ESCAPE)) {
            set(this, Ball::velocity, DPoint.NULL);
        }
    }

}
