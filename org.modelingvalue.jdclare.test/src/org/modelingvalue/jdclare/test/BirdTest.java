package org.modelingvalue.jdclare.test;

import static org.junit.Assert.assertEquals;
import static org.modelingvalue.jdclare.DClare.of;

import java.util.ConcurrentModificationException;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.test.BirdUniverse.Bird;
import org.modelingvalue.jdclare.test.BirdUniverse.BlackCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.BlackPheasantUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.BlackSparrowUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.BlueCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.BlueHummingBirdUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.BluePhaesantUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.BluePigeonUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.GreenCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.GreenHummingBirdUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.GreenPhaesantUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.GreenPigeonUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.GreyPigeonUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.RedCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.RedPheasantUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.WhiteCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.YellowCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.YellowHummingBirdUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.YellowPheasantUniverse;
import org.modelingvalue.transactions.NonDeterministicException;
import org.modelingvalue.transactions.State;
import org.modelingvalue.transactions.TooManyChangesException;
import org.modelingvalue.transactions.TooManyObservedException;
import org.modelingvalue.transactions.TooManyObserversException;

public class BirdTest {

    //TODO : refactor - use one universe, and different birds
    //TODO : add transactionException (e.g. by division by zero)
    //TODO : solve TODOs

    @Test
    public void tooManyChangesException1() {
        //        try {
        //        	DClare<BirdUniverse> birdUniverse = of(BirdUniverse.class);
        //        	BirdUniverse universe = birdUniverse.universe();
        //         	universe.addBird(Condor.class, "0", "red");
        //         	Assert.fail();
        //		 } catch (Throwable t) {
        //			 Throwable cause = getCause(t);
        //			 assertThrowable(cause, TooManyChangesException.class);
        //		 }	
        try {
            DClare<RedCondorUniverse> redCondorUniverse = of(RedCondorUniverse.class);
            redCondorUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyChangesException.class);
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
            assertThrowable(cause, TooManyChangesException.class);
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
            assertThrowable(cause, TooManyChangesException.class);
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
            assertThrowable(cause, TooManyChangesException.class);
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
            assertThrowable(cause, TooManyChangesException.class);
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
            assertThrowable(cause, TooManyChangesException.class);
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
            assertThrowable(cause, TooManyObservedException.class, "Too many observed (10002) by 0.Pigeon::addChildren", x -> ((TooManyObservedException) x).getSimpleMessage());
        }
    }

    // @Test  
    // TODO
    // Test does not throw expected exception.
    public void tooManyObserversException() {
        try {
            DClare<BlackSparrowUniverse> blackSparrowUniverse = of(BlackSparrowUniverse.class);
            blackSparrowUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyObserversException.class);
        }
    }

    @Test
    public void tooManyChangesException7() {
        try {
            DClare<BluePigeonUniverse> bluePigeonUniverse = of(BluePigeonUniverse.class);
            bluePigeonUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyChangesException.class);
        }
    }

    //@Test 
    // TODO this test fails at the moment - when are orphans deleted ? 
    public void noOrphans() {
        DClare<GreenPigeonUniverse> greenPigeonUniverse = of(GreenPigeonUniverse.class);
        State result = greenPigeonUniverse.run();
        Set<Bird> birds = result.getObjects(Bird.class).toSet();
        assertEquals("Unexpected Birds: " + birds, 1, birds.size());
    }

    @Test
    public void missingMandatory1() {
        try {
            DClare<GreenHummingBirdUniverse> hummingBirdUniverse = of(GreenHummingBirdUniverse.class);
            hummingBirdUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, Error.class, "Fatal problems: [fatal MANDATORY Problem 'color is empty.' on '0+']");
        }
    }

    //@Test TODO this test fails
    public void missingMandatory2() {
        try {
            DClare<BlueHummingBirdUniverse> hummingBirdUniverse = of(BlueHummingBirdUniverse.class);
            hummingBirdUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, Error.class, "Fatal problems: [fatal MANDATORY Problem 'color is empty.' on '0+']");
        }
    }

    @Test
    public void nullPointerException() {
        try {
            DClare<YellowHummingBirdUniverse> hummingBirdUniverse = of(YellowHummingBirdUniverse.class);
            hummingBirdUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, NullPointerException.class);
        }
    }

    @Test
    public void nonDeterministicException1() {
        try {
            DClare<BluePhaesantUniverse> bluePheasantUniverse = of(BluePhaesantUniverse.class);
            bluePheasantUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, NonDeterministicException.class, "Constant is not consistent 0.Constants:0=blue!=cobalt");
        }
    }

    @Test
    public void concurrentModificationException() {
        try {
            DClare<GreenPhaesantUniverse> greenPheasantUniverse = of(GreenPhaesantUniverse.class);
            greenPheasantUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, ConcurrentModificationException.class, "0.color= null -> green | yellow");
        }
    }

    @Test
    public void nonDeterministicException2() {
        try {
            DClare<YellowPheasantUniverse> yellowPheasantUniverse = of(YellowPheasantUniverse.class);
            yellowPheasantUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, NonDeterministicException.class, "Constant is not consistent 0.Constants:0=yellow!=gold");
        }
    }

    @Test
    public void constantNotSetAndNotDerivedError() {
        try {
            DClare<RedPheasantUniverse> redPheasantUniverse = of(RedPheasantUniverse.class);
            redPheasantUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, Error.class, "Constant headColor is not set and not derived");
        }
    }

    @Test
    public void constantIsDerivedError() {
        try {
            DClare<BlackPheasantUniverse> blackPheasantUniverse = of(BlackPheasantUniverse.class);
            blackPheasantUniverse.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, Error.class, "Constant leg1Color is derived");
        }
    }

    private Throwable getCause(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    private void assertThrowable(Throwable cause, Class<? extends Throwable> throwable) {
        cause.printStackTrace();
        assertEquals(throwable, cause.getClass());
    }

    private void assertThrowable(Throwable cause, Class<? extends Throwable> throwable, String message) {
        cause.printStackTrace();
        assertEquals(throwable, cause.getClass());
        assertEquals(message, cause.getMessage());
    }

    private void assertThrowable(Throwable cause, Class<? extends Throwable> throwable, String message, Function<Throwable, String> f) {
        cause.printStackTrace();
        assertEquals(throwable, cause.getClass());
        assertEquals(message, f.apply(cause));
    }

}
