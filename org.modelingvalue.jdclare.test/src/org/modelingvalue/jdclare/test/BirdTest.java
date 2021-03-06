package org.modelingvalue.jdclare.test;

import static org.junit.Assert.*;
import static org.modelingvalue.jdclare.DClare.*;

import java.util.HashSet;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.NotMergeableException;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.dclare.EmptyMandatoryException;
import org.modelingvalue.dclare.NonDeterministicException;
import org.modelingvalue.dclare.State;
import org.modelingvalue.dclare.TooManyChangesException;
import org.modelingvalue.dclare.TooManyObservedException;
import org.modelingvalue.dclare.TooManyObserversException;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.test.BirdUniverse.Bird;
import org.modelingvalue.jdclare.test.BirdUniverse.Condor;
import org.modelingvalue.jdclare.test.BirdUniverse.HouseSparrow;
import org.modelingvalue.jdclare.test.BirdUniverse.HummingBird;
import org.modelingvalue.jdclare.test.BirdUniverse.Pheasant;
import org.modelingvalue.jdclare.test.BirdUniverse.Pigeon;
import org.modelingvalue.jdclare.test.BirdUniverse.Sparrow;

public class BirdTest {

    private static final boolean PRINT_STACK_TRACE = Boolean.getBoolean("PRINT_STACK_TRACE");

    private void addBird(DClare<BirdUniverse> dclare, Class<? extends Bird> clazz, Pair<String, String> props) {
        dclare.put(dclare.universe(), () -> {
            Bird bird = dclare(clazz, dclare.universe(), props.a());
            set(bird, Bird::color, props.b());
            set(dclare.universe(), BirdUniverse::birds, Set::add, bird);
        });
    }

    private void addBirds(DClare<BirdUniverse> dclare, Class<? extends Bird> clazz, java.util.Set<Pair<String, String>> props) {
        dclare.put(dclare.universe(), () -> {
            for (Pair<String, String> p : props) {
                Bird bird = dclare(clazz, dclare.universe(), p.a());
                set(bird, Bird::color, p.b());
                set(dclare.universe(), BirdUniverse::birds, Set::add, bird);
            }
        });
    }

    private State stop(DClare<BirdUniverse> dclare) {
        dclare.stop();
        return dclare.waitForEnd();
    }

    private void start(DClare<BirdUniverse> dclare) {
        dclare.start();
    }

