package org.modelingvalue.jdclare;

import static org.modelingvalue.jdclare.DClare.*;

import java.time.Duration;
import java.time.Instant;

public interface DClock extends DObject, DStruct0 {

    @Property
    @Default
    default Instant time() {
        return dClare().getClock().instant();
    }

    @Property
    default Duration passTime() {
        return Duration.between(pre(this, DClock::time), time());
    }

    @Property
    default double passSeconds() {
        return passTime().toNanos() / 1000000000.0;
    }

}
