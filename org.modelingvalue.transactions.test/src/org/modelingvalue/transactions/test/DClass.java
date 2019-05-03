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
import org.modelingvalue.transactions.Mutable;
import org.modelingvalue.transactions.Observer;
import org.modelingvalue.transactions.Setable;

public class DClass {

    @SafeVarargs
    static DClass of(Object id, Observer<DObject>... observers) {
        return new DClass(id, Set.of(), Set.of(observers));
    }

    @SafeVarargs
    static DClass of(Object id, Setable<? extends Mutable, ?>... containers) {
        return new DClass(id, Set.of(containers), Set.of());
    }

    private final Object                             id;
    @SuppressWarnings("rawtypes")
    private final Set<Observer>                      observers;
    private final Set<Setable<? extends Mutable, ?>> containers;

    @SuppressWarnings("rawtypes")
    protected DClass(Object id, Set<Setable<? extends Mutable, ?>> containers, Set<Observer> observers) {
        this.id = id;
        this.containers = containers;
        this.observers = observers;
    }

    @SuppressWarnings("rawtypes")
    public Set<Observer> dObservers() {
        return observers;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Set<Setable<Mutable, ?>> dContainers() {
        return (Set) containers;
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
