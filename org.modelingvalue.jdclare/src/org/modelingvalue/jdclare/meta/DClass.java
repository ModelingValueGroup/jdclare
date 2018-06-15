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

import java.util.function.Function;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.Abstract;
import org.modelingvalue.jdclare.Constraints;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.types.DClassReference;

@Abstract
public interface DClass<T> extends DGeneric, DClassContainer {

    @SuppressWarnings("rawtypes")
    @Constraints
    private void metaRelations() {
        DClare.<DClass, DClass, Object, Object> OPPOSITE(DClass::allSupers, DClass::allSubs);
    }

    @Property
    Set<DClassReference> supers();

    @SuppressWarnings("rawtypes")
    @Property
    default Set<DClass> allSupers() {
        return Collection.concat(this, supers().map(DClassReference::referenced).flatMap(DClass::allSupers)).toSet();
    }

    @SuppressWarnings("rawtypes")
    @Property
    default List<DClass> sortedSupers() {
        return allSupers().sorted((a, b) -> a.equals(b) ? 0 : a.isSubOf(b) ? -1 : a.isSuperOf(b) ? 1 : 0).toList();
    }

    @SuppressWarnings("rawtypes")
    @Property
    Set<DClass> allSubs();

    @SuppressWarnings("rawtypes")
    default boolean isSubOf(DClass sup) {
        return allSupers().contains(sup);
    }

    @SuppressWarnings("rawtypes")
    default boolean isSuperOf(DClass sub) {
        return allSubs().contains(sub);
    }

    @Property(containment)
    Set<DFunction<T, ?>> functions();

    @Property(containment)
    Set<DConstraint<T>> constraints();

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Property
    default Set<DFunction<T, ?>> allFunctions() {
        return allSupers().flatMap((Function<DClass, Set<? extends DFunction<T, ?>>>) s -> s.functions()).toSet();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Property
    default Set<DConstraint<T>> allConstraints() {
        return allSupers().flatMap((Function<DClass, Set<? extends DConstraint<T>>>) s -> s.constraints()).toSet();
    }

    @Property
    Class<T> jClass();

    @Property
    boolean isAbstract();

}
