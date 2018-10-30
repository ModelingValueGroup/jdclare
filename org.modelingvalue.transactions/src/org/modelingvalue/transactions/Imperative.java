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

public class Imperative extends AbstractLeaf {

    public static Imperative of(Object id, State init, Root root, Consumer<Runnable> scheduler, TriConsumer<State, State, Boolean> diffHandler) {
        return new Imperative(id, init, root, scheduler, diffHandler);
    }

    private static Setable<Imperative, Long>         CHANGE_NR = Setable.of("CHANGE_NR", 0l);

    private final Consumer<Runnable>                 scheduler;
    private final TriConsumer<State, State, Boolean> diffHandler;
    private State                                    pre;
    private State                                    state;

    private Imperative(Object id, State init, Root root, Consumer<Runnable> scheduler, TriConsumer<State, State, Boolean> diffHandler) {
        super(id, root, Priority.high);
        this.state = init;
        this.pre = state();
        this.scheduler = r -> scheduler.accept(() -> {
            AbstractLeaf.setCurrent(this);
            r.run();
        });
        this.diffHandler = diffHandler;
    }

    @Override
    public State apply(State last) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void commit(State post, boolean timeTraveling) {
        Long changeNr = pre.get(this, CHANGE_NR);
        State now = state();
        if (now != pre) {
            State finalPre = pre;
            root().put(Pair.of(this, "toDClare"), () -> {
                CHANGE_NR.set(Imperative.this, changeNr + 1);
                finalPre.diff(now, o -> true, s -> true).forEach(s -> {
                    Object o = s.getKey();
                    for (Entry<Setable, Pair<Object, Object>> d : s.getValue()) {
                        d.getKey().set(o, d.getValue().b());
                    }
                });
            });
            CHANGE_NR.set(this, changeNr + 1);
            pre = state();
        } else if (post.get(this, CHANGE_NR).equals(changeNr)) {
            state = post;
            if (!timeTraveling) {
                pre = state();
            }
            diffHandler.accept(pre, post, true);
            if (timeTraveling) {
                pre = state();
            }
        }
    }

    @Override
    protected void trigger(Set<Observer> leafs, Priority prio, Object object, Object observed, Object post) {
        // Do nothing
    }

    @Override
    protected void trigger(AbstractLeaf leaf, Priority prio) {
        // Do nothing
    }

    public void schedule(Runnable action) {
        scheduler.accept(action);
    }

    @Override
    public State state() {
        return state;
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
            if (first) {
                root().dummy();
            }
            changed(object, property, preValue, postValue);
        }
    }

}
