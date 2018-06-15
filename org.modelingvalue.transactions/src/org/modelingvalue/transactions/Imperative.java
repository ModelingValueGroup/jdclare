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

import java.util.function.Consumer;

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.TriConsumer;

public class Imperative extends Leaf {

    public static Imperative of(Object id, State init, Root root, Consumer<Runnable> scheduler, TriConsumer<State, State, Boolean> diffHandler) {
        return new Imperative(id, init, root, scheduler, diffHandler);
    }

    private final Consumer<Runnable>                 scheduler;
    private final TriConsumer<State, State, Boolean> diffHandler;
    private boolean                                  doFire;

    private Imperative(Object id, State init, Root root, Consumer<Runnable> scheduler, TriConsumer<State, State, Boolean> diffHandler) {
        super(id, root, null, Priority.high);
        init(init);
        this.scheduler = scheduler;
        this.diffHandler = diffHandler;
    }

    @Override
    public State apply(State last) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void commit(State post, boolean timeTraveling) {
        Leaf.setCurrent(this);
        Map<Pair<Object, Setable>, Object> changes = setted.result();
        State pre = pre();
        preState = null;
        init(post);
        diffHandler.accept(pre, post, changes.isEmpty());
        Map<Pair<Object, Setable>, Object> effects = setted.result();
        post = pre();
        preState = null;
        for (Entry<Pair<Object, Setable>, Object> set : effects) {
            post = post.set(set.getKey().get0(), set.getKey().get1(), set.getValue());
        }
        init(post);
        if (!changes.isEmpty() || (!timeTraveling && !effects.isEmpty())) {
            Map<Pair<Object, Setable>, Object> diff = effects.putAll(changes);
            root().put(Pair.of(this, "toDClare"), () -> {
                for (Entry<Pair<Object, Setable>, Object> set : diff) {
                    set.getKey().get1().set(set.getKey().get0(), set.getValue());
                }
            });
        } else if (timeTraveling) {
            run(() -> run(() -> doFire = true));
        } else {
            doFire = true;
        }
    }

    @Override
    protected boolean merge() {
        return false;
    }

    @Override
    protected void trigger(Set<Observer> leafs, Priority prio, Object object, Object observed, Object post) {
        // Do nothing
    }

    @Override
    protected void trigger(Leaf leaf, Priority prio) {
        // Do nothing
    }

    @Override
    protected <O, T> void changed(O object, Setable<O, T> property, T preValue, T postValue) {
        super.changed(object, property, preValue, postValue);
        if (doFire) {
            doFire = false;
            root().dummy();
        }
    }

    public void run(Runnable action) {
        scheduler.accept(action);
    }

}
