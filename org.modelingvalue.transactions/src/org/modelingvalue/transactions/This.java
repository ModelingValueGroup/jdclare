package org.modelingvalue.transactions;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Set;

public final class This implements Mutable {

    This() {
    }

    @Override
    public String toString() {
        return "<this>";
    }

    @Override
    public Collection<? extends Observer<?>> dObservers() {
        return Set.of();
    }

    @Override
    public Collection<? extends Setable<? extends Mutable, ?>> dContainers() {
        return Set.of();
    }

    @Override
    public Collection<? extends Constant<? extends Mutable, ?>> dConstants() {
        return Set.of();
    }

}
