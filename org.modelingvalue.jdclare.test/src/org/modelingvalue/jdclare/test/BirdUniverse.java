package org.modelingvalue.jdclare.test;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DNamed;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct2;
import org.modelingvalue.jdclare.DUniverse;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;

public interface BirdUniverse extends DUniverse {

    @Property(containment)
    Set<Bird> birds();

    default void addBird(Class<? extends Bird> clazz, String name, String color) {
        Bird bird = dclare(clazz, this, name);
        set(this, BirdUniverse::birds, Set::add, bird);
        set(bird, Bird::color, color);
    }

    interface Bird extends DStruct2<DObject, String>, DNamed {

        @Property(key = 0)
        DObject parent();

        @Override
        @Property(key = 1)
        String name();

        @Property
        String color();

        @Property(optional)
        String wingColor();

        @Property(containment)
        Set<Wing> wings();

        @Property(containment)
        Set<Bird> children();

        @Property
        Set<Bird> orphans();

        String notAProperty();
    }

    interface Wing extends DStruct2<Bird, String>, DNamed {
        @Property(key = 0)
        Bird bird();

        @Override
        @Property(key = 1)
        String name();

        @Property
        default int span() {
            return 1;
        }
    }

    interface Feather extends DStruct2<Bird, Integer>, DObject {
        @Property(key = 0)
        Bird bird();

        @Property(key = 1)
        Integer id();

        @Property
        default String color() {
            return bird().color();
        }
    }

    interface Condor extends Bird {

        @Rule
        default void setColor() {
            if ("white".equals(color())) {
                set(this, Bird::wingColor, "snowy");
                set(this, Bird::wingColor, "ivory");
            }
        }

        @Rule
        default void setColor1() {
            if ("red".equals(color())) {
                set(this, Bird::wingColor, "ruby");
            }
        }

        @Rule
        default void setColor2() {
            if ("red".equals(color())) {
                set(this, Bird::wingColor, "cherry");
            }
        }

        @Rule
        default void addWing() {
            if ("green".equals(color())) {
                Wing wing = dclare(Wing.class, this, "Left");
                set(this, Bird::wings, Set::add, wing);
            }
        }

        @Rule
        default void removeWing() {
            if ("green".equals(color())) {
                Wing wing = dclare(Wing.class, this, "Left");
                set(this, Bird::wings, Set::remove, wing);
            }
        }

        @Rule
        default void addAndRemoveWing() {
            if ("blue".equals(this.color())) {
                Wing wing = dclare(Wing.class, this, "Left");
                set(this, Bird::wings, Set::remove, wing);
                set(this, Bird::wings, Set::add, wing);
            }
        }

        @Rule
        default void increaseSpan() {
            if ("black".equals(this.color())) {
                Wing wing = dclare(Wing.class, this, "Left");
                int span = wing.span();
                set(wing, Wing::span, span + 1);
            }
        }

        @Rule
        default void multiply() {
            if ("yellow".equals(color()) && children().isEmpty() && name().length() < 7) {
                for (int i = 0; i < 7; i++) {
                    Bird child = dclare(Condor.class, this, name() + i);
                    set(this, Bird::children, Set::add, child);
                    set(child, Bird::color, "yellow");
                }
            }
        }
    }

    interface Pigeon extends Bird {

        @Rule
        default void addChildren() {
            if ("grey".equals(color())) {
                for (int i = 0; i < 10000; i++) {
                    Bird child = dclare(Pigeon.class, this, name() + i);
                    set(this, Bird::children, Set::add, child);
                    set(child, Bird::color, "gray");
                }
            }
        }

        @Rule
        default void addGenerations() {
            if ("blue".equals(this.color()) && name().length() < 10000) {
                Bird child = dclare(Pigeon.class, this, name() + "+");
                set(this, Bird::children, Set::add, child);
                set(child, Bird::color, "blue");
            }
        }

