package org.modelingvalue.jdclare.test;

import static org.junit.Assert.assertEquals;
import static org.modelingvalue.jdclare.DClare.dclare;
import static org.modelingvalue.jdclare.DClare.of;
import static org.modelingvalue.jdclare.DClare.set;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.test.BirdUniverse.Bird;
import org.modelingvalue.jdclare.test.BirdUniverse.Condor;
import org.modelingvalue.jdclare.test.BirdUniverse.HummingBird;
import org.modelingvalue.jdclare.test.BirdUniverse.Pheasant;
import org.modelingvalue.jdclare.test.BirdUniverse.Pigeon;
import org.modelingvalue.jdclare.test.BirdUniverse.Sparrow;
import org.modelingvalue.transactions.NonDeterministicException;
import org.modelingvalue.transactions.State;
import org.modelingvalue.transactions.TooManyChangesException;
import org.modelingvalue.transactions.TooManyObservedException;
import org.modelingvalue.transactions.TooManyObserversException;

public class BirdTest {

    private void addBird(DClare<BirdUniverse> root, Class<? extends Bird> clazz, Pair<String, String> props) {
        root.put(root.universe(), () -> {
            Bird bird = dclare(clazz, root.universe(), props.a());
            set(bird, Bird::color, props.b());
            set(root.universe(), BirdUniverse::birds, Set::add, bird);
        });
    }

    private void addBirds(DClare<BirdUniverse> root, Class<? extends Bird> clazz, java.util.Set<Pair<String, String>> props) {
        root.put(root.universe(), () -> {
            for (Pair<String, String> p : props) {
                Bird bird = dclare(clazz, root.universe(), p.a());
                set(bird, Bird::color, p.b());
                set(root.universe(), BirdUniverse::birds, Set::add, bird);
            }
        });
    }

    private State stop(DClare<BirdUniverse> root) {
        root.stop();
        return root.waitForEnd();
    }

    private void start(DClare<BirdUniverse> root) {
        root.start();
    }

    @Test
    public void tooManyChangesException1() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, Condor.class, Pair.of("0", "red"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyChangesException.class);
        }
    }

    @Test
    public void tooManyChangesException2() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, Condor.class, Pair.of("0", "white"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyChangesException.class);
        }
    }

    @Test
    public void tooManyChangesException3() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, Condor.class, Pair.of("0", "green"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyChangesException.class);
        }
    }

    @Test
    public void tooManyChangesException4() {

        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, Condor.class, Pair.of("0", "black"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyChangesException.class);
        }
    }

    @Test
    public void tooManyChangesException5() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, Condor.class, Pair.of("0", "blue"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyChangesException.class);
        }
    }

    @Test
    public void tooManyChangesException6() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, Condor.class, Pair.of("0", "yellow"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyChangesException.class);
        }
    }

    @Test
    public void tooManyChangesException7() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, Condor.class, Pair.of("0", "brown"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyChangesException.class);
        }
    }

    @Test
    public void tooManyObservedException() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, Pigeon.class, Pair.of("0", "grey"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyObservedException.class, "Too many observed (10002) by 0.Pigeon::addChildren", x -> ((TooManyObservedException) x).getSimpleMessage());
        }
    }

    //@Test
    // TODO this test fails - when are orphans deleted ?
    public void noOrphans() {
        DClare<BirdUniverse> root = of(BirdUniverse.class);
        start(root);
        addBird(root, Pigeon.class, Pair.of("0", "green"));
        State result = stop(root);
        Set<Bird> birds = result.getObjects(Bird.class).toSet();
        assertEquals("Unexpected Birds: " + birds, 1, birds.size());
    }

    //@Test
    // TODO
    // Test does not throw expected exception.
    public void tooManyObserversException() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, Sparrow.class, Pair.of("0", "black"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, TooManyObserversException.class);
        }
    }

    @Test
    public void missingMandatory1() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, HummingBird.class, Pair.of("0", "green"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, Error.class, "Fatal problems: [fatal MANDATORY Problem 'color is empty.' on '0+']");
        }
    }

    //@Test
    //TODO this test fails
    public void missingMandatory2() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, HummingBird.class, Pair.of("0", "blue"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, Error.class, "Fatal problems: [fatal MANDATORY Problem 'color is empty.' on '0+']");
        }
    }

    @Test
    public void nullPointerException() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, HummingBird.class, Pair.of("0", "yellow"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, NullPointerException.class);
        }
    }

    @Test
    public void arithmeticException() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, HummingBird.class, Pair.of("0", "red"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, ArithmeticException.class);
        }
    }

    @Test
    public void nonDeterministicException1() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, Pheasant.class, Pair.of("0", "blue"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, NonDeterministicException.class, "Constant is not consistent 0.Constants:0=blue!=cobalt");
        }
    }

    @Test
    public void nonDeterministicException2() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, Pheasant.class, Pair.of("0", "yellow"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, NonDeterministicException.class, "Constant is not consistent 0.Constants:0=yellow!=gold");
        }
    }

    @Test
    public void nonDeterministicException3() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            java.util.Set<Pair<String, String>> props = new HashSet<>();
            props.add(Pair.of("0", "green"));
            props.add(Pair.of("1", "yellow"));
            addBirds(root, Pheasant.class, props);
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, NonDeterministicException.class, "Constant is not consistent 1.Constants:1=yellow!=gold");
        }
    }

    @Test
    public void concurrentModificationException() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            java.util.Set<Pair<String, String>> props = new HashSet<>();
            props.add(Pair.of("0", "green"));
            props.add(Pair.of("0", "yellow"));
            addBirds(root, Pheasant.class, props);
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, ConcurrentModificationException.class, "0.color= null -> green | yellow");
        }
    }

    @Test
    public void constantNotSetAndNotDerivedError() {
        //TODO I was expecting a circular constant error here
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, Pheasant.class, Pair.of("0", "red"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, Error.class, "Constant headColor is not set and not derived");
        }
    }

    @Test
    public void constantIsDerivedError() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, Pheasant.class, Pair.of("0", "black"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, Error.class, "Constant leftLegColor is derived");
        }
    }

    @Test
    public void circularConstantError() {
        try {
            DClare<BirdUniverse> root = of(BirdUniverse.class);
            start(root);
            addBird(root, Pheasant.class, Pair.of("0", "white"));
            stop(root);
            Assert.fail();
        } catch (Throwable t) {
            Throwable cause = getCause(t);
            assertThrowable(cause, Error.class, "Constant rightLegColor is derived");
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
