//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018-2019 Modeling Value Group B.V. (http://modelingvalue.org)                                        ~
//                                                                                                                     ~
// Licensed under the GNU Lesser General Public License v3.0 (the 'License'). You may not use this file except in      ~
// compliance with the License. You may obtain a copy of the License at: https://choosealicense.com/licenses/lgpl-3.0  ~
// Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on ~
// an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the  ~
// specific language governing permissions and limitations under the License.                                          ~
//                                                                                                                     ~
// Maintainers:                                                                                                        ~
//     Wim Bast, Tom Brus, Ronald Krijgsheld                                                                           ~
// Contributors:                                                                                                       ~
//     Arjan Kok, Carel Bast                                                                                           ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package org.modelingvalue.dclare;

import org.modelingvalue.collections.*;

public interface MutableClass {

    @SuppressWarnings({"rawtypes"})
    Constant <MutableClass, Set <Setable>> D_CONTAINMENTS = Constant.of("D_CONTAINMENTS", //
            c -> c.dSetables().filter(Setable::containment).map(s -> (Setable) s).toSet());

    @SuppressWarnings({"rawtypes"})
    Constant <MutableClass, Set <Constant>> D_PUSHING_CONSTANTS = Constant.of("D_PUSHING_CONSTANTS", //
            c -> c.dSetables().filter(s -> s instanceof Constant && s.isHandlingChange() && ((Constant) s).deriver() != null).map(s -> (Constant) s).toSet());

    Collection <? extends Observer <?>> dObservers();

    Collection <? extends Setable <? extends Mutable, ?>> dSetables();

}