        @Rule
        default void addOrphans() {
            if ("green".equals(color())) {
                for (int i = 0; i < 100; i++) {
                    Bird child = dclare(Pigeon.class, this, name() + i);
                    set(this, Bird::orphans, Set::add, child);
                    set(child, Bird::color, "green");
                }
            }
        }
    }

    interface Sparrow extends Bird {

        @Rule
        default void addChildren() {
            if ("black".equals(color())) {
                for (int i = 0; i < 800; i++) {
                    Sparrow child = dclare(Sparrow.class, this, name() + i);
                    set(child, Bird::color, "grey");
                    set(this, Bird::children, Set::add, child);
                    for (int j = 0; j < 0; j++) {
                        Sparrow grandChild = dclare(Sparrow.class, child, name() + j); //
                        // TODO
                        // what happens if you create objects with same identity but properties are not equal?
                        set(grandChild, Bird::color, "gray");
                        set(child, Bird::children, Set::add, grandChild);
                    }
                }
            }
        }

        @Rule
        default void parentColor1() {
            // TODO
            // Note that dclare(Bird.class, dclare(BirdUniverse.class), "0").color(); throws an EmptyMandatoryException.
            // This exception is not propagated to the user - why not?
            String parentColor = dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
            System.err.println("parent color is " + parentColor);
        }

        @Rule
        default void parentColor2() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

        @Rule
        default void parentColor3() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

        @Rule
        default void parentColor4() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

        @Rule
        default void parentColor5() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

        @Rule
        default void parentColor6() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

        @Rule
        default void parentColor7() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

        @Rule
        default void parentColor8() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

        @Rule
        default void parentColor9() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

        @Rule
        default void parentColor10() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

        @Rule
        default void parentColor11() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

        @Rule
        default void parentColor12() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

        @Rule
        default void parentColor13() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

        @Rule
        default void parentColor14() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

        @Rule
        default void parentColor15() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

        @Rule
        default void parentColor16() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

        @Rule
        default void parentColor17() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

        @Rule
        default void parentColor18() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

        @Rule
        default void parentColor19() {
            dclare(Sparrow.class, dclare(BlackSparrowUniverse.class), "0").color();
        }

    }

    interface HummingBird extends Bird {
        @Rule
        default void missingColor() {
            if ("green".equals(color())) {
                Bird child = dclare(HummingBird.class, this, this.name() + "+");
                set(this, Bird::children, Set::add, child);
            }
        }
    }

    @Override
    default void init() {
        DUniverse.super.init();
    }

    static void main(String[] args) {
        DClare.run(BirdUniverse.class);
    }

    public interface RedCondorUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            addBird(Condor.class, "0", "red");
        }
    }

    public interface GreenCondorUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            addBird(Condor.class, "0", "green");
        }
    }

    public interface BlueCondorUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            addBird(Condor.class, "0", "blue");
        }
    }

    public interface WhiteCondorUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            addBird(Condor.class, "0", "white");
        }
    }

    public interface BlackCondorUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            addBird(Condor.class, "0", "black");
        }
    }

    public interface YellowCondorUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            addBird(Condor.class, "0", "yellow");
        }
    }

    public interface GreyPigeonUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            addBird(Pigeon.class, "0", "grey");
        }
    }

    public interface BluePigeonUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            addBird(Pigeon.class, "0", "blue");
        }
    }

    public interface GreenPigeonUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            addBird(Pigeon.class, "0", "green");
        }
    }

    public interface BlackSparrowUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            addBird(Sparrow.class, "0", "black");
        }
    }

    public interface GreenHummingBirdUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            addBird(HummingBird.class, "0", "green");
        }
    }

    public interface MultipleBirdsUniverse extends GreyPigeonUniverse, RedCondorUniverse {

        @Override
        default void init() {
            GreyPigeonUniverse.super.init();
            RedCondorUniverse.super.init();
        }
    }
}