    @Test
    public void tooManyChangesException1() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, Condor.class, Pair.of("0", "red"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyChangesException.class);
        }
    }

    @Test
    public void tooManyChangesException2() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, Condor.class, Pair.of("0", "white"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyChangesException.class);
        }
    }

    @Test
    public void tooManyChangesException3() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, Condor.class, Pair.of("0", "green"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyChangesException.class);
        }
    }

    @Test
    public void tooManyChangesException4() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, Condor.class, Pair.of("0", "black"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyChangesException.class);
        }
    }

    @Test
    public void tooManyChangesException5() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, Condor.class, Pair.of("0", "blue"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyChangesException.class);
        }
    }

    @Test
    public void tooManyChangesException6() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, Condor.class, Pair.of("0", "brown"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyChangesException.class);
        }
    }

    @Test
    public void tooManyChangesException7() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, Condor.class, Pair.of("0", "yellow"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyChangesException.class);
        }
    }

    @Test
    public void tooManyObservedException1() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, Pigeon.class, Pair.of("0", "grey"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyObservedException.class, "Too many observed (10002) by 0.Pigeon::addChildren1", x -> ((TooManyObservedException) x).getSimpleMessage());
        }
    }

    @Test
    public void tooManyObservedException2() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, Pigeon.class, Pair.of("0", "yellow"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyObservedException.class, "Too many observed (4802) by 0.Pigeon::addChildren2", x -> ((TooManyObservedException) x).getSimpleMessage());
        }
    }

    @Test
    public void noOrphans() {
        DClare<BirdUniverse> dclare = of(BirdUniverse.class);
        start(dclare);
        addBird(dclare, Pigeon.class, Pair.of("0", "green"));
        State result = stop(dclare);
        Set<Bird> birds = result.getObjects(Bird.class).toSet();
        assertEquals("Unexpected Birds: " + birds, 1, birds.size());
    }

    // @Test
    public void tooManyObserversException1() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, HouseSparrow.class, Pair.of("1", "yellow"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyObserversException.class, "Too many observers (2801) of 1.D_PARENT", x -> ((TooManyObserversException) x).getSimpleMessage());
        }
    }

    @Test
    public void tooManyObserversException2() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, Sparrow.class, Pair.of("0", "black"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyObserversException.class, "Too many observers (2002) of 0.color", x -> ((TooManyObserversException) x).getSimpleMessage());
        }
    }

    @Test
    public void missingMandatory1() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, HummingBird.class, Pair.of("0", "green"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, EmptyMandatoryException.class, java.util.regex.Pattern.quote("Empty mandatory property 'color' of object '0+'"));
        }
    }

    @Test
    public void missingMandatory2() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, HummingBird.class, Pair.of("0", "blue"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, EmptyMandatoryException.class, java.util.regex.Pattern.quote("Empty mandatory property 'color' of object '0+'"));
        }
    }

    @Test
    public void nullPointerException2() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, HummingBird.class, Pair.of("0", "yellow"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, NullPointerException.class);
        }
    }

    @Test
    public void arithmeticException() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, HummingBird.class, Pair.of("0", "red"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, ArithmeticException.class);
        }
    }

    @Test
    public void nonDeterministicException1() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, Pheasant.class, Pair.of("0", "blue"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, NonDeterministicException.class);
        }
    }

    @Test
    public void nonDeterministicException2() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, Pheasant.class, Pair.of("0", "yellow"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, NonDeterministicException.class, java.util.regex.Pattern.quote("Constant is not consistent 0.tailColor=yellow!=notyellow"));
        }
    }

    @Test
    public void nonDeterministicException3() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            java.util.Set<Pair<String, String>> props = new HashSet<>();
            props.add(Pair.of("0", "green"));
            props.add(Pair.of("1", "yellow"));
            addBirds(dclare, Pheasant.class, props);
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, NonDeterministicException.class, java.util.regex.Pattern.quote("Constant is not consistent 1.tailColor=yellow!=notyellow"));
        }
    }

    @Test
    public void notMergeableException() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            java.util.Set<Pair<String, String>> props = new HashSet<>();
            props.add(Pair.of("0", "green"));
            props.add(Pair.of("0", "yellow"));
            addBirds(dclare, Pheasant.class, props);
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, NotMergeableException.class, java.util.regex.Pattern.quote("0.color= null -> [green,yellow]"));
        }
    }

    @Test
    public void constantNotSetAndNotDerivedError() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, Pheasant.class, Pair.of("0", "red"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, Error.class, "Constant headColor is not set and not derived");
        }
    }

    @Test
    public void constantIsDerivedError() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, Pheasant.class, Pair.of("0", "black"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, Error.class, "Constant legColor is derived");
        }
    }

    @Test
    public void circularConstantError() {
        try {
            DClare<BirdUniverse> dclare = of(BirdUniverse.class);
            start(dclare);
            addBird(dclare, Pheasant.class, Pair.of("0", "white"));
            stop(dclare);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, Error.class, "Constant (left|right)LegColor is derived");

        }
    }

    private Throwable getCause(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    private void assertThrowable(Throwable cause, Class<? extends Throwable> throwable) {
        if (PRINT_STACK_TRACE || !throwable.equals(cause.getClass())) {
            cause.printStackTrace();
        }
        assertEquals(throwable, cause.getClass());
    }

    private void assertThrowable(Throwable cause, Class<? extends Throwable> throwable, String regex) {
        if (PRINT_STACK_TRACE || !throwable.equals(cause.getClass())) {
            cause.printStackTrace();
        }
        assertEquals(throwable, cause.getClass());
        assertTrue(cause.getMessage() + " != " + regex, cause.getMessage().matches(regex));
    }

    private void assertThrowable(Throwable cause, Class<? extends Throwable> throwable, String message, Function<Throwable, String> f) {
        if (PRINT_STACK_TRACE || !throwable.equals(cause.getClass())) {
            cause.printStackTrace();
        }
        assertEquals(throwable, cause.getClass());
        assertEquals(message, f.apply(cause));
    }

}
