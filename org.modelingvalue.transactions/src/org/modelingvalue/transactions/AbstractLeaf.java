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

    protected static final Context<AbstractLeaf> CURRENT = Context.of();

    private final Priority                       initPrio;

    protected AbstractLeaf(Object id, Compound parent, Priority initPrio) {
        super(id, parent);
        this.initPrio = initPrio;
    }

    protected Priority initPrio() {
        return initPrio;
    }

    public void trigger() {
        AbstractLeaf leaf = getCurrent();
        leaf.trigger(leaf.commonAncestor(this, initPrio), this, initPrio, null, null, null, null);
    }

    public abstract State state();

    @Override
    public boolean isAncestorOf(Transaction child) {
        return false;
    }

    public <O, T> T get(O object, Getable<O, T> property) {
        if (property instanceof Observed && Constant.DEPTH.get() > 0) {
            throw new NonDeterministicException("Reading observed '" + property + "' while initializing constant");
        }
        return state().get(object, property);
    }

    public <O, T> T pre(O object, Getable<O, T> property) {
        return root().preState().get(object, property);
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

    public abstract <O, T, E> T set(O object, Setable<O, T> property, BiFunction<T, E, T> function, E element);

    public abstract <O, T> T set(O object, Setable<O, T> property, T post);

    @SuppressWarnings("rawtypes")
    protected void trigger(Compound common, AbstractLeaf leaf, Priority prio, Object object, Setable setable, Object pre, Object post) {
        set(common, prio.triggered, Set::add, leaf);
    }

    protected <O, T> void changed(O object, Setable<O, T> property, T preValue, T postValue) {
        property.changed(this, object, preValue, postValue);
    }

    public static Consumer<AbstractLeaf> consumer(Runnable action) {
        return $ -> CURRENT.run($, action);
    }

    public static <R> Function<AbstractLeaf, R> function(Supplier<R> supplier) {
        return $ -> CURRENT.get($, supplier);
    }

    public static AbstractLeaf getCurrent() {
        return CURRENT.get();
    }

    public static void setCurrent(AbstractLeaf t) {
        CURRENT.set(t);
    }

    public void runNonObserving(Runnable action) {
        action.run();
    }

}
