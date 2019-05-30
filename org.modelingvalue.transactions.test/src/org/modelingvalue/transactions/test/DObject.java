package org.modelingvalue.transactions.test;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.util.StringUtil;
import org.modelingvalue.transactions.Constant;
import org.modelingvalue.transactions.Mutable;
import org.modelingvalue.transactions.Observer;
import org.modelingvalue.transactions.Setable;

public class DObject implements Mutable {
    private final Object id;
    private final DClass dClass;

    public static DObject of(Object id, DClass dClass) {
        return new DObject(id, dClass);
    }

    protected DObject(Object id, DClass dClass) {
        this.id = id;
        this.dClass = dClass;
    }

    public DClass dClass() {
        return dClass;
    }

    @Override
    public Collection<? extends Observer<?>> dObservers() {
        return dClass.dObservers();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<? extends Setable<Mutable, ?>> dContainers() {
        return (Collection) dClass.dContainers();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<? extends Constant<? extends Mutable, ?>> dConstants() {
        return (Collection) dClass.dConstants();
    }

    public Object id() {
        return id;
    }

    @Override
    public String toString() {
        return dClass + "@" + StringUtil.toString(id);
    }

    @Override
    public int hashCode() {
        return dClass.hashCode() ^ id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        } else {
            DObject c = (DObject) obj;
            return id.equals(c.id) && dClass.equals(c.dClass);
        }
    }

}
