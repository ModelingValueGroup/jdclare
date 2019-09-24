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
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Context;
import org.modelingvalue.collections.util.QuadConsumer;

public class Constant<O, T> extends Setable<O, T> {

    public static final Context<Constant<?, ?>> DERIVED = Context.of(null);

    public static <C, V> Constant<C, V> of(Object id, V def) {
        return new Constant<C, V>(id, def, false, null, null, null, null, true);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Constant<C, V>(id, def, false, null, null, null, changed, true);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, boolean containment) {
        return new Constant<C, V>(id, def, containment, null, null, null, null, true);
    }

    public static <C, V> Constant<C, V> of(Object id, Function<C, V> deriver) {
        return new Constant<C, V>(id, null, false, null, null, deriver, null, true);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, Function<C, V> deriver) {
        return new Constant<C, V>(id, def, false, null, null, deriver, null, true);
    }

    public static <C, V> Constant<C, V> of(Object id, Function<C, V> deriver, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Constant<C, V>(id, null, false, null, null, deriver, changed, true);
    }

    public static <C, V> Constant<C, V> of(Object id, boolean containment, Function<C, V> deriver) {
        return new Constant<C, V>(id, null, containment, null, null, deriver, null, true);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, boolean containment, Function<C, V> deriver, boolean checkConsistency) {
        return new Constant<C, V>(id, def, containment, null, null, deriver, null, checkConsistency);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, Supplier<Setable<?, ?>> opposite, Supplier<Setable<C, Set<?>>> scope, Function<C, V> deriver, boolean checkConsistency) {
        return new Constant<C, V>(id, def, false, opposite, scope, deriver, null, checkConsistency);
    }

    private final Function<O, T> deriver;

    protected Constant(Object id, T def, boolean containment, Supplier<Setable<?, ?>> opposite, Supplier<Setable<O, Set<?>>> scope, Function<O, T> deriver, QuadConsumer<LeafTransaction, O, T, T> changed, boolean checkConsistency) {
        super(id, def, containment, opposite, scope, changed, checkConsistency);
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
        return constants.set(leafTransaction, object, this, value, false);
    }

    public T force(O object, T value) {
        LeafTransaction leafTransaction = LeafTransaction.getCurrent();
        ConstantState constants = leafTransaction.universeTransaction().constantState;
        return constants.set(leafTransaction, object, this, value, true);
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
