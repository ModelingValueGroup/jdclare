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

public enum Phase {

    triggeredForward(0),

    triggeredBackward(1),

    scheduled(2);

    public final PhaseSetable<AbstractLeaf>   preDepth;
    public final PhaseSetable<Compound>       depth;
    public final PhaseSetable<AbstractLeaf>   postDepth;
    public final int                          nr;
    public final PhaseSetable<AbstractLeaf>[] priorities;
    public final PhaseSetable<Transaction>[]  sequence;

    @SuppressWarnings("unchecked")
    private Phase(int nr) {
        preDepth = new PhaseSetable<>(Priority.preDepth);
        depth = new PhaseSetable<>(Priority.depth);
        postDepth = new PhaseSetable<>(Priority.postDepth);
        priorities = new PhaseSetable[]{preDepth, postDepth};
        sequence = new PhaseSetable[]{preDepth, depth, postDepth};
        this.nr = nr;
    }

    public final class PhaseSetable<T extends Transaction> extends Setable<Object, Set<T>> {
        private final Priority priority;

        private PhaseSetable(Priority priority) {
            super(Pair.of(Phase.this, priority), Set.of(), null);
            this.priority = priority;
        }

        public Phase phase() {
            return Phase.this;
        }

        public Priority priority() {
            return priority;
        }
    }

}
