package org.modelingvalue.jdclare.swing.examples.newton;

import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.draw2d.DPoint;

public interface CollisionPair extends DObject {

    @Property
    double preCollisionTime();

    double postCollisionTime();

    DPoint velocity(Ball ball);

    double distance();

}
