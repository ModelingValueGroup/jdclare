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

package org.modelingvalue.dclare;

import java.util.function.Consumer;

public class NonInternableObserver<O extends Mutable> extends Observer<O> {

    public static <M extends Mutable> NonInternableObserver<M> of(Object id, Consumer<M> action) {
        return new NonInternableObserver<M>(id, action, Direction.forward, Priority.postDepth);
    }

    public static <M extends Mutable> NonInternableObserver<M> of(Object id, Consumer<M> action, Priority priority) {
        return new NonInternableObserver<M>(id, action, Direction.forward, priority);
    }

    public static <M extends Mutable> NonInternableObserver<M> of(Object id, Consumer<M> action, Direction initDirection, Priority priority) {
        return new NonInternableObserver<M>(id, action, initDirection, priority);
    }

    protected NonInternableObserver(Object id, Consumer<O> action, Direction initDirection, Priority priority) {
        super(id, action, initDirection, priority);
    }

    @Override
    public boolean isInternable() {
        return false;
    }

}
