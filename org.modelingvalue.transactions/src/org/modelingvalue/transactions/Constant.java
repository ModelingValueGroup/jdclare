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

public class Constant<O, T> extends Setable<O, T> {

    public static final Context<Integer> DEPTH = Context.of(0);

    public static <C, V> Constant<C, V> of(Object id, Function<C, V> deriver) {
        return new Constant<C, V>(id, null, false, deriver, null);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, Function<C, V> deriver) {
        return new Constant<C, V>(id, def, false, deriver, null);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, Function<C, V> deriver, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Constant<C, V>(id, def, false, deriver, changed);
    }

    public static <C, V> Constant<C, V> of(Object id, Function<C, V> deriver, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Constant<C, V>(id, null, false, deriver, changed);
    }

    public static <C, V> Constant<C, V> of(Object id, boolean containment, Function<C, V> deriver) {
        return new Constant<C, V>(id, null, containment, deriver, null);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, boolean containment, Function<C, V> deriver) {
        return new Constant<C, V>(id, def, containment, deriver, null);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, boolean containment, Function<C, V> deriver, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Constant<C, V>(id, def, containment, deriver, changed);
    }

    public static <C, V> Constant<C, V> of(Object id, boolean containment, Function<C, V> deriver, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Constant<C, V>(id, null, containment, deriver, changed);
    }

    private final Function<O, T> deriver;

    protected Constant(Object id, T def, boolean containment, Function<O, T> deriver, QuadConsumer<LeafTransaction, O, T, T> changed) {
        super(id, def, containment, changed);
        this.deriver = deriver;
    }

    public Function<O, T> deriver() {
        return deriver;
    }

    @Override
    public <E> T set(O object, BiFunction<T, E, T> function, E element) {
        LeafTransaction leafTransaction = LeafTransaction.getCurrent();
        ConstantState constants = leafTransaction.universeTransaction().constantState;
        return constants.set(leafTransaction, object, this, function, element);
    }

    @Override
    public T set(O object, T value) {
        if (deriver != null) {
            throw new Error("Constant " + this + " is derived");
        }
        LeafTransaction leafTransaction = LeafTransaction.getCurrent();
        ConstantState constants = leafTransaction.universeTransaction().constantState;
        return constants.set(leafTransaction, object, this, value);
    }

    public T force(O object, T value) {
        LeafTransaction leafTransaction = LeafTransaction.getCurrent();
        ConstantState constants = leafTransaction.universeTransaction().constantState;
        return constants.set(leafTransaction, object, this, value);
    }

    @Override
    public T get(O object) {
        LeafTransaction leafTransaction = LeafTransaction.getCurrent();
        ConstantState constants = leafTransaction.universeTransaction().constantState;
        return constants.get(leafTransaction, object, this);
    }

    @Override
    public T pre(O object) {
        return get(object);
    }

    @Override
    protected void changed(LeafTransaction leafTransaction, O object, T preValue, T postValue) {
        if ((containment || changed != null) && !Objects.equals(preValue, postValue)) {
            Action.of(Triple.of(object, this, "changed"), o -> super.changed(leafTransaction, object, preValue, postValue)).trigger(leafTransaction.parent().mutable());
        }
    }

}
