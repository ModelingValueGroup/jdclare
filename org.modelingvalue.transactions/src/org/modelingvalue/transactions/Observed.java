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

import java.util.function.Supplier;

import org.modelingvalue.collections.ContainingCollection;
import org.modelingvalue.collections.DefaultMap;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.QuadConsumer;

public class Observed<O, T> extends Setable<O, T> {

    @SuppressWarnings("rawtypes")
    protected static final DefaultMap<Observed, Set<Mutable>> OBSERVED_MAP = DefaultMap.of(k -> Set.of());

    public static <C, V> Observed<C, V> of(Object id, V def) {
        return new Observed<C, V>(id, def, false, null, null, null, true);
    }

    public static <C, V> Observed<C, V> of(Object id, V def, boolean containment) {
        return new Observed<C, V>(id, def, containment, null, null, null, true);
    }

    public static <C, V> Observed<C, V> of(Object id, V def, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Observed<C, V>(id, def, false, null, null, changed, true);
    }

    public static <C, V> Observed<C, V> of(Object id, V def, boolean containment, boolean checkConsistency) {
        return new Observed<C, V>(id, def, containment, null, null, null, checkConsistency);
    }

    public static <C, V> Observed<C, V> of(Object id, V def, Supplier<Setable<?, ?>> opposite) {
        return new Observed<C, V>(id, def, false, opposite, null, null, true);
    }

    public static <C, V> Observed<C, V> of(Object id, V def, Supplier<Setable<?, ?>> opposite, Supplier<Setable<C, Set<?>>> scope, boolean checkConsistency) {
        return new Observed<C, V>(id, def, false, opposite, scope, null, checkConsistency);
    }

    private final Setable<Object, Set<ObserverTrace>> readers      = Setable.of(Pair.of(this, "readers"), Set.of());
    private final Setable<Object, Set<ObserverTrace>> writers      = Setable.of(Pair.of(this, "writers"), Set.of());
    private final Observers<O, T>[]                   observers;
    @SuppressWarnings("rawtypes")
    private final Entry<Observed, Set<Mutable>>       thisInstance = Entry.of(this, Mutable.THIS_SINGLETON);
    private boolean                                   isReference;

    @SuppressWarnings("unchecked")
    protected Observed(Object id, T def, boolean containment, Supplier<Setable<?, ?>> opposite, Supplier<Setable<O, Set<?>>> scope, QuadConsumer<LeafTransaction, O, T, T> changed, boolean checkConsistency) {
        this(id, def, containment, opposite, scope, observers(id), changed, checkConsistency);
    }

    @SuppressWarnings("rawtypes")
    private static Observers[] observers(Object id) {
        Observers[] observers = new Observers[2];
        for (int ia = 0; ia < 2; ia++) {
            observers[ia] = Observers.of(id, Direction.values()[ia]);
        }
        return observers;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Observed(Object id, T def, boolean containment, Supplier<Setable<?, ?>> opposite, Supplier<Setable<O, Set<?>>> scope, Observers<O, T>[] observers, QuadConsumer<LeafTransaction, O, T, T> changed, boolean checkConsistency) {
        super(id, def, containment, opposite, scope, null, checkConsistency);
        this.changed = (l, o, p, n) -> {
            if (changed != null) {
                changed.accept(l, o, p, n);
            }
            for (int ia = 0; ia < 2; ia++) {
                DefaultMap<Observer, Set<Mutable>> obsSet = l.get(o, observers[ia]);
                l.checkTooManyObservers(o, observers[ia].observed, obsSet);
                for (Entry<Observer, Set<Mutable>> e : obsSet) {
                    for (Mutable m : e.getValue()) {
                        Mutable mutable = m.resolve((Mutable) o);
                        if (!l.cls().equals(e.getKey()) || !l.parent().mutable().equals(mutable)) {
                            l.trigger(mutable, e.getKey(), Direction.values()[ia]);
                        }
                    }
                }
            }
            if (!containment && opposite == null && this != Mutable.D_PARENT && this != Mutable.D_CONTAINING && !isReference) {
                Object v = n;
                if (v instanceof ContainingCollection) {
                    v = ((ContainingCollection<?>) v).isEmpty() ? null : ((ContainingCollection<?>) v).get(0);
                }
                if (v instanceof Mutable) {
                    isReference = true;
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

    @SuppressWarnings("rawtypes")
    public static final class Observers<O, T> extends Setable<O, DefaultMap<Observer, Set<Mutable>>> {

        private Observed<O, T>  observed;
        private final Direction direction;

        public static <C, V> Observers<C, V> of(Object id, Direction direction) {
            return new Observers<C, V>(id, direction);
        }

        private Observers(Object id, Direction direction) {
            super(Pair.of(id, direction), Observer.OBSERVER_MAP, false, null, null, null, false);
            changed = (tx, o, b, a) -> tx.checkTooManyObservers(o, observed, a);
            this.direction = direction;
        }

        public Observed<O, T> observed() {
            return observed;
        }

        public Direction direction() {
            return direction;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + super.toString().substring(4);
        }

    }

    @SuppressWarnings("rawtypes")
    protected Entry<Observed, Set<Mutable>> entry(Mutable object, Mutable self) {
        return object.equals(self) ? thisInstance : Entry.of(this, Set.of(object));
    }

    @Override
    public boolean checkConsistency() {
        return super.checkConsistency() || (checkConsistency && isReference);
    }

    @Override
    public void checkConsistency(State state, O object, T pre, T post) {
        if (super.checkConsistency()) {
            super.checkConsistency(state, object, pre, post);
        }
        if (isReference) {
            for (Mutable m : mutables(post)) {
                if (!(m instanceof Universe) && state.get(m, Mutable.D_PARENT) == null) {
                    throw new ReferencedOrphanException(object, this, m);
                }
            }
        }
    }

}
