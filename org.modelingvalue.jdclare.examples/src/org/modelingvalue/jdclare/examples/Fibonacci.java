package org.modelingvalue.jdclare.examples;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.Property;

public interface Fibonacci extends DStruct1<Long> {

    @Property(key = 0)
    long nr();

    @Property(constant)
    default long fibonaci() {
        return nr() == 0 || nr() == 1 ? 1 : dStruct(Fibonacci.class, nr() - 1).fibonaci() + dStruct(Fibonacci.class, nr() - 2).fibonaci();
    }

}
