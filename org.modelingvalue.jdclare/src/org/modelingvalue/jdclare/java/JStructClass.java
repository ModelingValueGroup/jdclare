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

import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DStruct;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.meta.DProperty;
import org.modelingvalue.jdclare.meta.DStructClass;

public interface JStructClass<T extends DStruct> extends JClass<T>, DStructClass<T> {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    @Property(constant)
    default Set<DProperty<T, ?>> properties() {
        Set<DProperty<T, ?>> properties = Set.of();
        for (Method method : jClass().getDeclaredMethods()) {
            DProperty p = DClare.dProperty(method);
            if (p != null) {
                properties = properties.add(p);
            }
        }
        return properties;
    }

}
