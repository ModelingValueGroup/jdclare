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

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.transactions.Direction.Queued;

public class Observer<O extends Mutable> extends Action<O> {

    @SuppressWarnings("rawtypes")
    protected static final Map<Observer, Set<Mutable>> OBSERVER_MAP = Map.of(k -> Set.of());

    public static <M extends Mutable> Observer<M> of(Object id, Consumer<M> action) {
        return new Observer<M>(id, action, Direction.forward, Priority.postDepth);
    }

    public static <M extends Mutable> Observer<M> of(Object id, Consumer<M> action, Priority priority) {
        return new Observer<M>(id, action, Direction.forward, priority);
    }

    public static <M extends Mutable> Observer<M> of(Object id, Consumer<M> action, Direction initDirection, Priority priority) {
        return new Observer<M>(id, action, initDirection, priority);
    }

    public final Setable<Mutable, Set<ObserverTrace>> traces;

    private final Observerds[]                        observeds;

    protected long                                    runCount     = -1;
    protected int                                     instances;
    protected int                                     changes;
    protected boolean                                 stopped;
    @SuppressWarnings("rawtypes")
    private final Entry<Observer, Set<Mutable>>       thisInstance = Entry.of(this, Mutable.THIS_SINGLETON);

    protected Observer(Object id, Consumer<O> action, Direction initDirection, Priority priority) {
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

    @Override
    public ObserverTransaction openTransaction(MutableTransaction parent) {
        return parent.universeTransaction().observerTransactions.get().open(this, parent);
    }

    @Override
    public void closeTransaction(Transaction tx) {
        tx.universeTransaction().observerTransactions.get().close((ObserverTransaction) tx);
    }

    @Override
    public ObserverTransaction newTransaction(UniverseTransaction universeTransaction) {
        return new ObserverTransaction(universeTransaction);
    }

    public void deObserve(O mutable) {
        for (int ia = 0; ia < 2; ia++) {
            observeds[ia].setDefault(mutable);
        }
        for (Direction dir : Direction.values()) {
            for (Queued<Action<?>> set : dir.priorities) {
                set.setDefault(mutable);
            }
        }
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

    @SuppressWarnings("rawtypes")
    public static final class Observerds extends Setable<Mutable, Map<Observed, Set<Mutable>>> {

        public static Observerds of(Observer observer, Direction direction) {
            return new Observerds(observer, direction);
        }

        @SuppressWarnings("unchecked")
        private Observerds(Observer observer, Direction direction) {
            super(Pair.of(observer, direction), Observed.OBSERVED_MAP, false, null, (tx, mutable, pre, post) -> {
                for (Observed observed : Collection.concat(pre.toKeys(), post.toKeys()).distinct()) {
                    Setable<Mutable, Map<Observer, Set<Mutable>>> obs = observed.observers(direction);
                    pre.get(observed).compare(post.get(observed)).forEach(d -> {
                        if (d[0] == null) {
                            d[1].forEach(a -> {
                                Mutable o = a.resolve(mutable);
                                tx.set(o, obs, (m, e) -> m.add(e, Set::addAll), observer.entry(mutable, o));
                            });
                        }
                        if (d[1] == null) {
                            d[0].forEach(r -> {
                                Mutable o = r.resolve(mutable);
                                tx.set(o, obs, (m, e) -> m.remove(e, Set::removeAll), observer.entry(mutable, o));
                            });
                        }
                    });
                }
            });
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + super.toString().substring(4);
        }

    }

    @SuppressWarnings("rawtypes")
    private Entry entry(Mutable object, Mutable self) {
        return object.equals(self) ? thisInstance : Entry.of(this, Set.of(object));
    }

}
