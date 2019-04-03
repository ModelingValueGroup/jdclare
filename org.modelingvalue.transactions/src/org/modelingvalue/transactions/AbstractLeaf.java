<<<<<<< HEAD
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Context;

public abstract class AbstractLeaf extends Transaction {

    protected static final Context<AbstractLeafRun<?>> CURRENT = Context.of();

    private final Priority                             initPrio;

    protected AbstractLeaf(Object id, Compound parent, Priority initPrio) {
        super(id, parent);
        this.initPrio = initPrio;
    }

    protected Priority initPrio() {
        return initPrio;
    }

    public void trigger() {
        AbstractLeafRun<?> leaf = getCurrent();
        leaf.trigger(this, initPrio);
    }

    @Override
    public boolean isAncestorOf(Transaction child) {
        return false;
    }

    @SuppressWarnings("rawtypes")
    protected void checkTooManyObservers(AbstractLeafRun<?> run, Observed observed, Set<Observer> observers) {
        if (run.root().maxNrOfObservers() < observers.size()) {
            throw new TooManyObserversException(observed, observers);
        }
    }

    public static AbstractLeafRun<?> getCurrent() {
        return CURRENT.get();
    }

    public static Consumer<AbstractLeaf> consumer(Runnable action) {
        return tx -> {
            AbstractLeafRun<?> run = tx.startRun();
            try {
                CURRENT.run(run, action);
            } finally {
                run.stop();
            }
        };
    }

    public static <R> Function<AbstractLeaf, R> function(Supplier<R> supplier) {
        return tx -> {
            AbstractLeafRun<?> run = tx.startRun();
            try {
                return CURRENT.get(run, supplier);
            } finally {
                run.stop();
            }
        };
    }

    public static void setCurrent(AbstractLeafRun<?> t) {
        CURRENT.set(t);
    }

    @Override
    protected abstract AbstractLeafRun<?> startRun();

    public abstract static class AbstractLeafRun<L extends AbstractLeaf> extends TransactionRun<L> {

        protected AbstractLeafRun() {
            super();
        }

        public abstract State state();

        public abstract <O, T, E> T set(O object, Setable<O, T> property, BiFunction<T, E, T> function, E element);

        public abstract <O, T> T set(O object, Setable<O, T> property, T post);

        public <O, T> T get(O object, Getable<O, T> property) {
            if (property instanceof Observed && Constant.DEPTH.get() > 0) {
                throw new NonDeterministicException("Reading observed '" + property + "' while initializing constant");
            }
            return state().get(object, property);
        }

        public <O, T> T pre(O object, Getable<O, T> property) {
            return root().preState().get(object, property);
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

        protected void trigger(AbstractLeaf leaf, Priority prio) {
            set(transaction().commonAncestor(leaf.parent), prio.leafTriggered, Set::add, leaf);
        }

        public void runNonObserving(Runnable action) {
            action.run();
        }

    }

}
=======
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

public abstract class AbstractLeaf extends Transaction {

    protected static final Context<AbstractLeafRun<?>> CURRENT = Context.of();

    private final Priority                             initPrio;

    protected AbstractLeaf(Object id, Compound parent, Priority initPrio) {
        super(id, parent);
        this.initPrio = initPrio;
    }

    protected Priority initPrio() {
        return initPrio;
    }

    public void trigger() {
        AbstractLeafRun<?> leaf = getCurrent();
        leaf.trigger(this, initPrio);
    }

    @Override
    public boolean isAncestorOf(Transaction child) {
        return false;
    }

    @SuppressWarnings("rawtypes")
    protected void checkTooManyObservers(AbstractLeafRun<?> run, Object object, Observed observed, Set<Observer> observers) {
        if (run.root().maxNrOfObservers() < observers.size()) {
            throw new TooManyObserversException(observed, observers);
        }
    }

    public static AbstractLeafRun<?> getCurrent() {
        return CURRENT.get();
    }

    public static void setCurrent(AbstractLeafRun<?> t) {
        CURRENT.set(t);
    }

    @Override
    protected abstract AbstractLeafRun<?> startRun(Root root);

    public abstract static class AbstractLeafRun<L extends AbstractLeaf> extends TransactionRun<L> {

        protected AbstractLeafRun() {
            super();
        }

        public abstract State state();

        public abstract <O, T, E> T set(O object, Setable<O, T> property, BiFunction<T, E, T> function, E element);

        public abstract <O, T> T set(O object, Setable<O, T> property, T post);

        public <O, T> T get(O object, Getable<O, T> property) {
            if (property instanceof Observed && Constant.DEPTH.get() > 0) {
                throw new NonDeterministicException("Reading observed '" + property + "' while initializing constant");
            }
            return state().get(object, property);
        }

        public <O, T> T pre(O object, Getable<O, T> property) {
            return root().preState().get(object, property);
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

        protected void trigger(AbstractLeaf leaf, Priority prio) {
            set(transaction().commonAncestor(leaf.parent).getId(), prio.leafTriggered, Set::add, leaf);
        }

        public void runNonObserving(Runnable action) {
            action.run();
        }

    }

}
>>>>>>> 3cc8cd322e80e50522ccb92930bf0e991a23d0ea
