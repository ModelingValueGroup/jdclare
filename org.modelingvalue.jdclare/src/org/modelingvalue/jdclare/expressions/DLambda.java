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

package org.modelingvalue.jdclare.expressions;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.util.function.Function;

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.jdclare.DFunctionObject;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.meta.DParameter;
import org.modelingvalue.jdclare.meta.DParameterized;

public interface DLambda<O, T> extends DExpression<O, DFunctionObject<T>>, DParameterized<O> {
    @Property(containment)
    DExpression<O, T> value();

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    default DFunctionObject<T> run(O self, Map<DParameter<?>, ?> params) {
        Function<List<?>, T> function = ps -> {
            Function<DParameter<?>, Entry<DParameter<?>, ?>> dParameterEntryFunction = p -> Entry.of(p, ps.get(p.nr()));
            return (T) value().run(self, params.putAll(parameters().toMap((Function) dParameterEntryFunction)));
        };
        return dclare(DFunctionObject.class, id(function, this, self, params));
    }
}
