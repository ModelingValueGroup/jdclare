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

package org.modelingvalue.jdclare.meta;

import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DNamed;
import org.modelingvalue.jdclare.DStruct;
import org.modelingvalue.jdclare.Property;

public interface DStructClass<T extends DStruct> extends DClass<T> {

    @Property(containment)
    Set<DProperty<T, ?>> properties();

    @SuppressWarnings("unchecked")
    @Property
    default QualifiedSet<String, DProperty<T, ?>> allProperties() {
        QualifiedSet<String, DProperty<T, ?>> result = QualifiedSet.of(DNamed::name);
        for (DClass<T> cls : sortedSupers()) {
            if (cls instanceof DStructClass) {
                result = result.addAll(((DStructClass<T>) cls).properties().map(p -> p.actualize(this)));
            }
        }
        return result;
    }

    @Property
    default List<DProperty<T, ?>> keys() {
        return allProperties().filter(p -> p.key()).sorted((a, b) -> ((Integer) a.keyNr()).compareTo(b.keyNr())).toList();
    }

}
