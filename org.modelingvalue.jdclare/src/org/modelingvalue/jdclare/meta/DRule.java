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

import org.modelingvalue.collections.Map;
import org.modelingvalue.jdclare.Abstract;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DNamed;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.expressions.DStatement;
import org.modelingvalue.transactions.AbstractLeaf;
import org.modelingvalue.transactions.Direction;
import org.modelingvalue.transactions.Rule;
import org.modelingvalue.transactions.StopObserverException;

@Abstract
public interface DRule<O extends DObject> extends DNamed {

    @Property(containment)
    DStatement<O> statement();

    @Property
    Direction initDirection();

    @SuppressWarnings("unchecked")
    @Property(constant)
    default Rule rule() {
        return Rule.of(this, o -> {
            if (AbstractLeaf.getCurrent().parent().equals(DClare.TRANSACTION.get((O) o))) {
                statement().run((O) o, Map.of());
            } else {
                throw new StopObserverException("Transaction not Current");
            }
        });
    }

    @Property
    boolean validation();

}
