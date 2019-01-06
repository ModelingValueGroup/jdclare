package org.modelingvalue.jdclare.swing.examples;

import static org.modelingvalue.jdclare.DClare.*;

import org.modelingvalue.jdclare.DStruct2;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.draw2d.DPoint;

public interface Vector extends DStruct2<Double, Double> {

    Vector NULL = dclare(Vector.class, 0.0, 0.0);

    @Property(key = 0)
    double x();

    @Property(key = 1)
    double y();

    default Vector mult(double factor) {
        return dclare(Vector.class, x() * factor, y() * factor);
    }

    default Vector div(double factor) {
        return dclare(Vector.class, x() / factor, y() / factor);
    }

    default DPoint toPoint() {
        return dclare(DPoint.class, (int) x(), (int) y());
    }

    static Vector fromPoint(DPoint point) {
        return dclare(Vector.class, (double) point.x(), (double) point.y());
    }

}
