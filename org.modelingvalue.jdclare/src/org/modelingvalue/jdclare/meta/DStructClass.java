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

import java.lang.reflect.Method;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.Abstract;
import org.modelingvalue.jdclare.Constraints;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DNamed;
import org.modelingvalue.jdclare.DStruct;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.Property;

public interface DStructClass<T extends DStruct> extends DClassContainer, DStruct1<Class<?>> {

    @SuppressWarnings("rawtypes")
    @Constraints
    private void metaRelations() {
        DClare.<DStructClass, DStructClass, Object, Object> OPPOSITE(DStructClass::allSupers, DStructClass::allSubs);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Property
    default Set<DStructClass> allSupers() {
        return Collection.concat(this, supers().flatMap(DStructClass::allSupers)).toSet();
    }

    @SuppressWarnings("rawtypes")
    @Property
    default List<DStructClass> sortedSupers() {
        return allSupers().sorted((a, b) -> a.equals(b) ? 0 : a.isSubOf(b) ? -1 : a.isSuperOf(b) ? 1 : 0).toList();
    }

    @SuppressWarnings("rawtypes")
    @Property
    Set<DStructClass> allSubs();

    @SuppressWarnings("rawtypes")
    default boolean isSubOf(DStructClass sup) {
        return allSupers().contains(sup);
    }

    @SuppressWarnings("rawtypes")
    default boolean isSuperOf(DStructClass sub) {
        return allSubs().contains(sub);
    }

    @Property(key = 0)
    Class<T> jClass();

    @Override
    @Property(constant)
    default String name() {
        return jClass().getSimpleName();
    }

    @SuppressWarnings("rawtypes")
    @Property(constant)
    default Set<DStructClass> supers() {
        return DClare.dSupers(jClass());
    }

    @Override
    @Property(constant)
    default Set<DStructClass<?>> classes() {
        return DClare.dInnerClasses(jClass());
    }

    @Property(constant)
    default boolean isAbstract() {
        return jClass().getAnnotation(Abstract.class) != null;
    }

    @SuppressWarnings("unchecked")
    @Property
    default QualifiedSet<String, DProperty<T, ?>> allProperties() {
        QualifiedSet<String, DProperty<T, ?>> result = QualifiedSet.of(DNamed::name);
        for (DStructClass<T> cls : sortedSupers()) {
            if (cls instanceof DStructClass) {
                result = result.addAll(cls.properties().map(p -> p.actualize(this)));
            }
        }
        return result;
    }

    @Property
    default List<DProperty<T, ?>> keys() {
        return allProperties().filter(p -> p.key()).sorted((a, b) -> ((Integer) a.keyNr()).compareTo(b.keyNr())).toList();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Property({constant, containment})
    default Set<DProperty<T, ?>> properties() {
        Set<DProperty<T, ?>> properties = Set.of();
        for (Method method : jClass().getDeclaredMethods()) {
            DProperty p = DClare.dProperty(method);
            if (p != null) {
                properties = properties.add(p);
            }
        }
        return properties;
    }

}
