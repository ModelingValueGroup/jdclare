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
        default void setWingColor() {
            if ("white".equals(color())) {
                set(this, Bird::wingColor, "snowy");
                set(this, Bird::wingColor, "ivory");
            }
        }

        @Rule
        default void setWingColor1() {
            if ("red".equals(color())) {
                set(this, Bird::wingColor, "ruby");
            }
        }

        @Rule
        default void setWingColor2() {
            if ("red".equals(color())) {
                set(this, Bird::wingColor, "cherry");
            }
        }

        @Rule
        default void addLeftWing() {
            if ("green".equals(color())) {
                Wing wing = dclare(Wing.class, this, "Left");
                set(this, Bird::wings, Set::add, wing);
            }
        }

        @Rule
        default void removeLeftWing() {
            if ("green".equals(color())) {
                Wing wing = dclare(Wing.class, this, "Left");
                set(this, Bird::wings, Set::remove, wing);
            }
        }

        @Rule
        default void addAndRemoveLeftWing() {
            if ("blue".equals(this.color())) {
                Wing wing = dclare(Wing.class, this, "Left");
                set(this, Bird::wings, Set::remove, wing);
                set(this, Bird::wings, Set::add, wing);
            }
        }

        @Rule
        default void increaseLeftWingSpan() {
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

        @Rule
        default void addGenerations() {
            if ("brown".equals(this.color()) && name().length() < 10000) {
                Bird child = dclare(Condor.class, this, name() + "+");
                set(this, Bird::children, Set::add, child);
                set(child, Bird::color, "brown");
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
        default void addOrphans() {
            if ("green".equals(color())) {
                for (int i = 0; i < 100; i++) {
                    Bird orphan = dclare(Pigeon.class, this, name() + i);
                    set(this, Bird::orphans, Set::add, orphan);
                    set(orphan, Bird::color, "green");
                }
                set(this, Bird::color, "notGreen");
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
                        // what happens if you create objects with same identity but properties are not
                        // equal?
                        set(grandChild, Bird::color, "gray");
                        set(child, Bird::children, Set::add, grandChild);
                    }
                }
            }
        }

        @Rule
        default void setWingColor1() {
            // TODO
            //Bird parent = dclare(Bird.class, dclare(BirdUniverse.class), "0");// throws an EmptyMandatoryException.
            // This exception is not propagated to the user - why not?
            Bird parent = dclare(Sparrow.class, dclare(BirdUniverse.class), "0");
            if (parent.color().equals("black")) {
                set(this, Bird::wingColor, parent.color());
                set(parent, Bird::color, "ebony");
            }
        }

        @Rule
        default void setWingColor2() {
            Bird parent = dclare(Sparrow.class, dclare(BirdUniverse.class), "0");
            if (parent.color().equals("black")) {
                set(this, Bird::wingColor, parent.color());
                set(parent, Bird::color, "ebony");
            }
        }

        @Rule
        default void setWingColor3() {
            Bird parent = dclare(Sparrow.class, dclare(BirdUniverse.class), "0");
            if (parent.color().equals("black")) {
                set(this, Bird::wingColor, parent.color());
                set(parent, Bird::color, "ebony");
            }
        }

        @Rule
        default void setWingColor4() {
            Bird parent = dclare(Sparrow.class, dclare(BirdUniverse.class), "0");
            if (parent.color().equals("black")) {
                set(this, Bird::wingColor, parent.color());
                set(parent, Bird::color, "ebony");
            }
        }

        @Rule
        default void setWingColor5() {
            Bird parent = dclare(Sparrow.class, dclare(BirdUniverse.class), "0");
            if (parent.color().equals("black")) {
                set(this, Bird::wingColor, parent.color());
                set(parent, Bird::color, "ebony");
            }
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

        @SuppressWarnings("null")
        @Rule
        default void nullPointerInMandatoryProperty() {
            if ("blue".equals(color())) {
                Bird child = dclare(HummingBird.class, this, this.name() + "+");
                set(child, Bird::color, null);
                String color = child.color();
                if (color == null) {
                    System.err.println(color.length());
                }
            }
        }

        @SuppressWarnings("null")
        @Rule
        default void nullPointerInOptionalProperty() {
            if ("yellow".equals(color())) {
                Bird child = dclare(HummingBird.class, this, this.name() + "+");
                set(child, Bird::wingColor, null);
                String wingColor = child.wingColor();
                if (wingColor == null) {
                    System.err.println(wingColor.length());
                }
            }
        }

        @Rule
        default void divisionByZero() {
            if ("red".equals(color())) {
                Wing wing = dclare(Wing.class, this, "Left");
                int span = wing.span();
                set(wing, Wing::span, span / 0);
            }
        }
    }

    interface Pheasant extends Bird {

        @Property(constant)
        String tailColor();

        @Property(constant)
        String headColor();

        @Property(constant)
        default String legColor() {
            return "";
        }

        @Property(constant)
        default String leftLegColor() {
            return "";
        }

        @Property(constant)
        default String rightLegColor() {
            return "";
        }

        @Rule
        default void setColor() {
            if ("yellow".equals(color())) {
                set(this, Bird::color, "gold");
            }
        }

        @Rule
        default void setTailColor1() {
            if ("blue".equals(color())) {
                set(this, Pheasant::tailColor, "cobalt");
            }
        }

        @Rule
        default void setTailColor2() {
            set(this, Pheasant::tailColor, color());
        }

        @Rule
        default void setHeadColor1() {
            if ("red".equals(color())) {
                set(this, Pheasant::headColor, headColor());
            }
        }

        @Rule
        default void setLegColor() {
            if ("black".equals(color())) {
                set(this, Pheasant::legColor, "black");
            }
        }

        @Rule
        default void setLegColorCircular1() {
            if ("white".equals(color())) {
                set(this, Pheasant::rightLegColor, leftLegColor());
            }
        }

        @Rule
        default void setLegColorCircular2() {
            if ("white".equals(color())) {
                set(this, Pheasant::leftLegColor, rightLegColor());
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

}
