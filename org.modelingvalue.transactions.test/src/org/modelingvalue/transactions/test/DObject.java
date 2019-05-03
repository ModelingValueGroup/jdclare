package org.modelingvalue.transactions.test;

import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.StringUtil;
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

    @SuppressWarnings("rawtypes")
    @Override
    public Set<Observer> dObservers() {
        return dClass.dObservers();
    }

    @Override
    public Set<Setable<Mutable, ?>> dContainers() {
        return dClass.dContainers();
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
