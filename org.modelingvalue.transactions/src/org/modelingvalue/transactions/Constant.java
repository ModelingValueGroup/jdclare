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
import java.util.function.Function;

import org.modelingvalue.collections.util.Context;
import org.modelingvalue.collections.util.QuadConsumer;
import org.modelingvalue.collections.util.Triple;
import org.modelingvalue.transactions.AbstractLeaf.AbstractLeafRun;

public class Constant<O, T> extends Setable<O, T> {

    public static final Context<Integer> DEPTH = Context.of(0);

    public static <C, V> Constant<C, V> of(Object id, Function<C, V> deriver) {
        return new Constant<C, V>(id, null, deriver, null);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, Function<C, V> deriver) {
        return new Constant<C, V>(id, def, deriver, null);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, Function<C, V> deriver, QuadConsumer<AbstractLeafRun<?>, C, V, V> changed) {
        return new Constant<C, V>(id, def, deriver, changed);
    }

    public static <C, V> Constant<C, V> of(Object id, Function<C, V> deriver, QuadConsumer<AbstractLeafRun<?>, C, V, V> changed) {
        return new Constant<C, V>(id, null, deriver, changed);
    }

    private final Function<O, T> deriver;

    protected Constant(Object id, T def, Function<O, T> deriver, QuadConsumer<AbstractLeafRun<?>, O, T, T> changed) {
        super(id, def, changed);
        this.deriver = deriver;
    }

    public Function<O, T> deriver() {
        return deriver;
    }

    @Override
    public <E> T set(O object, BiFunction<T, E, T> function, E element) {
        AbstractLeafRun<?> leaf = AbstractLeaf.getCurrent();
        ConstantState constants = leaf.root().constantState;
        return constants.set(leaf, object, this, function, element);
    }

    @Override
    public T set(O object, T value) {
        if (deriver != null) {
            throw new Error("Constant " + this + " is derived");
        }
        AbstractLeafRun<?> leaf = AbstractLeaf.getCurrent();
        ConstantState constants = leaf.root().constantState;
        return constants.set(leaf, object, this, value);
    }

    public T force(O object, T value) {
        AbstractLeafRun<?> leaf = AbstractLeaf.getCurrent();
        ConstantState constants = leaf.root().constantState;
        return constants.set(leaf, object, this, value);
    }

    @Override
    public T get(O object) {
        AbstractLeafRun<?> leaf = AbstractLeaf.getCurrent();
        ConstantState constants = leaf.root().constantState;
        return constants.get(leaf, object, this);
    }

    @Override
    public T pre(O object) {
        return get(object);
    }

    @Override
    protected void changed(AbstractLeafRun<?> leaf, O object, T preValue, T postValue) {
        if (changed != null && !Objects.equals(preValue, postValue)) {
            Leaf.of(Action.of(Triple.of(object, this, "changed"), o -> super.changed(leaf, object, preValue, postValue)), leaf.parent()).trigger();
        }
    }

}
