package org.modelingvalue.jdclare;

import static org.modelingvalue.jdclare.DClare.*;

import java.time.Duration;
import java.time.Instant;

public interface DClock extends DObject, DStruct0 {

    final double BILLION = 1000000000.0;

    @Property
    @Default
    default Instant time() {
        return dClare().getClock().instant();
    }

    @Property
    @Default
    default Instant setTime() {
        return dClare().getClock().instant();
    }

    @Property
    default Duration passTime() {
        return Duration.between(pre(this, DClock::time), time());
    }

    @Property
    default double passSeconds() {
        return passTime().toNanos() / BILLION;
    }

}
