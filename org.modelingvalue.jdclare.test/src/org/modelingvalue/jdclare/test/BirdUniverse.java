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
                    Bird son = dclare(Condor.class, this, name() + i);
                    set(this, Bird::children, Set::add, son);
                    set(son, Bird::color, "yellow");
                }
            }
        }
    }

    interface Pigeon extends Bird {

        @Rule
        default void addChildren() {
            if ("grey".equals(color())) {
                for (int i = 0; i < 10000; i++) {
                    Bird son = dclare(Pigeon.class, this, name() + i);
                    set(this, Bird::children, Set::add, son);
                }
            }
        }

        @Rule
        default void addGenerations() {
            if ("blue".equals(this.color()) && name().length() < 10000) {
                Bird son = dclare(Pigeon.class, this, name() + "+");
                set(this, Bird::children, Set::add, son);
                set(son, Bird::color, "blue");
            }
        }

        @Rule
        default void addOrphans() {
            if ("green".equals(color())) {
                for (int i = 0; i < 100; i++) {
                    Bird son = dclare(Pigeon.class, this, name() + i);
                    set(this, Bird::orphans, Set::add, son);
                    set(son, Bird::color, "green");
                }
            }
        }
    }

    interface HummingBird extends Bird {
        @Rule
        default void missingColor() {
            if ("green".equals(color())) {
                Bird son = dclare(HummingBird.class, this, this.name() + "+");
                set(this, Bird::children, Set::add, son);
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

    public interface CondorUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            Bird condor = dclare(Condor.class, this, "0");
            set(this, BirdUniverse::birds, Set::add, condor);
        }
    }

    public interface RedCondorUniverse extends CondorUniverse {

        @Override
        default void init() {
            CondorUniverse.super.init();
            set(dclare(Condor.class, this, "0"), Bird::color, "red");
        }
    }

    public interface GreenCondorUniverse extends CondorUniverse {

        @Override
        default void init() {
            CondorUniverse.super.init();
            set(dclare(Condor.class, this, "0"), Bird::color, "green");
        }
    }

    public interface BlueCondorUniverse extends CondorUniverse {

        @Override
        default void init() {
            CondorUniverse.super.init();
            set(dclare(Condor.class, this, "0"), Bird::color, "blue");
        }
    }

    public interface WhiteCondorUniverse extends CondorUniverse {

        @Override
        default void init() {
            CondorUniverse.super.init();
            set(dclare(Condor.class, this, "0"), Bird::color, "white");
        }
    }

    public interface BlackCondorUniverse extends CondorUniverse {

        @Override
        default void init() {
            CondorUniverse.super.init();
            set(dclare(Condor.class, this, "0"), Bird::color, "black");
        }
    }

    public interface YellowCondorUniverse extends CondorUniverse {

        @Override
        default void init() {
            CondorUniverse.super.init();
            set(dclare(Condor.class, this, "0"), Bird::color, "yellow");
        }
    }

    public interface PigeonUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            Bird pigeon = dclare(Pigeon.class, this, "0");
            set(this, BirdUniverse::birds, Set::add, pigeon);
        }
    }

    public interface GreyPigeonUniverse extends PigeonUniverse {

        @Override
        default void init() {
            PigeonUniverse.super.init();
            set(dclare(Pigeon.class, this, "0"), Bird::color, "grey");
        }
    }

    public interface BluePigeonUniverse extends PigeonUniverse {

        @Override
        default void init() {
            PigeonUniverse.super.init();
            set(dclare(Pigeon.class, this, "0"), Bird::color, "blue");
        }
    }

    public interface GreenPigeonUniverse extends PigeonUniverse {

        @Override
        default void init() {
            PigeonUniverse.super.init();
            set(dclare(Pigeon.class, this, "0"), Bird::color, "green");
        }
    }

    public interface HummingBirdUniverse extends BirdUniverse {

        @Property(containment)
        Set<Feather> feathers();

        @Override
        default void init() {
            BirdUniverse.super.init();
            Bird hummingbird = dclare(HummingBird.class, this, "0");
            set(this, BirdUniverse::birds, Set::add, hummingbird);
        }
    }

    public interface GreenHummingBirdUniverse extends HummingBirdUniverse {

        @Override
        default void init() {
            HummingBirdUniverse.super.init();
            set(dclare(HummingBird.class, this, "0"), Bird::color, "green");
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
