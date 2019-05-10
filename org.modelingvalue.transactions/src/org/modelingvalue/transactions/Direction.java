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

import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Pair;

public enum Direction {

    forward(0),

    backward(1),

    scheduled(2);

    public final DirectionSetable<Action<?>>          preDepth;
    public final DirectionSetable<Mutable>            depth;
    public final DirectionSetable<Action<?>>          postDepth;
    public final int                                  nr;
    public final DirectionSetable<Action<?>>[]        priorities;
    public final DirectionSetable<TransactionClass>[] sequence;

    @SuppressWarnings("unchecked")
    private Direction(int nr) {
        preDepth = new DirectionSetable<>(Priority.preDepth);
        depth = new DirectionSetable<>(Priority.depth);
        postDepth = new DirectionSetable<>(Priority.postDepth);
        priorities = new DirectionSetable[]{preDepth, postDepth};
        sequence = new DirectionSetable[]{preDepth, depth, postDepth};
        this.nr = nr;
    }

    public final class DirectionSetable<T extends TransactionClass> extends Setable<Mutable, Set<T>> {
        private final Priority priority;

        private DirectionSetable(Priority priority) {
            super(Pair.of(Direction.this, priority), Set.of(), false, null, null);
            this.priority = priority;
        }

        public Direction direction() {
            return Direction.this;
        }

        public Priority priority() {
            return priority;
        }
    }

}
