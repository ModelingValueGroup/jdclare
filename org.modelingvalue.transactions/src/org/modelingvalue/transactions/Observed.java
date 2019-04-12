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
import org.modelingvalue.collections.util.QuadConsumer;
import org.modelingvalue.transactions.AbstractLeaf.AbstractLeafRun;

public class Observed<O, T> extends Setable<O, T> {

    public static <C, V> Observed<C, V> of(Object id, V def) {
        return of(id, def, null);
    }

    public static <C, V> Observed<C, V> of(Object id, V def, QuadConsumer<AbstractLeafRun<?>, C, V, V> changed) {
        return new Observed<C, V>(id, def, changed);
    }

    private final Setable<Object, Set<ObserverTrace>> readers = Setable.of(Pair.of(this, "readers"), Set.of());
    private final Setable<Object, Set<ObserverTrace>> writers = Setable.of(Pair.of(this, "writers"), Set.of());
    private final Observers<O, T>[]                   observers;

    @SuppressWarnings("unchecked")
    protected Observed(Object id, T def, QuadConsumer<AbstractLeafRun<?>, O, T, T> changed) {
        this(id, def, observers(id), changed);
    }

    @SuppressWarnings("rawtypes")
    private static Observers[] observers(Object id) {
        Observers[] observers = new Observers[2];
        for (int ia = 0; ia < 2; ia++) {
            observers[ia] = Observers.of(id, Phase.values()[ia]);
        }
        return observers;
    }

    private Observed(Object id, T def, Observers<O, T>[] observers, QuadConsumer<AbstractLeafRun<?>, O, T, T> changed) {
        super(id, def, null);
        this.changed = (l, o, p, n) -> {
            if (changed != null) {
                changed.accept(l, o, p, n);
            }
            for (int ia = 0; ia < 2; ia++) {
                for (Observer obs : l.get(o, observers[ia])) {
                    if (!l.transaction().equals(obs)) {
                        l.trigger(obs, Phase.values()[ia]);
                    }
                }
            }
        };
        this.observers = observers;
        for (int ia = 0; ia < 2; ia++) {
            observers[ia].observed = this;
        }
    }

    public Observers<O, T> observers(Phase phase) {
        return observers[phase.nr];
    }

    public Observers<O, T>[] observers() {
        return observers;
    }

    public Setable<Object, Set<ObserverTrace>> readers() {
        return readers;
    }

    public Setable<Object, Set<ObserverTrace>> writers() {
        return writers;
    }

    public int getNrOfObservers(O object) {
        AbstractLeafRun<?> leaf = AbstractLeaf.getCurrent();
        int nr = 0;
        for (int ia = 0; ia < 2; ia++) {
            nr += leaf.get(object, observers[ia]).size();
        }
        return nr;
    }

    public static final class Observers<O, T> extends Setable<O, Set<Observer>> {

        private Observed<O, T> observed;
        private final Phase    phase;

        public static <C, V> Observers<C, V> of(Object id, Phase phase) {
            return new Observers<C, V>(id, phase);
        }

        private Observers(Object id, Phase phase) {
            super(Pair.of(id, phase), Set.of(), null);
            changed = (tx, o, b, a) -> tx.transaction().checkTooManyObservers(tx, o, observed, a);
            this.phase = phase;
        }

        public Observed<O, T> observed() {
            return observed;
        }

        public Phase phase() {
            return phase;
        }

    }

}
