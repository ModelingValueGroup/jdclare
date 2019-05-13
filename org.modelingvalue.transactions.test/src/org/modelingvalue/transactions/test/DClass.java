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

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.StringUtil;
import org.modelingvalue.transactions.Observer;
import org.modelingvalue.transactions.Setable;

public class DClass {

    @SafeVarargs
    static DClass of(Object id, Observer<? extends DObject>... observers) {
        return new DClass(id, Set.of(), Set.of(observers));
    }

    @SafeVarargs
    static DClass of(Object id, Setable<? extends DObject, ?> container0, Observer<? extends DObject>... observers) {
        return new DClass(id, Set.of(container0), Set.of(observers));
    }

    @SafeVarargs
    static DClass of(Object id, Setable<? extends DObject, ?> container0, Setable<? extends DObject, ?> container1, Observer<? extends DObject>... observers) {
        return new DClass(id, Set.of(container0, container1), Set.of(observers));
    }

    @SafeVarargs
    static DClass of(Object id, Setable<? extends DObject, ?> container0, Setable<? extends DObject, ?> container1, Setable<? extends DObject, ?> container2, Observer<? extends DObject>... observers) {
        return new DClass(id, Set.of(container0, container1, container2), Set.of(observers));
    }

    @SafeVarargs
    static DClass of(Object id, Setable<? extends DObject, ?> container0, Setable<? extends DObject, ?> container1, Setable<? extends DObject, ?> container2, Setable<? extends DObject, ?> container3, Observer<? extends DObject>... observers) {
        return new DClass(id, Set.of(container0, container1, container2, container3), Set.of(observers));
    }

    private final Object                       id;
    private final Set<? extends Observer<?>>   observers;
    private final Set<? extends Setable<?, ?>> containers;

    protected DClass(Object id, Set<? extends Setable<?, ?>> containers, Set<? extends Observer<?>> observers) {
        this.id = id;
        this.containers = containers;
        this.observers = observers;
    }

    public Set<? extends Observer<?>> dObservers() {
        return observers;
    }

    public Collection<? extends Setable<?, ?>> dContainers() {
        return containers;
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
