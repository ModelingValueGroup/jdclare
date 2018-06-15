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

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.ContainingCollection;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.Property;

public interface DObjectClass<T extends DObject> extends DStructClass<T> {

    @Property(containment)
    Set<DRule<T>> rules();

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Property
    default Set<DRule> allRules() {
        return allSupers().filter(DObjectClass.class).<DRule> flatMap(s -> s.rules()).toSet();
    }

    @Property
    default Set<DProperty<T, ?>> allContainments() {
        return allProperties().filter(DProperty::containment).toSet();
    }

    @Property
    default Set<DProperty<T, ?>> allConstantContainments() {
        return allContainments().filter(DProperty::constant).toSet();
    }

    @Property
    default Set<DProperty<T, ?>> allValidations() {
        return allProperties().filter(DProperty::validation).toSet();
    }

    @Property
    default Set<DProperty<T, ?>> allNonContainments() {
        return allProperties().filter(p -> !p.containment()).toSet();
    }

    @Property
    default Map<DProperty<T, ?>, DFeature<T, Collection<?>>> scopedProperties() {
        return allProperties().map(p -> {
            DFeature<T, Collection<?>> scope = p.scopeProperty();
            return scope != null ? Entry.<DProperty<T, ?>, DFeature<T, Collection<?>>> of(p, scope) : null;
        }).notNull().toMap(e -> e);
    }

    @Property
    default Set<DProperty<T, ?>> mandatoryProperties() {
        return allProperties().filter(p -> !p.constant() && p.mandatory() && //
                (p.defaultValue() == null || p.defaultValue() instanceof ContainingCollection)).toSet();
    }

}
