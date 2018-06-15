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

package org.modelingvalue.jdclare;

import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.io.Serializable;
import java.lang.invoke.MethodHandles.Lookup;

import org.modelingvalue.jdclare.java.JStructClass;
import org.modelingvalue.jdclare.meta.DStructClass;

@Extend(JStructClass.class)
public interface DStruct extends Comparable<DStruct>, Serializable {

    int getKeySize();

    Object getKey(int i);

    @SuppressWarnings("unchecked")
    @Property({constant, hidden})
    default <T extends DStruct> DStructClass<T> dStructClass() {
        return (DStructClass<T>) DClare.dClass(DClare.jClass(this));
    }

    @Override
    default int compareTo(DStruct other) {
        return DClare.COMPARATOR.compare(this, other);
    }

    default String asString() {
        return DClare.toString(this);
    }

    Lookup lookup();
}
