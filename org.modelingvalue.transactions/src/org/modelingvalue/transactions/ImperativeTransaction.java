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

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.TriConsumer;

public class ImperativeTransaction extends LeafTransaction {

    public static ImperativeTransaction of(Leaf cls, State init, UniverseTransaction universeTransaction, Consumer<Runnable> scheduler, TriConsumer<State, State, Boolean> diffHandler) {
        return new ImperativeTransaction(cls, init, universeTransaction, scheduler, diffHandler);
    }

    private static Setable<ImperativeTransaction, Long> CHANGE_NR = Setable.of("CHANGE_NR", 0l);

    private final Consumer<Runnable>                    scheduler;

    @SuppressWarnings("rawtypes")
    private Set<Pair<Object, Setable>>                  setted;
    private State                                       pre;
    private State                                       state;
    private TriConsumer<State, State, Boolean>          diffHandler;

    protected ImperativeTransaction(Leaf cls, State init, UniverseTransaction universeTransaction, Consumer<Runnable> scheduler, TriConsumer<State, State, Boolean> diffHandler) {
        super(universeTransaction);
        this.pre = init;
        this.state = init;
        this.setted = Set.of();
        this.diffHandler = diffHandler;
        super.start(cls, universeTransaction);
        this.scheduler = r -> scheduler.accept(() -> {
            LeafTransaction.setCurrent(this);
            try {
                r.run();
            } catch (Throwable t) {
                pre = universeTransaction.handleException(pre, t);
            }
        });
    }

    @Override
    public void stop() {
        super.stop();
        pre = null;
        state = null;
        setted = null;
        diffHandler = null;
        LeafTransaction.setCurrent(null);
    }

    public void schedule(Runnable action) {
        scheduler.accept(action);
    }

    public void commit(State post, boolean timeTraveling) {
        extern2intern();
        intern2extern(post, timeTraveling);
    }

    @Override
    public State state() {
        return state;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void extern2intern() {
        if (pre != state) {
            State finalPre = pre;
            CHANGE_NR.set(this, (n, i) -> n + i, 1);
            State finalState = state;
            pre = finalState;
            universeTransaction().put(Pair.of(this, "$toDClare"), () -> {
                try {
                    finalPre.diff(finalState, o -> true, s -> true).forEach(s -> {
                        Object o = s.getKey();
                        for (Entry<Setable, Pair<Object, Object>> d : s.getValue()) {
                            d.getKey().set(o, d.getValue().b());
                        }
                    });
                } catch (Throwable t) {
                    CHANGE_NR.set(ImperativeTransaction.this, finalState.get(ImperativeTransaction.this, CHANGE_NR));
                    pre = universeTransaction().handleException(pre, t);
                }
            });
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void intern2extern(State post, boolean timeTraveling) {
        if (pre != post) {
            State finalState = state;
            boolean last = post.get(this, CHANGE_NR).equals(finalState.get(this, CHANGE_NR));
            if (last) {
                setted = Set.of();
            } else {
                for (Pair<Object, Setable> slot : setted) {
                    post = post.set(slot.a(), slot.b(), finalState.get(slot.a(), slot.b()));
                }
            }
            state = post;
            if (!timeTraveling) {
                pre = state;
            }
            diffHandler.accept(finalState, post, last);
            if (timeTraveling) {
                pre = state;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <O, T> T set(O object, Setable<O, T> property, T post) {
        T[] old = (T[]) new Object[1];
        boolean first = pre == state;
        state = state.set(object, property, post, old);
        changed(object, property, old[0], post, first);
        return old[0];
    }

    @SuppressWarnings("unchecked")
    @Override
    public <O, T, E> T set(O object, Setable<O, T> property, BiFunction<T, E, T> function, E element) {
        T[] oldNew = (T[]) new Object[2];
        boolean first = pre == state;
        state = state.set(object, property, function, element, oldNew);
        changed(object, property, oldNew[0], oldNew[1], first);
        return oldNew[0];
    }

    private <O, T> void changed(O object, Setable<O, T> property, T preValue, T postValue, boolean first) {
        if (!Objects.equals(preValue, postValue)) {
            setted = setted.add(Pair.of(object, property));
            if (first) {
                universeTransaction().dummy();
            }
            changed(object, property, preValue, postValue);
        }
    }

    @Override
    protected State run(State state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ActionInstance actionInstance() {
        throw new UnsupportedOperationException();
    }

}
