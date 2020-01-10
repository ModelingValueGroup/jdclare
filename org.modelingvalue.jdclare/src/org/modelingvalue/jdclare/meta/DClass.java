//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018-2019 Modeling Value Group B.V. (http://modelingvalue.org)                                        ~
//                                                                                                                     ~
// Licensed under the GNU Lesser General Public License v3.0 (the 'License'). You may not use this file except in      ~
// compliance with the License. You may obtain a copy of the License at: https://choosealicense.com/licenses/lgpl-3.0  ~
// Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on ~
// an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the  ~
// specific language governing permissions and limitations under the License.                                          ~
//                                                                                                                     ~
// Maintainers:                                                                                                        ~
//     Wim Bast, Tom Brus, Ronald Krijgsheld                                                                           ~
// Contributors:                                                                                                       ~
//     Arjan Kok, Carel Bast                                                                                           ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package org.modelingvalue.jdclare.meta;

import org.modelingvalue.collections.*;
import org.modelingvalue.dclare.*;
import org.modelingvalue.jdclare.*;

import java.lang.reflect.*;

import static org.modelingvalue.jdclare.PropertyQualifier.*;

public interface DClass<T extends DObject> extends DStructClass<T>, MutableClass {

    @SuppressWarnings({"rawtypes"})
    @Property(constant)
    default Set<DRule> allRules() {
        return allSupers().filter(DClass.class). <DRule>flatMap(DClass::rules).toSet();
    }

    @Property(constant)
    default Set<DProperty<T, ?>> allContainments() {
        return allProperties().filter(DProperty::containment).toSet();
    }

    @Property(constant)
    default Set<DProperty<T, ?>> allConstants() {
        return allProperties().filter(DProperty::constant).toSet();
    }

    @Property(constant)
    default Set<DProperty<T, ?>> allValidations() {
        return allProperties().filter(DProperty::validation).toSet();
    }

    @Property(constant)
    default Set<DProperty<T, ?>> allNonContainments() {
        return allProperties().filter(p -> !p.containment()).toSet();
    }

    @Property(constant)
    default Map<DProperty<T, ?>, DProperty<T, Set<?>>> scopedProperties() {
        return allProperties().map(p -> {
            DProperty<T, Set<?>> scope = p.scopeProperty();
            return scope != null ? Entry.<DProperty<T, ?>, DProperty<T, Set<?>>> of(p, scope) : null;
        }).notNull().toMap(e -> e);
    }

    @Property(constant)
    default Set<DProperty<T, ?>> mandatoryProperties() {
        return allProperties().filter(p -> !p.constant() && p.mandatory() && //
                (p.defaultValue() == null || p.defaultValue() instanceof ContainingCollection)).toSet();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Property({constant, containment})
    default Set<DRule<T>> rules() {
        Set<DRule<T>> rules = Set.of();
        for (Method method : jClass().getDeclaredMethods()) {
            DMethodRule r = DClare.RULE.get(method);
            if (r != null) {
                rules = rules.add(r);
            }
        }
        return rules;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    default Collection<? extends Observer<?>> dObservers() {
        return allRules().map(DRule::observer);
    }

    @SuppressWarnings("unchecked")
    @Override
    default Collection<? extends Setable<? extends Mutable, ?>> dSetables() {
        return allProperties().map(DClare::getable).filter(Setable.class).map(s -> (Setable <? extends Mutable, ?>) s);
    }

}
