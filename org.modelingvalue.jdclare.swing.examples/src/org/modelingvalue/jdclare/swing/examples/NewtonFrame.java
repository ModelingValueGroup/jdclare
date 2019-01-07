package org.modelingvalue.jdclare.swing.examples;

import org.modelingvalue.jdclare.Default;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.draw2d.DCanvas;

public interface NewtonFrame extends DCanvas {

    @Default
    @Property
    default double frictionCoefficient() {
        return 0.005;
    }

}
