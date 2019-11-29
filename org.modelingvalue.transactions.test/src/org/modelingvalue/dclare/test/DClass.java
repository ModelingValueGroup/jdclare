//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018 Modeling Value Group B.V. (http://modelingvalue.org)                                             ~
//                                                                                                                     ~
// Licensed under the GNU Lesser General Public License v3.0 (the "License"). You may not use this file except in      ~
// compliance with the License. You may obtain a copy of the License at: https://choosealicense.com/licenses/lgpl-3.0  ~
// Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on ~
// an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the  ~
// specific language governing permissions and limitations under the License.                                          ~
//                                                                                                                     ~
// Contributors:                                                                                                       ~
//     Wim Bast, Carel Bast, Tom Brus, Arjan Kok, Ronald Krijgsheld                                                    ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package org.modelingvalue.dclare.test;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.StringUtil;
import org.modelingvalue.dclare.Constant;
import org.modelingvalue.dclare.Mutable;
import org.modelingvalue.dclare.MutableClass;
import org.modelingvalue.dclare.Observer;
import org.modelingvalue.dclare.Setable;

public class DClass implements MutableClass {

    @SafeVarargs
    static DClass of(Object id, Observer<? extends DObject>... observers) {
        return new DClass(id, Set.of(), Set.of(observers));
    }

    @SafeVarargs
    static DClass of(Object id, Setable<? extends DObject, ?> setable, Observer<? extends DObject>... observers) {
        return new DClass(id, Set.of(setable), Set.of(observers));
    }

    @SafeVarargs
    static DClass of(Object id, Setable<? extends DObject, ?> setable0, Setable<? extends DObject, ?> setable1, Observer<? extends DObject>... observers) {
        return new DClass(id, Set.of(setable0, setable1), Set.of(observers));
    }

    @SafeVarargs
    static DClass of(Object id, Setable<? extends DObject, ?> setable0, Setable<? extends DObject, ?> setable1, Setable<? extends DObject, ?> setable2, Observer<? extends DObject>... observers) {
        return new DClass(id, Set.of(setable0, setable1, setable2), Set.of(observers));
    }

    @SafeVarargs
    static DClass of(Object id, Setable<? extends DObject, ?> setable0, Setable<? extends DObject, ?> setable1, Setable<? extends DObject, ?> setable2, Setable<? extends DObject, ?> setable3, Observer<? extends DObject>... observers) {
        return new DClass(id, Set.of(setable0, setable1, setable2, setable3), Set.of(observers));
    }

    private final Object                        id;
    private final Set<? extends Observer<?>>    observers;
    private final Set<? extends Setable<?, ?>>  containers;
    private final Set<? extends Constant<?, ?>> constants;
    private final Set<? extends Setable<?, ?>>  setables;

    @SuppressWarnings("unchecked")
    protected DClass(Object id, Set<? extends Setable<?, ?>> setables, Set<? extends Observer<?>> observers) {
        this.id = id;
        this.setables = setables;
        this.containers = setables.filter(s -> s.containment()).toSet();
        this.observers = observers;
        this.constants = (Set<? extends Constant<?, ?>>) setables.filter(s -> s instanceof Constant).toSet();
    }

    @Override
    public Set<? extends Observer<?>> dObservers() {
        return observers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<? extends Setable<? extends Mutable, ?>> dContainers() {
        return (Set<? extends Setable<? extends Mutable, ?>>) containers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<? extends Setable<? extends Mutable, ?>> dSetables() {
        return (Set<? extends Setable<? extends Mutable, ?>>) setables;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<? extends Constant<? extends Mutable, ?>> dConstants() {
        return (Set<? extends Constant<? extends Mutable, ?>>) constants;
    }

    @Override
    public String toString() {
        return StringUtil.toString(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
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
            DClass c = (DClass) obj;
            return id.equals(c.id);
        }
    }

}
