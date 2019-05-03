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
        return of(id, def, false, null);
    }

    public static <C, V> Observed<C, V> of(Object id, V def, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return of(id, def, false, changed);
    }

    public static <C, V> Observed<C, V> of(Object id, V def, boolean containment) {
        return of(id, def, containment, null);
    }

    public static <C, V> Observed<C, V> of(Object id, V def, boolean containment, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Observed<C, V>(id, def, containment, changed);
    }

    private final Setable<Object, Set<ObserverTrace>> readers = Setable.of(Pair.of(this, "readers"), Set.of());
    private final Setable<Object, Set<ObserverTrace>> writers = Setable.of(Pair.of(this, "writers"), Set.of());
    private final Observers<O, T>[]                   observers;

    @SuppressWarnings("unchecked")
    protected Observed(Object id, T def, boolean containment, QuadConsumer<LeafTransaction, O, T, T> changed) {
        this(id, def, containment, observers(id), changed);
    }

    @SuppressWarnings("rawtypes")
    private static Observers[] observers(Object id) {
        Observers[] observers = new Observers[2];
        for (int ia = 0; ia < 2; ia++) {
            observers[ia] = Observers.of(id, Direction.values()[ia]);
        }
        return observers;
    }

    @SuppressWarnings("unchecked")
    private Observed(Object id, T def, boolean containment, Observers<O, T>[] observers, QuadConsumer<LeafTransaction, O, T, T> changed) {
        super(id, def, containment, null);
        this.changed = (l, o, p, n) -> {
            if (changed != null) {
                changed.accept(l, o, p, n);
            }
            for (int ia = 0; ia < 2; ia++) {
                for (ActionInstance obs : l.get(o, observers[ia])) {
                    if (!l.cls().equals(obs.action()) || !l.parent().mutable().equals(obs.mutable())) {
                        l.trigger(obs.mutable(), obs.action(), Direction.values()[ia]);
                    }
                }
            }
        };
        this.observers = observers;
        for (int ia = 0; ia < 2; ia++) {
            observers[ia].observed = this;
        }
    }

    public Observers<O, T> observers(Direction direction) {
        return observers[direction.nr];
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
        LeafTransaction leafTransaction = LeafTransaction.getCurrent();
        int nr = 0;
        for (int ia = 0; ia < 2; ia++) {
            nr += leafTransaction.get(object, observers[ia]).size();
        }
        return nr;
    }

    public static final class Observers<O, T> extends Setable<O, Set<ActionInstance>> {

        private Observed<O, T>  observed;
        private final Direction direction;

        public static <C, V> Observers<C, V> of(Object id, Direction direction) {
            return new Observers<C, V>(id, direction);
        }

        private Observers(Object id, Direction direction) {
            super(Pair.of(id, direction), Set.of(), false, null);
            changed = (tx, o, b, a) -> tx.checkTooManyObservers(tx, o, observed, a);
            this.direction = direction;
        }

        public Observed<O, T> observed() {
            return observed;
        }

        public Direction direction() {
            return direction;
        }

    }

}
