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

import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.collections.Map;
import org.modelingvalue.jdclare.Abstract;
import org.modelingvalue.jdclare.DStruct;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.meta.DParameter;
import org.modelingvalue.jdclare.meta.DProperty;
import org.modelingvalue.transactions.EmptyMandatoryException;

@Abstract
public interface DEqualize<O extends DStruct, T> extends DStatement<O> {

    @Property
    DProperty<O, T> target();

    @Property(containment)
    DExpression<O, T> source();

    @Override
    default void run(O dObject, Map<DParameter<?>, ?> params) {
        DProperty<O, T> property = target();
        T value = property.defaultValue();
        try {
            value = source().run(dObject, params);
        } catch (EmptyMandatoryException ooe) {
        }
        property.set(dObject, value);
    }

}
