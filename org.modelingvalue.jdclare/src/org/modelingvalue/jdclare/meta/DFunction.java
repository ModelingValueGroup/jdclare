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

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.util.function.Function;

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.List;
import org.modelingvalue.jdclare.Abstract;
import org.modelingvalue.jdclare.DFunctionObject;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.expressions.DExpression;

@Abstract
public interface DFunction<O, R> extends DFeature<O, DFunctionObject<R>>, DGeneric, DParameterized<O> {

    @Property(containment)
    DExpression<O, R> expression();

    @Property
    DOperator operator();

    @SuppressWarnings("unchecked")
    @Override
    default DFunctionObject<R> get(O object) {
        Function<List<?>, R> function = ps -> expression().run(object, parameters().toMap(p -> Entry.of(p, ps.get(p.nr()))));
        return dclare(DFunctionObject.class, id(function, this, object));
    }

}
