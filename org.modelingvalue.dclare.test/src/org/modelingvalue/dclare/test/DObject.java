package org.modelingvalue.dclare.test;

import org.modelingvalue.collections.util.StringUtil;
import org.modelingvalue.dclare.Mutable;

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

    @Override
    public DClass dClass() {
        return dClass;
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
