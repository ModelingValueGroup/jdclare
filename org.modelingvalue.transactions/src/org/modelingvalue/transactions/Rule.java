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
import org.modelingvalue.collections.util.StringUtil;

public class Rule {

    private final Observerds[] observeds;
    private final Object       id;

    public Rule(Object id) {
        this.id = id;
        observeds = new Observerds[Priority.values().length];
        for (int i = 0; i < observeds.length; i++) {
            observeds[i] = Observerds.of(this, Priority.values()[i]);
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        } else {
            return id.equals(((Rule) obj).id);
        }
    }

    @Override
    public String toString() {
        return StringUtil.toString(id);
    }

    public Observerds[] observeds() {
        return observeds;
    }

    public Object id() {
        return id;
    }

    public static final class Observerds extends Setable<Object, Set<Slot>> {

        public static Observerds of(Rule rule, Priority prio) {
            return new Observerds(rule, prio);
        }

        @SuppressWarnings("unchecked")
        private Observerds(Rule rule, Priority prio) {
            super(Pair.of(prio, rule), Set.of(), (tx, object, pre, post) -> pre.compare(post).forEach(d -> {
                if (d[0] == null) {
                    d[1].forEach(n -> tx.set(n.object(), n.property().observers(prio), Set<Observer>::add, (Observer) tx.transaction()));
                }
                if (d[1] == null) {
                    if (tx.transaction() instanceof Observer) {
                        d[0].forEach(o -> tx.set(o.object(), o.property().observers(prio), Set<Observer>::remove, (Observer) tx.transaction()));
                    } else {
                        d[0].forEach(o -> tx.set(o.object(), o.property().observers(prio), (Set<Observer> s, Object k) -> {
                            return s.filter(r -> !r.rule().equals(rule) || r.initPrio() != prio || !r.parent.getId().equals(k)).toSet();
                        }, object));
                    }
                }
            }));
        }

    }

}
