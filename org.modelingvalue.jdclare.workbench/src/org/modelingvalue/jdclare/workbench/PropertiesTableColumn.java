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

package org.modelingvalue.jdclare.workbench;

import java.util.function.BiFunction;

import org.modelingvalue.collections.util.StringUtil;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.meta.DProperty;

public enum PropertiesTableColumn {

    property((o, p) -> p.name(), 20),

    value((o, p) -> o.dClass().allNonContainments().contains(p) ? StringUtil.toString(p.get(o)) : "null", 80);

    private final BiFunction<DObject, DProperty<DObject, ?>, Object> function;
    private final int                                                width;

    PropertiesTableColumn(BiFunction<DObject, DProperty<DObject, ?>, Object> function, int width) {
        this.function = function;
        this.width = width;
    }

    public BiFunction<DObject, DProperty<DObject, ?>, Object> function() {
        return function;
    }

    int width() {
        return width;
    }

}
