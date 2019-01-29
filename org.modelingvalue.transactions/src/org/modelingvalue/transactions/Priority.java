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

public enum Priority {

    high(0),

    mid(1),

    low(2);

    public final PrioritySetable<AbstractLeaf> leafTriggered;
    public final PrioritySetable<AbstractLeaf> leafScheduled;
    public final PrioritySetable<Compound>     compTriggered;
    public final PrioritySetable<Compound>     compScheduled;
    public final int                           nr;

    private Priority(int nr) {
        leafTriggered = new PrioritySetable<>("leafTriggered");
        leafScheduled = new PrioritySetable<>("leafScheduled");
        compTriggered = new PrioritySetable<>("compTriggered");
        compScheduled = new PrioritySetable<>("compScheduled");
        this.nr = nr;
    }

    public final class PrioritySetable<T extends Transaction> extends Setable<Compound, Set<T>> {
        private PrioritySetable(String id) {
            super(Pair.of(Priority.this, id), Set.of(), null);
        }

        public Priority prio() {
            return Priority.this;
        }
    }

    public static Priority lowest() {
        Priority[] priorities = Priority.values();
        return priorities[priorities.length - 1];
    }

}
