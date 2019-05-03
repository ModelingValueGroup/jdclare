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

public class Observer<O extends Mutable> extends Action<O> {

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

    protected long                                    runCount = -1;
    protected int                                     instances;
    protected int                                     changes;
    protected boolean                                 stopped;

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
            observeds[ia].set(mutable, observeds[ia].getDefault());
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
    public static final class Observerds extends Setable<Mutable, Set<ObservedInstance>> {

        public static Observerds of(Observer observer, Direction direction) {
            return new Observerds(observer, direction);
        }

        @SuppressWarnings("unchecked")
        private Observerds(Observer observer, Direction direction) {
            super(Pair.of(observer, direction), Set.of(), false, (tx, mutable, pre, post) -> {
                ActionInstance ai = ActionInstance.of(mutable, observer);
                pre.compare(post).forEach(d -> {
                    if (d[0] == null) {
                        d[1].forEach(n -> tx.set(n.object(), n.property().observers(direction), Set<ActionInstance>::add, ai));
                    }
                    if (d[1] == null) {
                        d[0].forEach(o -> tx.set(o.object(), o.property().observers(direction), Set<ActionInstance>::remove, ai));
                    }
                });
            });
        }

    }

}
