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

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.meta.DParameter;
import org.modelingvalue.jdclare.types.DType;

public interface JParameter<T> extends DParameter<T>, DStruct1<Parameter> {

    @Property(key = 0)
    Parameter parameter();

    @Override
    @Property(constant)
    default DType type() {
        return DClare.dType(parameter().getParameterizedType());
    }

    @Override
    @Property(constant)
    default String name() {
        return parameter().getName();
    }

    @Override
    @Property(constant)
    default int nr() {
        Parameter p = parameter();
        Executable declaring = p.getDeclaringExecutable();
        boolean s = declaring instanceof Method && (((Method) declaring).getModifiers() & Modifier.STATIC) != 0;
        Parameter[] ps = declaring.getParameters();
        for (int i = s ? 1 : 0; i < ps.length; i++) {
            if (ps[i].equals(p)) {
                return s ? i - 1 : i;
            }
        }
        return -1;
    }

}
