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

package org.modelingvalue.transactions;

import java.util.function.Consumer;

public class Action extends LeafClass {

    public static Action of(Object id, Consumer<Contained> action) {
        return new Action(id, action, Direction.forward, Priority.postDepth);
    }

    public static Action of(Object id, Consumer<Contained> action, Priority priority) {
        return new Action(id, action, Direction.forward, priority);
    }

    public static Action of(Object id, Consumer<Contained> action, Direction initDirection, Priority priority) {
        return new Action(id, action, initDirection, priority);
    }

    private final Consumer<Contained> action;

    protected Action(Object id, Consumer<Contained> action, Direction initDirection, Priority priority) {
        super(id, initDirection, priority);
        this.action = action;
    }

    public void run(Contained object) {
        action.accept(object);
    }

}
