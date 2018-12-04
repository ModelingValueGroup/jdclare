package org.modelingvalue.jdclare.examples;

import static java.math.BigInteger.*;
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
        return nr() == 0 ? ZERO : nr() == 1 ? ONE : of(nr() - 1).add(of(nr() - 2));
    }

    static BigInteger of(int nr) {
        return dStruct(Fibonacci.class, nr).fibonaci();
    }

}
