package org.modelingvalue.jdclare.swing.examples.newton;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.draw2d.DCanvas;
import org.modelingvalue.jdclare.swing.draw2d.DPoint;

public interface BilliardTable extends DCanvas {

    @Property(constant)
    default double rollingResistance() {
        return 0.1;
    }

    @Property(constant)
    default double borderBouncingResistance() {
        return 0.1;
    }

    @Property(constant)
    default double ballsBouncingResistance() {
        return 0.1;
    }

    @Property(constant)
    default double ballRadius() {
        return 20.0;
    }

    @Property(constant)
    default DPoint minimum() {
        return dclare(DPoint.class, ballRadius(), ballRadius());
    }

    @Property
    default DPoint maximum() {
        return size().toPoint().minus(minimum());
    }

}
