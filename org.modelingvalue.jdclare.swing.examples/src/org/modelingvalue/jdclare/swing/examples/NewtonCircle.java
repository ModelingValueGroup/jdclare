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

    @Property
    default double mass() { // amount of dots
        return Math.PI * radius() * radius();
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

    @Rule
    default void setNonDraggingPosition() {
        if (!dragging()) {
            Vector velocity = velocity();
            if (!velocity.equals(Vector.NULL) && !canvas().deviceInput().pressedKeys().contains(KeyEvent.VK_ESCAPE)) {
                DPoint prePosition = pre(this, DShape::position);
                double passTime = dUniverse().clock().passSeconds();
                DPoint movement = velocity.mult(passTime).toPoint();
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
