package org.modelingvalue.jdclare.test;

import static org.junit.Assert.*;
import static org.modelingvalue.jdclare.DClare.*;

import org.junit.Assert;
import org.junit.Test;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.test.BirdUniverse.BlackCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.BlueCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.GreenCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.RedCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.WhiteCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.YellowCondorUniverse;

public class BirdTest {

    @Test
    public void TooManyChangesException1() {
        try {
            DClare<RedCondorUniverse> redCondor = of(RedCondorUniverse.class);
            redCondor.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertEquals(org.modelingvalue.transactions.TooManyChangesException.class, cause.getClass());
        }
    }

    @Test
    public void TooManyChangesException2() {
        try {
            DClare<WhiteCondorUniverse> whiteCondor = of(WhiteCondorUniverse.class);
            whiteCondor.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertEquals(org.modelingvalue.transactions.TooManyChangesException.class, cause.getClass());
        }
    }

    @Test
    public void TooManyChangesException3() {
        try {
            DClare<GreenCondorUniverse> greenCondor = of(GreenCondorUniverse.class);
            greenCondor.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertEquals(org.modelingvalue.transactions.TooManyChangesException.class, cause.getClass());
        }
    }

    @Test
    public void TooManyChangesException4() {
        try {
            DClare<BlackCondorUniverse> blackCondor = of(BlackCondorUniverse.class);
            blackCondor.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertEquals(org.modelingvalue.transactions.TooManyChangesException.class, cause.getClass());
        }
    }

    @Test
    public void TooManyChangesException5() {
        try {
            DClare<BlueCondorUniverse> bleuCondor = of(BlueCondorUniverse.class);
            bleuCondor.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertEquals(org.modelingvalue.transactions.TooManyChangesException.class, cause.getClass());
        }
    }

    @Test
    public void TooManyChangesException6() {
        try {
            DClare<YellowCondorUniverse> yellowCondor = of(YellowCondorUniverse.class);
            yellowCondor.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertEquals(org.modelingvalue.transactions.TooManyChangesException.class, cause.getClass());
        }
    }

    private Throwable getCause(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

}
