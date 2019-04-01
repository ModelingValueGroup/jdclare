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

public class ReadOnly extends AbstractLeaf {

    public static ReadOnly of(Object id, Root root) {
        return new ReadOnly(id, root);
    }

    private ReadOnly(Object id, Root root) {
        super(id, root, Priority.mid);
    }

    @Override
    protected State run(State state, Root root, Priority prio) {
        throw new UnsupportedOperationException();
    }

    public <R> R get(Supplier<R> action, State... states) {
        ReadOnlyRun run = startRun();
        run.states = states;
        try {
            return CURRENT.get(run, action);
        } finally {
            stopRun(run);
        }
    }

    public void run(Runnable action, State... states) {
        ReadOnlyRun run = startRun();
        run.states = states;
        try {
            CURRENT.run(run, action);
        } finally {
            stopRun(run);
        }
    }

    @Override
    protected ReadOnlyRun startRun() {
        return root().readOnlyRuns.get().open(this);
    }

    @Override
    protected void stopRun(TransactionRun<?> run) {
        root().readOnlyRuns.get().close((ReadOnlyRun) run);
    }

    protected static class ReadOnlyRun extends AbstractLeafRun<ReadOnly> {
        private State[] states;

        protected ReadOnlyRun() {
            super();
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
        protected void trigger(AbstractLeaf leaf, Priority prio) {
            // Do nothing
        }

        @Override
        protected void stop() {
            super.stop();
            states = null;
        }

    }

}
