package org.modelingvalue.collections.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.modelingvalue.collections.util.Age;

public class AgeTest {

    @Test
    public void age() {
        Object[] a = new Object[1];
        Object o = new Object();
        a[0] = o;
        assertEquals(0, Age.age(a));
        assertEquals(0, Age.age(o));
        Object[] big = new Object[1000000000];
        if (0 < big.length) {
        }
        big = null;
        System.gc();
        assertEquals(1, Age.age(a));
        assertEquals(1, Age.age(o));
    }

}
