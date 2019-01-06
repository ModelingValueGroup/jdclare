package org.modelingvalue.jdclare.swing.examples;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.time.Duration;
import java.time.Instant;

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
    double mass(); // kilogram

    @Property
    @Default
    default Vector velocity() { // meter per second
        return Vector.NULL;
    }

    @Override
    default DPoint nonDraggingPosition() {
        Vector velocity = velocity();
        if (!velocity.equals(Vector.NULL)) {
            DPoint prePosition = pre(this, DShape::position);
            double passTime = dUniverse().clock().passSeconds();
            DPoint movement = velocity.mult(passTime).toPoint();
            return prePosition.plus(movement);
        } else {
            return position();
        }
    }

    @Property(optional)
    Instant startDraggingTime();

    @Rule
    default void draggingVelocity() {
        DPoint pre = pre(this, DFilled::dragStartPosition);
        DPoint post = dragStartPosition();
        if (pre == null && post != null) {
            //start dragging
            set(this, NewtonCircle::startDraggingTime, dUniverse().clock().time());
        } else if (pre != null && post == null) {
            //stop dragging
            DPoint moved = dragStopPosition().minus(pre);
            double passTime = Duration.between(startDraggingTime(), dUniverse().clock().time()).toNanos() / 1000000000.0;
            Vector velocity = Vector.fromPoint(moved).div(passTime);
            set(this, NewtonCircle::velocity, velocity);
        }
    }

}
