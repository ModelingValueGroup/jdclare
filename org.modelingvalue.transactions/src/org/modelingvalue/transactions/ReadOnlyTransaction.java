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

    private State state;

    protected ReadOnlyTransaction(UniverseTransaction universeTransaction) {
        super(universeTransaction);
    }

    public final ReadOnly readOnlyCls() {
        return (ReadOnly) cls();
    }

    @Override
    protected State run(State state) {
        throw new UnsupportedOperationException();
    }

    public <R> R get(Supplier<R> action, State state) {
        this.state = state;
        try {
            return CURRENT.get(this, action);
        } finally {
            this.state = null;
        }
    }

    public void run(Runnable action, State state) {
        this.state = state;
        try {
            CURRENT.run(this, action);
        } finally {
            this.state = null;
        }
    }

    @Override
    public State state() {
        return state;
    }

    @Override
    public <O, T> T get(O object, Getable<O, T> property) {
        return state.get(object, property);
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void stop() {
        super.stop();
        state = null;
    }

}
