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

import org.modelingvalue.collections.List;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.Operator;
import org.modelingvalue.jdclare.Precedence;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.meta.DFunction;
import org.modelingvalue.jdclare.meta.DOperator;
import org.modelingvalue.jdclare.meta.DTypeParameter;
import org.modelingvalue.jdclare.meta.DTyped;
import org.modelingvalue.jdclare.types.DFunctionType;
import org.modelingvalue.jdclare.types.DType;

public interface AFunction<O, R> extends DFunction<O, R>, DStruct1<Method> {

    @Property(key = 0)
    Method method();

    @Override
    @Property(constant)
    default DType type() {
        Method method = method();
        return DClare.dclare(DFunctionType.class, DClare.dType(method.getGenericReturnType()), parameters().map(DTyped::type).toList());
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
        DClare.Handle n = DClare.HANDLE.get(method());
        return n.handle == null;
    }

    @Override
    @Property(constant)
    default DOperator operator() {
        Method method = method();
        Operator operator = DClare.ann(method, Operator.class);
        if (operator != null) {
            Precedence presedence = DClare.ann(method, Precedence.class);
            if (presedence == null) {
                throw new Error("Precedence for " + method + " not defined");
            }
            return DClare.dclare(DOperator.class, List.of(operator.value()), presedence.value());
        } else {
            return null;
        }
    }

}
