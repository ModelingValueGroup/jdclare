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

import java.util.function.BiFunction;

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Context;
import org.modelingvalue.collections.util.Pair;

public abstract class LeafTransaction extends Transaction {

    protected static final Context<LeafTransaction> CURRENT = Context.of();

    protected LeafTransaction(UniverseTransaction universeTransaction) {
        super(universeTransaction);
    }

    public Leaf leaf() {
        return (Leaf) cls();
    }

    @SuppressWarnings("rawtypes")
    protected void checkTooManyObservers(Object object, Observed observed, Set<ActionInstance> observers) {
        if (universeTransaction().maxNrOfObservers() < observers.size()) {
            throw new TooManyObserversException(object, observed, observers, universeTransaction());
        }
    }

    public static LeafTransaction getCurrent() {
        return CURRENT.get();
    }

    public static void setCurrent(LeafTransaction t) {
        CURRENT.set(t);
    }

    public abstract State state();

    public abstract <O, T, E> T set(O object, Setable<O, T> property, BiFunction<T, E, T> function, E element);

    public abstract <O, T> T set(O object, Setable<O, T> property, T post);

    public <O, T> T get(O object, Getable<O, T> property) {
        return state().get(object, property);
    }

    public <O, T> T pre(O object, Getable<O, T> property) {
        return universeTransaction().preState().get(object, property);
    }

    protected <O, T> void changed(O object, Setable<O, T> property, T preValue, T postValue) {
        property.changed(this, object, preValue, postValue);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <O> void clear(O object) {
        Map<Setable, Object> properties = state().properties(object);
        if (properties != null) {
            for (Entry<Setable, Object> e : properties) {
                set(object, e.getKey(), e.getKey().getDefault());
            }
        }
    }

    protected <O extends Mutable> void trigger(O mutable, Action<O> action, Direction direction) {
        Mutable object = mutable;
        set(object, direction.priorities[action.priority().nr], Set::add, action);
        Mutable container = dParent(object);
        while (container != null && (Direction.backward == direction || !parent().ancestorId(object))) {
            set(container, direction.depth, Set::add, object);
            object = container;
            container = dParent(object);
        }
    }

    protected Mutable dParent(Mutable object) {
        Pair<Mutable, ?> parent = state().get(object, Mutable.D_PARENT_CONTAINING);
        return parent != null ? parent.a() : null;
    }

    public void runNonObserving(Runnable action) {
        action.run();
    }

    public abstract ActionInstance actionInstance();

}
