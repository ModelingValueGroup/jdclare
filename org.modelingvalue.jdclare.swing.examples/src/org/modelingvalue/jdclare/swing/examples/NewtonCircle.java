package org.modelingvalue.jdclare.swing.examples;

import static org.modelingvalue.jdclare.DClare.*;

import java.awt.event.KeyEvent;

import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.Default;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;
import org.modelingvalue.jdclare.swing.InputDeviceData;
import org.modelingvalue.jdclare.swing.draw2d.DCircle;
import org.modelingvalue.jdclare.swing.draw2d.DPoint;
import org.modelingvalue.jdclare.swing.draw2d.DShape;

public interface NewtonCircle extends NewtonShape, DCircle {

    default NewtonFrame frame() {
        return (NewtonFrame) canvas();
    }

    @Property
    default double mass() { // amount of dots
        return Math.PI * radius() * radius();
    }

    @Override
    default String text() {
        return "" + (int) mass();
    }

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

    @Default
    @Property
    default Vector velocity() { // dots per second
        return Vector.NULL;
    }

    @Rule
    default void setStill() {
        InputDeviceData di = canvas().deviceInput();
        if (di.pressedKeys().contains(KeyEvent.VK_ESCAPE)) {
            set(this, NewtonCircle::velocity, Vector.NULL);
        }
    }

    @Property
    default Set<NewtonCircle> others() {
        return canvas().shapes().filter(NewtonCircle.class).toSet().remove(this);
    }

    @Rule
    default void setNonDraggingVelocity() {
        if (!dragging() && !canvas().deviceInput().pressedKeys().contains(KeyEvent.VK_ESCAPE)) {
            set(this, NewtonCircle::velocity, friction(rebound(pre(this, NewtonCircle::velocity))));
        }
    }

    default Vector rebound(Vector vel) {
        DPoint min = dclare(DPoint.class, radius(), radius());
        DPoint max = canvas().size().toPoint().minus(min);
        DPoint pos = position();
        if ((min.x() > pos.x() && vel.x() < 0) || (max.x() < pos.x() && vel.x() > 0)) {
            vel = dclare(Vector.class, -vel.x(), vel.y());
        }
        if ((min.y() > pos.y() && vel.y() < 0) || (max.y() < pos.y() && vel.y() > 0)) {
            vel = dclare(Vector.class, vel.x(), -vel.y());
        }
        return vel;
    }

    default Vector friction(Vector vel) {
        return vel.minus(vel.mult(frame().frictionCoefficient()));
    }

    @Rule
    default void setNonDraggingPosition() {
        if (!dragging() && !canvas().deviceInput().pressedKeys().contains(KeyEvent.VK_ESCAPE)) {
            Vector preVelocity = pre(this, NewtonCircle::velocity);
            if (!preVelocity.equals(Vector.NULL)) {
                DPoint prePosition = pre(this, DShape::position);
                double passTime = dUniverse().clock().passSeconds();
                DPoint movement = preVelocity.mult(passTime).toPoint();
                DPoint position = prePosition.plus(movement);
                set(this, DShape::position, position);
            }
        }
    }

    @Rule
    default void setDraggingVelocity() {
        if (dragging()) {
            DPoint prePosition = pre(this, DShape::position);
            Vector preVelocity = pre(this, NewtonCircle::velocity);
            double passTime = dUniverse().clock().passSeconds();
            DPoint movement = position().minus(prePosition);
            Vector velocity = Vector.fromPoint(movement).div(passTime);
            Vector movingAvarage = preVelocity.plus(velocity).div(2.0);
            set(this, NewtonCircle::velocity, movingAvarage);
        }
    }

}
