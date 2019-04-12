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

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.lang.reflect.Method;

import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.expressions.DStatement;
import org.modelingvalue.jdclare.meta.DRule;
import org.modelingvalue.transactions.Direction;

public interface JRule<O extends DObject, T> extends DRule<O>, DStruct1<Method> {

    @Property(key = 0)
    Method method();

    @Override
    @Property(constant)
    default String name() {
        Method method = method();
        return method.getDeclaringClass().getSimpleName() + "::" + method.getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    @Property(constant)
    default DStatement<O> statement() {
        Method method = method();
        return method.getReturnType() == Void.TYPE ? DClare.dclare(JStatement.class, method) : DClare.dclare(JEqualize.class, method);
    }

    @Override
    @Property(constant)
    default boolean validation() {
        Method method = method();
        return qual(method, validation);
    }

    @Override
    @Property(constant)
    default Direction initDirection() {
        return method().getReturnType() == Void.TYPE ? Direction.backward : Direction.forward;
    }

}
