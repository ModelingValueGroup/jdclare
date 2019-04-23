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

import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Pair;

public class Rule extends Action {

    public static Rule of(Object id, Consumer<Contained> action) {
        return new Rule(id, action, Direction.forward, Priority.postDepth);
    }

    public static Rule of(Object id, Consumer<Contained> action, Priority priority) {
        return new Rule(id, action, Direction.forward, priority);
    }

    public static Rule of(Object id, Consumer<Contained> action, Direction initDirection, Priority priority) {
        return new Rule(id, action, initDirection, priority);
    }

    public final Setable<Contained, Set<ObserverTrace>> traces;

    private final Observerds[]                          observeds;

    protected long                                      runCount = -1;
    protected int                                       instances;
    protected int                                       changes;
    protected boolean                                   stopped;

    protected Rule(Object id, Consumer<Contained> action, Direction initDirection, Priority priority) {
        super(id, action, initDirection, priority);
        this.traces = Setable.of(Pair.of(this, "TRACES"), Set.of());
        observeds = new Observerds[2];
        for (int ia = 0; ia < 2; ia++) {
            observeds[ia] = Observerds.of(this, Direction.values()[ia]);
        }
    }

    public Observerds[] observeds() {
        return observeds;
    }

    public int countChangesPerInstance() {
        ++changes;
        return changesPerInstance();
    }

    public int changesPerInstance() {
        int i = instances;
        if (i <= 0) {
            instances = 1;
            return changes;
        } else {
            return changes / i;
        }
    }

    public static final class Observerds extends Setable<Contained, Set<Slot>> {

        public static Observerds of(Rule rule, Direction direction) {
            return new Observerds(rule, direction);
        }

        @SuppressWarnings("unchecked")
        private Observerds(Rule rule, Direction direction) {
            super(Pair.of(direction, rule), Set.of(), (tx, object, pre, post) -> pre.compare(post).forEach(d -> {
                if (d[0] == null) {
                    d[1].forEach(n -> tx.set(n.object(), n.property().observers(direction), Set<Observer>::add, (Observer) tx.transaction()));
                }
                if (d[1] == null) {
                    if (tx.transaction() instanceof Observer) {
                        d[0].forEach(o -> tx.set(o.object(), o.property().observers(direction), Set<Observer>::remove, (Observer) tx.transaction()));
                    } else {
                        d[0].forEach(o -> tx.set(o.object(), o.property().observers(direction), (Set<Observer> s, Object k) -> {
                            return s.filter(r -> !r.rule().equals(rule) || r.rule().initDirection() != direction || !r.parent().contained().equals(k)).toSet();
                        }, object));
                    }
                }
            }));
        }

    }

}
