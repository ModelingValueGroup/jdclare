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
        assertEquals("pre age = " + Age.age(a), 0, Age.age(a));
        assertEquals("pre age = " + Age.age(o), 0, Age.age(o));
        Object cg = null;
        @SuppressWarnings("unused")
        int h = 0;
        for (int i = 0; i < 100_000_000; i++) {
            cg = new Object();
            h += cg.hashCode();
        }
        cg = null;
        assertTrue("post age = " + Age.age(a), Age.age(a) > 0);
        assertTrue("post age = " + Age.age(o), Age.age(o) > 0);
    }

}
