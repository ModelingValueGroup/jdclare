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
            DPoint min = minimum();
            DPoint max = maximum();
            if (position.x() < min.x()) {
                position = dclare(DPoint.class, min.x() + (min.x() - position.x()), position.y());
                velocity = dclare(DPoint.class, -velocity.x(), velocity.y());
            } else if (position.x() > max.x()) {
                position = dclare(DPoint.class, max.x() - (position.x() - max.x()), position.y());
                velocity = dclare(DPoint.class, -velocity.x(), velocity.y());
            }
            if (position.y() < min.y()) {
                position = dclare(DPoint.class, position.x(), min.y() + (min.y() - position.y()));
                velocity = dclare(DPoint.class, velocity.x(), -velocity.y());
            } else if (position.y() > max.y()) {
                position = dclare(DPoint.class, position.x(), max.y() - (position.y() - max.y()));
                velocity = dclare(DPoint.class, velocity.x(), -velocity.y());
            }
            set(this, Ball::solPosition, position);
            set(this, Ball::solVelocity, velocity);
        }
    }

    @Rule
    default void setNonDraggingVelocityAndPosition() {
        if (!dragging() && !canvas().deviceInput().pressedKeys().contains(KeyEvent.VK_ESCAPE)) {
            set(this, DShape::position, solPosition().plus(pairs().map(Pair::positionDelta).reduce((a, b) -> a.plus(b)).orElse(DPoint.NULL)));
            set(this, Ball::velocity, solVelocity().plus(pairs().map(Pair::velocityDelta).reduce((a, b) -> a.plus(b)).orElse(DPoint.NULL)));
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

    // Others Circles

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
            return DPoint.NULL;
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
