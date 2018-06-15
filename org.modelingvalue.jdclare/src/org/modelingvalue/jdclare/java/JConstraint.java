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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.modelingvalue.collections.List;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DClare.Handle;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.expressions.DStatement;
import org.modelingvalue.jdclare.meta.DConstraint;
import org.modelingvalue.jdclare.meta.DParameter;
import org.modelingvalue.jdclare.meta.DTypeParameter;
import org.modelingvalue.jdclare.meta.DTyped;
import org.modelingvalue.jdclare.types.DConstraintType;
import org.modelingvalue.jdclare.types.DType;

public interface JConstraint<O> extends DConstraint<O>, DStruct1<Method> {

    @Property(key = 0)
    Method method();

    @Override
    @Property(constant)
    default DType type() {
        return DClare.dclare(DConstraintType.class, parameters().map(DTyped::type).toList());
    }

    @Override
    @Property(constant)
    default List<DTypeParameter> typeParameters() {
        return DClare.dTypeParameters(method());
    }

    @Override
    @Property(constant)
    default String name() {
        return method().getName();
    }

    @Override
    @Property(constant)
    default boolean isAbstract() {
        Handle n = DClare.HANDLE.get(method());
        return n.handle == null;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Property(constant)
    default List<DParameter<?>> parameters() {
        return List.<Parameter, DParameter<?>> of(p -> DClare.dclare(JParameter.class, p), method().getParameters());
    }

    @SuppressWarnings("unchecked")
    @Override
    @Property(constant)
    default DStatement<O> statement() {
        return DClare.dclare(JStatement.class, method());
    }

}
