package org.modelingvalue.jdclare.test;

import static org.junit.Assert.*;
import static org.modelingvalue.jdclare.DClare.*;

import org.junit.Assert;
import org.junit.Test;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.test.BirdUniverse.Bird;
import org.modelingvalue.jdclare.test.BirdUniverse.BlackCondorUniverse;
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
            DClare<RedCondorUniverse> redCondor = of(RedCondorUniverse.class);
            redCondor.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertTooManyChanges(cause);
        }
    }

    @Test
    public void tooManyChangesException2() {
        try {
            DClare<WhiteCondorUniverse> whiteCondor = of(WhiteCondorUniverse.class);
            whiteCondor.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertTooManyChanges(cause);
        }
    }

    @Test
    public void tooManyChangesException3() {
        try {
            DClare<GreenCondorUniverse> greenCondor = of(GreenCondorUniverse.class);
            greenCondor.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertTooManyChanges(cause);
        }
    }

    @Test
    public void tooManyChangesException4() {
        try {
            DClare<BlackCondorUniverse> blackCondor = of(BlackCondorUniverse.class);
            blackCondor.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertTooManyChanges(cause);
        }
    }

    @Test
    public void tooManyChangesException5() {
        try {
            DClare<BlueCondorUniverse> bleuCondor = of(BlueCondorUniverse.class);
            bleuCondor.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertTooManyChanges(cause);
        }
    }

    @Test
    public void tooManyChangesException6() {
        try {
            DClare<YellowCondorUniverse> yellowCondor = of(YellowCondorUniverse.class);
            yellowCondor.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertTooManyChanges(cause);
        }
    }
    
    @Test
    public void tooManySubscriptionsException() {
        try {
            DClare<GreyPigeonUniverse> pigeon = of(GreyPigeonUniverse.class);
            pigeon.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertTooManySubscriptions(cause);
        }
    }
    
    //@Test
    public void tooManySubscriptionsException2() {
    	// TODO test for too many observers  
    }
    
    @Test
    public void stackOverflow() {
        try {
        	DClare<BluePigeonUniverse> bluePigeon = of(BluePigeonUniverse.class);
        	bluePigeon.run();
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertStackOverflowError(cause);
        }
    }
    
    //@Test AKK this test fails at the moment - when are orphans deleted ? 
    public void noOrphans() {
        DClare<GreenPigeonUniverse> greenPigeon = of(GreenPigeonUniverse.class);
        State result = greenPigeon.run();
        Set<Bird> birds = result.getObjects(Bird.class).toSet();
        assertEquals("Unexpected Birds: " + birds, 1, birds.size());
    }
    
    @Test
    public void missingMandatory() {
    	 try {
    		 DClare<GreenHummingBirdUniverse> hummingBird = of(GreenHummingBirdUniverse.class);
    		 hummingBird.run();
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
    
    private void assertTooManySubscriptions(Throwable cause) {
        cause.printStackTrace();
        assertEquals(org.modelingvalue.transactions.TooManySubscriptionsException.class, cause.getClass());
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
