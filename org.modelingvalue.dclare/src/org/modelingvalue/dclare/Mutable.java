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

package org.modelingvalue.dclare;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.DefaultMap;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Pair;

public interface Mutable extends TransactionClass {

    Mutable                                               THIS                     = new This();

    Set<Mutable>                                          THIS_SINGLETON           = Set.of(THIS);

    Observed<Mutable, Pair<Mutable, Setable<Mutable, ?>>> D_PARENT_CONTAINING      = new Observed<Mutable, Pair<Mutable, Setable<Mutable, ?>>>("D_PARENT_CONTAINING", null, false, null, null, null, true) {
                                                                                       @SuppressWarnings("rawtypes")
                                                                                       @Override
                                                                                       protected void checkTooManyObservers(LeafTransaction tx, Object object, DefaultMap<Observer, Set<Mutable>> observers) {
                                                                                       };
                                                                                   };                                                                                                                         //

    @SuppressWarnings({"rawtypes", "unchecked"})
    Setable<Mutable, Set<? extends Observer<?>>>          D_OBSERVERS              = Setable.of("D_OBSERVERS", Set.of(), (tx, obj, pre, post) -> {
                                                                                       Setable.<Set<? extends Observer<?>>, Observer> diff(pre, post,                                                         //
                                                                                               added -> added.trigger(obj),                                                                                   //
                                                                                               removed -> removed.deObserve(obj));
                                                                                   });

    Observer<Mutable>                                     D_OBSERVERS_RULE         = Observer.of("D_OBSERVERS_RULE", m -> {
                                                                                       D_OBSERVERS.set(m, Collection.concat(m.dClass().dObservers(), m.dMutableObservers()).toSet());
                                                                                   }, Priority.preDepth);

    @SuppressWarnings("unchecked")
    Observer<Mutable>                                     D_PUSHING_CONSTANTS_RULE = Observer.of("D_CONTAINMENT_CONSTANTS_RULE", m -> {
                                                                                       MutableClass.D_PUSHING_CONSTANTS.get(m.dClass()).forEach(c -> c.get(m));
                                                                                   }, Priority.preDepth);

    default Mutable dParent() {
        Pair<Mutable, Setable<Mutable, ?>> pair = D_PARENT_CONTAINING.get(this);
        return pair != null ? pair.a() : null;
    }

    default Setable<Mutable, ?> dContaining() {
        Pair<Mutable, Setable<Mutable, ?>> pair = D_PARENT_CONTAINING.get(this);
        return pair != null ? pair.b() : null;
    }

    @SuppressWarnings("unchecked")
    default <C> C dAncestor(Class<C> cls) {
        Mutable parent = dParent();
        while (parent != null && !cls.isInstance(parent)) {
            parent = parent.dParent();
        }
        if (parent == null) {
            throw new DeferException();
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
        D_PUSHING_CONSTANTS_RULE.trigger(this);
    }

    default void dDeactivate() {
        D_OBSERVERS_RULE.deObserve(this);
        D_PUSHING_CONSTANTS_RULE.deObserve(this);
        D_OBSERVERS.setDefault(this);
        for (Direction dir : Direction.values()) {
            dir.depth.set(dParent(), Set::remove, this);
        }
    }

    MutableClass dClass();

    default Collection<? extends Observer<?>> dMutableObservers() {
        return Set.of();
    }

    @SuppressWarnings("unchecked")
    default Collection<? extends Mutable> dChildren() {
        return MutableClass.D_CONTAINMENTS.get(dClass()).flatMap(c -> (Collection<? extends Mutable>) c.getCollection(this));
    }

    @SuppressWarnings("unchecked")
    default Collection<? extends Mutable> dChildren(State state) {
        return MutableClass.D_CONTAINMENTS.get(dClass()).flatMap(c -> (Collection<? extends Mutable>) state.getCollection(this, c));
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

    default Mutable resolve(Mutable self) {
        return this;
    }

}
