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

public class ReadOnlyTransaction extends LeafTransaction {

    private State[] states;

    protected ReadOnlyTransaction(UniverseTransaction universeTransaction) {
        super(universeTransaction);
    }

    public ReadOnly readOnlyCls() {
        return (ReadOnly) cls();
    }

    @Override
    protected State run(State state) {
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
    public State state() {
        return states[0];
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
        throw new UnsupportedOperationException();
    }

    @Override
    public <O, T> T set(O object, Setable<O, T> property, T post) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ActionInstance actionInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected <O extends Mutable> void trigger(O mutable, Action<O> action, Direction direction) {
        // Do nothing
    }

    @Override
    protected void stop() {
        super.stop();
        states = null;
    }

}
