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
        System.gc();
        synchronized (this) {
            try {
                wait(10l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        assertEquals(1, Age.age(a));
        assertEquals(1, Age.age(o));
    }

}
