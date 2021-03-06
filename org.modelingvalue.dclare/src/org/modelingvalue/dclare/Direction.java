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

import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Internable;
import org.modelingvalue.collections.util.Pair;

public enum Direction implements Internable {

    forward(0),

    backward(1),

    scheduled(2);

    public final Queued<Action<?>>          preDepth;
    public final Queued<Mutable>            depth;
    public final Queued<Action<?>>          postDepth;
    public final int                        nr;
    public final Queued<Action<?>>[]        priorities;
    public final Queued<TransactionClass>[] sequence;

    @SuppressWarnings("unchecked")
    private Direction(int nr) {
        preDepth = new Queued<>(Priority.preDepth);
        depth = new Queued<>(Priority.depth);
        postDepth = new Queued<>(Priority.postDepth);
        priorities = new Queued[]{preDepth, postDepth};
        sequence = new Queued[]{preDepth, depth, postDepth};
        this.nr = nr;
    }

    public final class Queued<T extends TransactionClass> extends Setable<Mutable, Set<T>> {
        private final Priority priority;

        private Queued(Priority priority) {
            super(Pair.of(Direction.this, priority), Set.of(), false, null, null, null, false);
            this.priority = priority;
        }

        public Direction direction() {
            return Direction.this;
        }

        public Priority priority() {
            return priority;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + super.toString().substring(4);
        }
    }

}
