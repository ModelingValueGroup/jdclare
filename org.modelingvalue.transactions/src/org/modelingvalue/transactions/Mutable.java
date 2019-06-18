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

public interface Mutable extends TransactionClass {

    This                                         THIS             = new This();

    Observed<Mutable, Setable<Mutable, ?>>       D_CONTAINING     = InternableObserved.of("D_CONTAINING", null);

    Observed<Mutable, Mutable>                   D_PARENT         = Observed.of("D_PARENT", null);

    @SuppressWarnings({"rawtypes", "unchecked"})
    Setable<Mutable, Set<? extends Observer<?>>> D_OBSERVERS      = new Setable<Mutable, Set<? extends Observer<?>>>("D_OBSERVERS", Set.of(), false, null, (tx, obj, pre, post) -> {
                                                                      Setable.<Set<? extends Observer<?>>, Observer> diff(pre, post,                                                //
                                                                              added -> added.trigger(obj),                                                                          //
                                                                              removed -> removed.deObserve(obj));
                                                                  }) {
                                                                      @Override
                                                                      public boolean isInternable(Set<? extends Observer<?>> value) {
                                                                          return value.allMatch(Observer::isInternable);
                                                                      };
                                                                  };

    Observer<Mutable>                            D_OBSERVERS_RULE = Observer.of("D_OBSERVERS_RULE", m -> {
                                                                      D_OBSERVERS.set(m, m.dObservers().toSet());
                                                                  }, Priority.preDepth);

    @SuppressWarnings({"unchecked", "rawtypes"})
    Observer<Mutable>                            D_CONSTANTS_RULE = Observer.of("D_CONSTANTS_RULE", m -> {
                                                                      m.dConstants().forEach(c -> ((Constant) c).get(m));
                                                                  }, Priority.preDepth);

    default Mutable dParent() {
        return D_PARENT.get(this);
    }

    default Setable<Mutable, ?> dContaining() {
        return D_CONTAINING.get(this);
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
        D_CONSTANTS_RULE.trigger(this);
    }

    default void dDeactivate() {
        D_OBSERVERS_RULE.deObserve(this);
        D_CONSTANTS_RULE.deObserve(this);
        D_OBSERVERS.setDefault(this);
        for (Direction dir : Direction.values()) {
            dir.depth.set(dParent(), Set::remove, this);
        }
    }

    Collection<? extends Observer<?>> dObservers();

    Collection<? extends Setable<? extends Mutable, ?>> dContainers();

    Collection<? extends Constant<? extends Mutable, ?>> dConstants();

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Collection<? extends Mutable> dChildren() {
        return dContainers().flatMap(c -> (Collection<? extends Mutable>) ((Setable) c).getCollection(this));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Collection<? extends Mutable> dChildren(State state) {
        return dContainers().flatMap(c -> (Collection<? extends Mutable>) state.getCollection(this, (Setable) c));
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
