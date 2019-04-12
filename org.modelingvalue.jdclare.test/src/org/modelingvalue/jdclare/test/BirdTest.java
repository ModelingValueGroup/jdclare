package org.modelingvalue.jdclare.test;

import static org.junit.Assert.*;
import static org.modelingvalue.jdclare.DClare.*;

import org.junit.Assert;
import org.junit.Test;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.test.BirdUniverse.Bird;
import org.modelingvalue.jdclare.test.BirdUniverse.BlackCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.BlackSparrowUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.BlueCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.BluePigeonUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.GreenCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.GreenHummingBirdUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.GreenPigeonUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.GreyPigeonUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.RedCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.WhiteCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.YellowCondorUniverse;
import org.modelingvalue.transactions.State;

public class BirdTest {

    @Test
    public void tooManyChangesException1() {
        try {
            DClare<RedCondorUniverse> redCondorUniverse = of(RedCondorUniverse.class);
            redCondorUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertTooManyChanges(cause);
        }
    }

    @Test
    public void tooManyChangesException2() {
        try {
            DClare<WhiteCondorUniverse> whiteCondorUniverse = of(WhiteCondorUniverse.class);
            whiteCondorUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertTooManyChanges(cause);
        }
    }

    @Test
    public void tooManyChangesException3() {
        try {
            DClare<GreenCondorUniverse> greenCondorUniverse = of(GreenCondorUniverse.class);
            greenCondorUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertTooManyChanges(cause);
        }
    }

    @Test
    public void tooManyChangesException4() {
        try {
            DClare<BlackCondorUniverse> blackCondorUniverse = of(BlackCondorUniverse.class);
            blackCondorUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertTooManyChanges(cause);
        }
    }

    @Test
    public void tooManyChangesException5() {
        try {
            DClare<BlueCondorUniverse> bleuCondorUniverse = of(BlueCondorUniverse.class);
            bleuCondorUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertTooManyChanges(cause);
        }
    }

    @Test
    public void tooManyChangesException6() {
        try {
            DClare<YellowCondorUniverse> yellowCondorUniverse = of(YellowCondorUniverse.class);
            yellowCondorUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertTooManyChanges(cause);
        }
    }

    @Test
    public void tooManyObservedException() {
        try {
            DClare<GreyPigeonUniverse> greyPigeonUniverseUniverse = of(GreyPigeonUniverse.class);
            greyPigeonUniverseUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertTooManyObserved(cause);
        }
    }

    //@Test 
    public void tooManyObserversException() {
        try {
            DClare<BlackSparrowUniverse> blackSparrowUniverse = of(BlackSparrowUniverse.class);
            blackSparrowUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertTooManyObservers(cause);
        }
    }

    @Test
    public void stackOverflow() {
        try {
            DClare<BluePigeonUniverse> bluePigeonUniverse = of(BluePigeonUniverse.class);
            bluePigeonUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertStackOverflowError(cause);
        }
    }

    //@Test AKK this test fails at the moment - when are orphans deleted ? 
    public void noOrphans() {
        DClare<GreenPigeonUniverse> greenPigeonUniverse = of(GreenPigeonUniverse.class);
        State result = greenPigeonUniverse.run();
        Set<Bird> birds = result.getObjects(Bird.class).toSet();
        assertEquals("Unexpected Birds: " + birds, 1, birds.size());
    }

    @Test
    public void missingMandatory() {
        try {
            DClare<GreenHummingBirdUniverse> hummingBirdUniverse = of(GreenHummingBirdUniverse.class);
            hummingBirdUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertError(cause, "Fatal problems: [fatal MANDATORY Problem 'color is empty.' on '0+']");
        }
    }

    private Throwable getCause(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    private void assertTooManyChanges(Throwable cause) {
        cause.printStackTrace();
        assertEquals(org.modelingvalue.transactions.TooManyChangesException.class, cause.getClass());
    }

    private void assertTooManyObservers(Throwable cause) {
        cause.printStackTrace();
        assertEquals(org.modelingvalue.transactions.TooManyObserversException.class, cause.getClass());
    }

    private void assertTooManyObserved(Throwable cause) {
        cause.printStackTrace();
        assertEquals(org.modelingvalue.transactions.TooManyObservedException.class, cause.getClass());
    }

    private void assertStackOverflowError(Throwable cause) {
        cause.printStackTrace();
        assertEquals(java.lang.StackOverflowError.class, cause.getClass());
    }

    private void assertError(Throwable cause, String message) {
        cause.printStackTrace();
        assertEquals(java.lang.Error.class, cause.getClass());
        assertEquals(message, cause.getMessage());
    }

}
