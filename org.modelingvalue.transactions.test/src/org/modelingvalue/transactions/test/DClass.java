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

package org.modelingvalue.transactions.test;

import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.StringUtil;
import org.modelingvalue.transactions.Constant;
import org.modelingvalue.transactions.Observer;
import org.modelingvalue.transactions.Setable;

public class DClass {

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

    @SuppressWarnings("unchecked")
    protected DClass(Object id, Set<? extends Setable<?, ?>> setables, Set<? extends Observer<?>> observers) {
        this.id = id;
        this.containers = setables.filter(s -> s.containment()).toSet();
        this.observers = observers;
        this.constants = (Set<? extends Constant<?, ?>>) setables.filter(s -> s instanceof Constant).toSet();
    }

    public Set<? extends Observer<?>> dObservers() {
        return observers;
    }

    public Set<? extends Setable<?, ?>> dContainers() {
        return containers;
    }

    public Set<? extends Constant<?, ?>> dConstants() {
        return constants;
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
