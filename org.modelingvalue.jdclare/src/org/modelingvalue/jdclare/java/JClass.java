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

package org.modelingvalue.jdclare.java;

import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.Abstract;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.meta.DClass;
import org.modelingvalue.jdclare.meta.DConstraint;
import org.modelingvalue.jdclare.meta.DFunction;
import org.modelingvalue.jdclare.meta.DTypeParameter;
import org.modelingvalue.jdclare.types.DClassReference;

public interface JClass<T> extends DClass<T>, DStruct1<Class<?>> {

    @Override
    @Property(key = 0)
    Class<T> jClass();

    @Override
    @Property(constant)
    default String name() {
        return jClass().getSimpleName();
    }

    @Override
    @Property(constant)
    default List<DTypeParameter> typeParameters() {
        return DClare.dTypeParameters(jClass());
    }

    @Override
    @Property(constant)
    default Set<DClassReference> supers() {
        return DClare.dSupers(jClass());
    }

    @Override
    @Property(constant)
    default Set<DFunction<T, ?>> functions() {
        return DClare.dFunctions(jClass());
    }

    @Override
    @Property(constant)
    default Set<DConstraint<T>> constraints() {
        return DClare.dConstraints(jClass());
    }

    @Override
    @Property(constant)
    default Set<DClass<?>> classes() {
        return DClare.dInnerClasses(jClass());
    }

    @Override
    @Property(constant)
    default boolean isAbstract() {
        return jClass().getAnnotation(Abstract.class) != null;
    }

}
