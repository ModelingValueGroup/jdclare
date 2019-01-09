package org.modelingvalue.jdclare.swing.examples.newton;

import org.modelingvalue.jdclare.Default;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.draw2d.DCanvas;

public interface Billiard extends DCanvas {

    @Default
    @Property
    default double rollingResistanceCoefficient() {
        return 100.0;
    }

}
