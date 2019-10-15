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

package org.modelingvalue.jdclare.syntax.test.simple;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.Constraints;
import org.modelingvalue.jdclare.DProblem;
import org.modelingvalue.jdclare.DSeverity;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.syntax.test.types.AClassType;
import org.modelingvalue.jdclare.syntax.test.types.ANumberType;
import org.modelingvalue.jdclare.syntax.test.types.AStringType;
import org.modelingvalue.jdclare.syntax.test.types.AType;

public interface AField extends ANamed, ATyped {

    @Property({containment, optional})
    AExpression expression();

    @Override
    AType type();

    @Property
    Set<AFieldCall> callers();

    @Property(hidden)
    default Set<AType> types() {
        Collection<AType> aClasses = dAncestor(APackage.class).classes().map(c -> dclare(AClassType.class, c));
        return Set.<AType> of(dclare(AStringType.class), dclare(ANumberType.class)).addAll(aClasses);
    }

    @Constraints
    default void modelConstraints() {
        SCOPE(AField::type, AField::types);
        OPPOSITE(AField::callers, AFieldCall::field);
    }

    @Property(validation)
    default DProblem isAssignable() {
        AExpression expr = expression();
        if (expr != null) {
            final AType target = type();
            final AType source = expr.type();
            if (!target.isAssignableFrom(source)) {
                return dclare(DProblem.class, this, "TYPE", DSeverity.error, source + " is not assignable to " + target);
            }
        }
        return null;
    }

    @Property(validation)
    default DProblem problem() {
        if (dAncestor(AClass.class).fields().filter(f -> f.name().equals(name())).size() > 1) {
            return dclare(DProblem.class, this, "UNIQUE_FIELD_NAME", DSeverity.error, "All field in a class must have a unique name");
        }
        return null;
    }

}
