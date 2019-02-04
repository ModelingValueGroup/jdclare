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

public class Observed<O, T> extends Setable<O, T> {

    public static <C, V> Observed<C, V> of(Object id, V def) {
        return of(id, def, null);
    }

    public static <C, V> Observed<C, V> of(Object id, V def, QuadConsumer<AbstractLeaf, C, V, V> changed) {
        return new Observed<C, V>(id, def, changed);
    }

    private final Setable<Object, Set<ObserverRun>> readers = Setable.of(Pair.of(this, "readers"), Set.of());
    private final Setable<Object, Set<ObserverRun>> writers = Setable.of(Pair.of(this, "writers"), Set.of());
    private final Observers<O, T>[]                 observers;

    @SuppressWarnings("unchecked")
    protected Observed(Object id, T def, QuadConsumer<AbstractLeaf, O, T, T> changed) {
        this(id, def, observers(id), changed);
    }

    @SuppressWarnings("rawtypes")
    private static Observers[] observers(Object id) {
        Priority[] priorities = Priority.values();
        Observers[] observers = new Observers[priorities.length];
        for (int i = 0; i < priorities.length; i++) {
            observers[i] = Observers.of(Pair.of(id, priorities[i]), priorities[i]);
        }
        return observers;
    }

    private Observed(Object id, T def, Observers<O, T>[] observers, QuadConsumer<AbstractLeaf, O, T, T> changed) {
        super(id, def, null);
        this.changed = (l, o, p, n) -> {
            if (changed != null) {
                changed.accept(l, o, p, n);
            }
            for (Observers<O, T> observ : observers) {
                for (Observer obs : l.get(o, observ)) {
                    if (!l.equals(obs)) {
                        l.trigger(obs, observ.prio());
                    }
                }
            }
        };
        this.observers = observers;
        for (Observers<O, T> observ : observers) {
            observ.observed = this;
        }
    }

    public Observers<O, T> observers(Priority prio) {
        return observers[prio.nr];
    }

    public Observers<O, T>[] observers() {
        return observers;
    }

    public Setable<Object, Set<ObserverRun>> readers() {
        return readers;
    }

    public Setable<Object, Set<ObserverRun>> writers() {
        return writers;
    }

    public int getNrOfObservers(O object) {
        AbstractLeaf leaf = AbstractLeaf.getCurrent();
        int nr = 0;
        for (Observers<O, T> observ : observers) {
            nr += leaf.get(object, observ).size();
        }
        return nr;
    }

    public static final class Observers<O, T> extends Setable<O, Set<Observer>> {

        private Observed<O, T> observed;
        private final Priority prio;

        public static <C, V> Observers<C, V> of(Object id, Priority prio) {
            return new Observers<C, V>(id, prio);
        }

        private Observers(Object id, Priority prio) {
            super(id, Set.of(), null);
            this.prio = prio;
        }

        public Observed<O, T> observed() {
            return observed;
        }

        public Priority prio() {
            return prio;
        }

    }

}
