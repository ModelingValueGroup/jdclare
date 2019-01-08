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
        return Math.PI * Math.pow(radius(), 2.0);
    }

    @Default
    @Property
    default DPoint velocity() { // dots per second
        return DPoint.NULL;
    }

    @Property
    default DPoint force() { // Newton
        double mass = mass();
        DPoint preVelocity = pre(this, NewtonCircle::velocity);
        if (mass > 0 && !preVelocity.equals(DPoint.NULL)) {
            DPoint momentum = preVelocity.mult(mass);
            return momentum.mult(-frictionForce());
        } else {
            return DPoint.NULL;
        }
    }

    @Property
    default double frictionForce() {
        return frame().rollingResistanceCoefficient() * mass() / radius();
    }

    @Property
    default DPoint acceleration() { // dots per sqrt second
        double mass = mass();
        return mass > 0.0 ? force().div(mass) : DPoint.NULL;
    }

    @Rule
    default void setNonDraggingVelocity() {
        if (!dragging() && !canvas().deviceInput().pressedKeys().contains(KeyEvent.VK_ESCAPE)) {
            DPoint acceleration = acceleration();
            if (!acceleration.equals(DPoint.NULL)) {
                DPoint preVelocity = pre(this, NewtonCircle::velocity);
                double passTime = dUniverse().clock().passSeconds();
                DPoint velocity = preVelocity.plus(acceleration.mult(passTime));
                set(this, NewtonCircle::velocity, velocity);
            }
        }
    }

    @Rule
    default void setNonDraggingPosition() {
        if (!dragging() && !canvas().deviceInput().pressedKeys().contains(KeyEvent.VK_ESCAPE)) {
            DPoint preVelocity = pre(this, NewtonCircle::velocity);
            DPoint acceleration = acceleration();
            if (!preVelocity.equals(DPoint.NULL) || !acceleration.equals(DPoint.NULL)) {
                DPoint prePosition = pre(this, DShape::position);
                double passTime = dUniverse().clock().passSeconds();
                DPoint preMovement = preVelocity.mult(passTime);
                DPoint movement = acceleration.mult(Math.pow(passTime, 2.0)).div(0.5);
                DPoint position = prePosition.plus(preMovement).plus(movement);
                set(this, DShape::position, position);
            }
        }
    }

    // While dragging

    @Rule
    default void setDraggingVelocityAndPosition() {
        if (dragging()) {
            DPoint prePosition = pre(this, DShape::position);
            DPoint position = position();
            DPoint preVelocity = pre(this, NewtonCircle::velocity);
            double passTime = dUniverse().clock().passSeconds();
            DPoint movement = position.minus(prePosition);
            DPoint velocity = movement.div(passTime);
            DPoint movingAvarage = preVelocity.plus(velocity).div(2.0);
            set(this, NewtonCircle::velocity, movingAvarage);
        }
    }

    // Others Circles

    @Property
    default Set<NewtonCircle> others() {
        return canvas().shapes().filter(NewtonCircle.class).toSet().remove(this);
    }

    @Property
    default Set<NewtonCircle> bouncing() {
        int radius = radius();
        DPoint position = position();
        return others().filter(o -> o.radius() + radius > position.minus(o.position()).length()).toSet();
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
            set(this, NewtonCircle::velocity, DPoint.NULL);
        }
    }

}
