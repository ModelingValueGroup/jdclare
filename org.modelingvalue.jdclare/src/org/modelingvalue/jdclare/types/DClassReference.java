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

package org.modelingvalue.jdclare.types;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.collections.ContainingCollection;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DStruct2;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.meta.DClass;
import org.modelingvalue.jdclare.meta.DTypeParameter;

public interface DClassReference extends DStruct2<DClass<?>, List<DType>>, DType {

    @Property(key = 0)
    DClass<?> referenced();

    @Property(key = 1)
    List<DType> typeParameters();

    @Override
    default String asString() {
        List<DType> typeParameters = typeParameters();
        if (typeParameters.isEmpty()) {
            return referenced().name();
        } else {
            String ps = "";
            for (DType p : typeParameters) {
                ps += (ps.isEmpty() ? "" : ",") + p.asString();
            }
            return referenced().name() + "<" + ps + ">";
        }
    }

    @Property
    default Map<DTypeParameter, DType> typeParameterMap() {
        List<DType> ats = typeParameters();
        return referenced().typeParameters().toMap(tp -> Entry.of(tp, ats.get(tp.nr())));
    }

    @Property
    default Set<DType> supers() {
        return referenced().supers().map(s -> s.actualize(typeParameterMap())).toSet();
    }

    @Override
    default DType actualize(Map<DTypeParameter, DType> params) {
        return dclare(DClassReference.class, referenced(), typeParameters().map(p -> p.actualize(params)).toList());
    }

    @Override
    @Property(constant)
    default DType elementType() {
        Class<?> jClass = referenced().jClass();
        List<DType> typeParameters = typeParameters();
        return QualifiedSet.class.isAssignableFrom(jClass) ? typeParameters.get(1) : ContainingCollection.class.isAssignableFrom(jClass) && typeParameters.size() == 1 ? typeParameters.get(0) : this;
    }

}
