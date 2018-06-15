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

import java.util.function.Function;

import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct2;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.expressions.DEqualize;
import org.modelingvalue.jdclare.expressions.DExpression;
import org.modelingvalue.jdclare.meta.DProperty;

public interface FEqualize<O extends DObject, T> extends DEqualize<O, T>, DStruct2<DProperty<O, T>, Function<O, T>> {

    @Property(key = 0)
    DProperty<O, T> property();

    @Property(key = 1)
    Function<O, T> function();

    @Override
    @Property(constant)
    default DProperty<O, T> target() {
        return property();
    }

    @SuppressWarnings("unchecked")
    @Override
    @Property(constant)
    default DExpression<O, T> source() {
        return DClare.dclare(FExpression.class, function());
    }

    @Override
    default String asString() {
        return property().toString();
    }
}
