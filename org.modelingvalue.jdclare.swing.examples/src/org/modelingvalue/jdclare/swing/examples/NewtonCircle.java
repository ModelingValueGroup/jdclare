package org.modelingvalue.jdclare.swing.examples;

import static org.modelingvalue.jdclare.DClare.*;

import org.modelingvalue.jdclare.Default;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;
import org.modelingvalue.jdclare.swing.draw2d.DCircle;
import org.modelingvalue.jdclare.swing.draw2d.DFilled;
import org.modelingvalue.jdclare.swing.draw2d.DPoint;
import org.modelingvalue.jdclare.swing.draw2d.DShape;

public interface NewtonCircle extends NewtonShape, DCircle {

    public static final double BILLION = 1000000000.0;

    @Property
    @Default
    default double mass() { // kilogram
        return 1.0;
    }

    @Property
    @Default
    default Vector velocity() { // meter per second
        return Vector.NULL;
    }

    @Rule
    default void nonDraggingPosition() {
        if (!dragging()) {
            Vector velocity = velocity();
            if (!velocity.equals(Vector.NULL)) {
                DPoint prePosition = pre(this, DShape::position);
                double passTime = dUniverse().clock().passSeconds();
                DPoint movement = velocity.mult(passTime).toPoint();
                DPoint position = prePosition.plus(movement);
                set(this, DShape::position, position);
            }
        }
    }

    @Rule
    default void draggingVelocity() {
        if (pre(this, DFilled::dragging) && !dragging()) {
            DPoint prePosition = pre(this, DShape::position);
            double passTime = dUniverse().clock().passSeconds();
            DPoint movement = position().minus(prePosition);
            Vector velocity = Vector.fromPoint(movement).div(passTime);
            set(this, NewtonCircle::velocity, velocity);
        }
    }

}
