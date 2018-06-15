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

import java.util.function.Function;

import org.modelingvalue.collections.util.StringUtil;
import org.modelingvalue.jdclare.DProblem;

public enum ProblemsTableColumn {

    context(p -> StringUtil.toString(p.context()), 11),

    id(p -> p.id(), 11),

    severity(p -> p.severity(), 7),

    message(p -> p.message(), 71);

    private final Function<DProblem, Object> function;
    private final int                        width;

    ProblemsTableColumn(Function<DProblem, Object> function, int width) {
        this.function = function;
        this.width = width;
    }

    public Function<DProblem, Object> function() {
        return function;
    }

    public int width() {
        return width;
    }

}
