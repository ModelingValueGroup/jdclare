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
import java.util.function.Supplier;

import org.modelingvalue.collections.Set;

public class ReadOnly extends Leaf {

    public static ReadOnly of(Object id, Root root) {
        return new ReadOnly(id, root);
    }

    private State[] states;

    private ReadOnly(Object id, Root root) {
        super(id, root, null, Priority.high);
    }

    @Override
    public State apply(State state) {
        throw new UnsupportedOperationException();
    }

    public <R> R get(Supplier<R> action, State... states) {
        this.states = states;
        try {
            return CURRENT.get(this, action);
        } finally {
            this.states = null;
        }
    }

    public void run(Runnable action, State... states) {
        this.states = states;
        try {
            CURRENT.run(this, action);
        } finally {
            this.states = null;
        }
    }

    @Override
    public <O, T> T get(O object, Getable<O, T> property) {
        T def = property.getDefault();
        for (State state : states) {
            T val = state.get(object, property);
            if (val != def) {
                return val;
            }
        }
        return def;
    }

    @Override
    public <O, T, E> T set(O object, Setable<O, T> property, BiFunction<T, E, T> function, E element) {
        if (ConstantSetable.CURRENT.get() == null) {
            throw new NonDeterministicException("Change on '" + property + "' while NOT initializing constant");
        }
        T pre = get(object, property);
        T post = function.apply(pre, element);
        states[0] = states[0].set(object, property, post);
        changed(object, property, pre, post);
        return pre;
    }

    @Override
    public <O, T> T set(O object, Setable<O, T> property, T post) {
        if (ConstantSetable.CURRENT.get() == null) {
            throw new NonDeterministicException("Change on '" + property + "' while NOT initializing constant");
        }
        T pre = get(object, property);
        states[0] = states[0].set(object, property, post);
        changed(object, property, pre, post);
        return pre;
    }

    @Override
    protected void trigger(Set<Observer> leafs, Priority prio, Object object, Object observed, Object post) {
        // Do nothing
    }

    @Override
    protected void trigger(Leaf leaf, Priority prio) {
        // Do nothing
    }

}
