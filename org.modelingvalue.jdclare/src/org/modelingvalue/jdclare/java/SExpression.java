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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import org.modelingvalue.collections.Map;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.meta.DParameter;

public interface SExpression<I, O> extends MExpression<I, O> {

    @SuppressWarnings("unchecked")
    @Override
    default O run(I self, Map<DParameter<?>, ?> params) {
        Method method = method();
        if (method.getParameterCount() > 1) {
            Parameter[] jParams = method.getParameters();
            jParams = Arrays.copyOfRange(jParams, 1, jParams.length);
            Object[] args = new Object[jParams.length];
            for (int i = 0; i < args.length; i++) {
                args[i] = params.get(DClare.dclare(JParameter.class, jParams[i]));
            }
            return (O) DClare.run(self, method, args);
        } else {
            return (O) DClare.run(self, method);
        }
    }

}
