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
import java.util.function.Function;
import java.util.function.Supplier;

import org.modelingvalue.collections.DefaultMap;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.util.Context;
import org.modelingvalue.collections.util.QuadConsumer;

public class Constant<O, T> extends Setable<O, T> {

    public static final Context<Constant<?, ?>> DERIVED = Context.of(null);

    public static <C, V> Constant<C, V> of(Object id, Function<C, V> deriver) {
        return new Constant<C, V>(id, null, false, null, deriver, null);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, Function<C, V> deriver) {
        return new Constant<C, V>(id, def, false, null, deriver, null);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, Function<C, V> deriver, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Constant<C, V>(id, def, false, null, deriver, changed);
    }

    public static <C, V> Constant<C, V> of(Object id, Function<C, V> deriver, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Constant<C, V>(id, null, false, null, deriver, changed);
    }

    public static <C, V> Constant<C, V> of(Object id, boolean containment, Function<C, V> deriver) {
        return new Constant<C, V>(id, null, containment, null, deriver, null);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, boolean containment, Function<C, V> deriver) {
        return new Constant<C, V>(id, def, containment, null, deriver, null);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, boolean containment, Function<C, V> deriver, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Constant<C, V>(id, def, containment, null, deriver, changed);
    }

    public static <C, V> Constant<C, V> of(Object id, boolean containment, Function<C, V> deriver, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Constant<C, V>(id, null, containment, null, deriver, changed);
    }

    public static <C, V> Constant<C, V> of(Object id, Supplier<Setable<?, ?>> opposite, Function<C, V> deriver) {
        return new Constant<C, V>(id, null, false, opposite, deriver, null);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, Supplier<Setable<?, ?>> opposite, Function<C, V> deriver) {
        return new Constant<C, V>(id, def, false, opposite, deriver, null);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, Supplier<Setable<?, ?>> opposite, Function<C, V> deriver, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Constant<C, V>(id, def, false, opposite, deriver, changed);
    }

    public static <C, V> Constant<C, V> of(Object id, Supplier<Setable<?, ?>> opposite, Function<C, V> deriver, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Constant<C, V>(id, null, false, opposite, deriver, changed);
    }

    private final Function<O, T> deriver;

    protected Constant(Object id, T def, boolean containment, Supplier<Setable<?, ?>> opposite, Function<O, T> deriver, QuadConsumer<LeafTransaction, O, T, T> changed) {
        super(id, def, containment, opposite, changed);
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

    @SuppressWarnings("rawtypes")
    @Override
    protected Entry<Setable, Object> entry(T value, DefaultMap<Setable, Object> properties) {
        return Entry.of(this, value);
    }

}
