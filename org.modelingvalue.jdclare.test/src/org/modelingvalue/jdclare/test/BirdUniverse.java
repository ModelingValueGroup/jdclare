package org.modelingvalue.jdclare.test;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DNamed;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct2;
import org.modelingvalue.jdclare.DUniverse;
import org.modelingvalue.jdclare.Default;
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

        @Property()
        @Default()
        default String wingColor() {
            return "";
        }

        @Property(containment)
        Set<Wing> wings();

        @Property(containment)
        Set<Bird> children();

        @Rule
        default void multiply() {
            if ("yellow".equals(color()) && children().isEmpty()) {
                for (int i = 0; i < 1000; i++) {
                    Bird son = dclare(Bird.class, this, "Son" + i);
                    set(this, Bird::children, (s, b) -> s.add(b), son);
                    set(son, Bird::color, "yellow");
                }
            }
        }

        @Rule
        default void change() {
            if ("yellow".equals(color()) && wingColor().length() < 10) {
                set(this, Bird::wingColor, wingColor() + ".");
            }
        }
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

    @Rule
    default void setCondorColor() {
        Bird condor = dclare(Bird.class, this, "Condor");
        if ("white".equals(condor.color())) {
            set(condor, Bird::wingColor, "orange");
            set(condor, Bird::wingColor, "purple");
        }
    }

    @Rule
    default void setCondorColor1() {
        Bird condor = dclare(Bird.class, this, "Condor");
        if ("red".equals(condor.color())) {
            set(condor, Bird::wingColor, "orange");
        }
    }

    @Rule
    default void setCondorColor2() {
        Bird condor = dclare(Bird.class, this, "Condor");
        if ("red".equals(condor.color())) {
            set(condor, Bird::wingColor, "purple");
        }
    }

    @Rule
    default void addWing() {
        Bird condor = dclare(Bird.class, this, "Condor");
        if ("green".equals(condor.color())) {
            Wing wing = dclare(Wing.class, condor, "Left");
            set(condor, Bird::wings, Set::add, wing);
        }
    }

    @Rule
    default void removeWing() {
        Bird condor = dclare(Bird.class, this, "Condor");
        if ("green".equals(condor.color())) {
            Wing wing = dclare(Wing.class, condor, "Left");
            set(condor, Bird::wings, Set::remove, wing);
        }
    }

    @Rule
    default void addAndRemoveWing() {
        Bird condor = dclare(Bird.class, this, "Condor");
        if ("blue".equals(condor.color())) {
            Wing wing = dclare(Wing.class, condor, "Left");
            set(condor, Bird::wings, Set::remove, wing);
            set(condor, Bird::wings, Set::add, wing);
        }
    }

    @Rule
    default void increaseSpan() {
        Bird condor = dclare(Bird.class, this, "Condor");
        if ("black".equals(condor.color())) {
            Wing wing = dclare(Wing.class, condor, "Left");
            int span = wing.span();
            set(wing, Wing::span, span + 1);
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
            Bird condor = dclare(Bird.class, this, "Condor");
            set(this, BirdUniverse::birds, (s, b) -> s.add(b), condor);
            set(condor, Bird::color, "red");
        }

    }

    public interface GreenCondorUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            Bird condor = dclare(Bird.class, this, "Condor");
            set(this, BirdUniverse::birds, (s, b) -> s.add(b), condor);
            set(condor, Bird::color, "green");
        }
    }

    public interface BlueCondorUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            Bird condor = dclare(Bird.class, this, "Condor");
            set(this, BirdUniverse::birds, (s, b) -> s.add(b), condor);
            set(condor, Bird::color, "blue");
        }
    }

    public interface WhiteCondorUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            Bird condor = dclare(Bird.class, this, "Condor");
            set(this, BirdUniverse::birds, (s, b) -> s.add(b), condor);
            set(condor, Bird::color, "white");
        }
    }

    public interface BlackCondorUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            Bird condor = dclare(Bird.class, this, "Condor");
            set(this, BirdUniverse::birds, (s, b) -> s.add(b), condor);
            set(condor, Bird::color, "black");
        }
    }

    public interface YellowCondorUniverse extends BirdUniverse {

        @Override
        default void init() {
            BirdUniverse.super.init();
            Bird condor = dclare(Bird.class, this, "0");
            set(this, BirdUniverse::birds, (s, b) -> s.add(b), condor);
            set(condor, Bird::color, "yellow");
        }
    }
}
