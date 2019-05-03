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

    Observed<Mutable, Pair<Mutable, Setable<Mutable, ?>>> D_PARENT_CONTAINING = Observed.of("D_PARENT_CONTAINING", null);

    default Mutable dParent() {
        Pair<Mutable, Setable<Mutable, ?>> p = D_PARENT_CONTAINING.get(this);
        return p != null ? p.a() : null;
    }

    default Mutable dParent(State state) {
        Pair<Mutable, Setable<Mutable, ?>> p = state.get(this, D_PARENT_CONTAINING);
        return p != null ? p.a() : null;
    }

    default Setable<Mutable, ?> dContaining() {
        Pair<Mutable, Setable<Mutable, ?>> p = D_PARENT_CONTAINING.get(this);
        return p != null ? p.b() : null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    default void dActivate() {
        for (Observer observer : dObservers()) {
            observer.trigger(this);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    default void dDeactivate() {
        for (Observer observer : dObservers()) {
            observer.deObserve(this);
        }
    }

    @SuppressWarnings("rawtypes")
    default Set<Observer> dObservers() {
        return Set.of();
    }

    default Set<Setable<Mutable, ?>> dContainers() {
        return Set.of();
    }

    @SuppressWarnings("unchecked")
    default Collection<Mutable> dChildren() {
        return (Collection<Mutable>) dContainers().flatMap(c -> c.getCollection(this));
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
