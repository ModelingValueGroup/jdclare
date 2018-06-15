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

import java.lang.reflect.Parameter;

import org.modelingvalue.collections.List;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.expressions.DExpression;
import org.modelingvalue.jdclare.meta.DParameter;

public interface JFunction<O, R> extends AFunction<O, R> {

    @SuppressWarnings("unchecked")
    @Override
    @Property(constant)
    default List<DParameter<?>> parameters() {
        return List.<Parameter, DParameter<?>> of(p -> DClare.dclare(JParameter.class, p), method().getParameters());
    }

    @SuppressWarnings("unchecked")
    @Override
    @Property(constant)
    default DExpression<O, R> expression() {
        return DClare.dclare(MExpression.class, method());
    }

}
