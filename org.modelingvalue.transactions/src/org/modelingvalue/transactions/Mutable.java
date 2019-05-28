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

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Pair;

public interface Mutable extends TransactionClass {

    Observed<Mutable, Pair<Mutable, Setable<Mutable, ?>>>          D_PARENT_CONTAINING        = Observed.of("D_PARENT_CONTAINING", null);

    @SuppressWarnings({"rawtypes", "unchecked"})
    Setable<Mutable, Set<? extends Observer<?>>>                   D_OBSERVERS                = Setable.of("D_OBSERVERS", Set.of(), (tx, obj, pre, post) -> {
                                                                                                  Setable.<Set<? extends Observer<?>>, Observer> diff(pre, post,                        //
                                                                                                          added -> added.trigger(obj),                                                  //
                                                                                                          removed -> removed.deObserve(obj));
                                                                                              });

    Observer<Mutable>                                              D_OBSERVERS_RULE           = Observer.of(D_OBSERVERS, m -> {
                                                                                                  D_OBSERVERS.set(m, m.dObservers().toSet());
                                                                                              }, Priority.preDepth);

    @SuppressWarnings("rawtypes")
    Constant<Set<? extends Setable<?, ?>>, Set<? extends Setable>> D_CONSTANT_CONTAINERS      = Constant.of("D_CONSTANT_CONTAINERS", s -> s.filter(c -> c instanceof Constant).toSet());

    @SuppressWarnings("unchecked")
    Observer<Mutable>                                              D_CONSTANT_CONTAINERS_RULE = Observer.of(D_CONSTANT_CONTAINERS, m -> {
                                                                                                  D_CONSTANT_CONTAINERS.get(m.dContainers().toSet()).forEach(c -> c.get(m));
                                                                                              }, Priority.preDepth);

    default Mutable dParent() {
        Pair<Mutable, Setable<Mutable, ?>> p = D_PARENT_CONTAINING.get(this);
        return p != null ? p.a() : null;
    }

    default Setable<Mutable, ?> dContaining() {
        Pair<Mutable, Setable<Mutable, ?>> p = D_PARENT_CONTAINING.get(this);
        return p != null ? p.b() : null;
    }

    @SuppressWarnings("unchecked")
    default <C> C dAncestor(Class<C> cls) {
        Mutable parent = dParent();
        while (parent != null && !cls.isInstance(parent)) {
            parent = parent.dParent();
        }
        if (parent == null) {
            throw new EmptyMandatoryException();
        }
        return (C) parent;
    }

    @SuppressWarnings("unchecked")
    default <T> T dParent(Class<T> cls) {
        Mutable p = dParent();
        return p != null && cls.isInstance(p) ? (T) p : null;
    }

    default void dActivate() {
        D_OBSERVERS_RULE.trigger(this);
        D_CONSTANT_CONTAINERS_RULE.trigger(this);
    }

    default void dDeactivate() {
        D_OBSERVERS_RULE.deObserve(this);
        D_OBSERVERS.setDefault(this);
        for (Direction dir : Direction.values()) {
            dir.depth.set(dParent(), Set::remove, this);
        }
    }

    Collection<? extends Observer<?>> dObservers();

    Collection<? extends Setable<? extends Mutable, ?>> dContainers();

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Collection<? extends Mutable> dChildren() {
        return dContainers().flatMap(c -> (Collection<? extends Mutable>) ((Setable) c).getCollection(this));
    }

    @Override
    default MutableTransaction openTransaction(MutableTransaction parent) {
        return parent.universeTransaction().mutableTransactions.get().open(this, parent);
    }

    @Override
    default void closeTransaction(Transaction tx) {
        tx.universeTransaction().mutableTransactions.get().close((MutableTransaction) tx);
    }

    @Override
    default MutableTransaction newTransaction(UniverseTransaction universeTransaction) {
        return new MutableTransaction(universeTransaction);
    }

}