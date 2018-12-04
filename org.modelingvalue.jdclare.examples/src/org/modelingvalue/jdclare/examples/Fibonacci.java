package org.modelingvalue.jdclare.examples;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.math.BigInteger;

import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.Property;

public interface Fibonacci extends DStruct1<Integer> {

    @Property(key = 0)
    int nr();

    @Property(constant)
    default BigInteger fibonaci() {
        int nr = nr();
        return nr == 0 ? BigInteger.ZERO : nr == 1 ? BigInteger.ONE : dStruct(Fibonacci.class, nr - 1).fibonaci().add(dStruct(Fibonacci.class, nr - 2).fibonaci());
    }

}
