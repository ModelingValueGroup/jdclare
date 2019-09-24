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

import java.util.function.Supplier;

import org.modelingvalue.collections.ContainingCollection;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.QuadConsumer;

public class MandatoryObserved<O, T> extends Observed<O, T> {

    public static <C, V> MandatoryObserved<C, V> of(Object id, V def, boolean containment) {
        return new MandatoryObserved<C, V>(id, def, containment, null, null, null, true);
    }

    public static <C, V> MandatoryObserved<C, V> of(Object id, V def, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new MandatoryObserved<C, V>(id, def, false, null, null, changed, true);
    }

    public static <C, V> MandatoryObserved<C, V> of(Object id, V def, Supplier<Setable<?, ?>> opposite) {
        return new MandatoryObserved<C, V>(id, def, false, opposite, null, null, true);
    }

    public static <C, V> MandatoryObserved<C, V> of(Object id, V def) {
        return new MandatoryObserved<C, V>(id, def, false, null, null, null, true);
    }

    public static <C, V> MandatoryObserved<C, V> of(Object id, V def, boolean containment, boolean checkConsistency) {
        return new MandatoryObserved<C, V>(id, def, containment, null, null, null, checkConsistency);
    }

    public static <C, V> MandatoryObserved<C, V> of(Object id, V def, Supplier<Setable<?, ?>> opposite, Supplier<Setable<C, Set<?>>> scope, boolean checkConsistency) {
        return new MandatoryObserved<C, V>(id, def, false, opposite, scope, null, checkConsistency);
    }

    protected MandatoryObserved(Object id, T def, boolean containment, Supplier<Setable<?, ?>> opposite, Supplier<Setable<O, Set<?>>> scope, QuadConsumer<LeafTransaction, O, T, T> changed, boolean checkConsistency) {
        super(id, def, containment, opposite, scope, changed, checkConsistency);
    }

    @Override
    public T get(O object) {
        T result = super.get(object);
        if (check(result)) {
            throw new EmptyMandatoryException(object, this);
        } else {
            return result;
        }
    }

    @Override
    public T pre(O object) {
        T result = super.pre(object);
        if (check(result)) {
            return get(object);
        } else {
            return result;
        }
    }

    @SuppressWarnings("rawtypes")
    protected boolean check(T result) {
        return result == null || (result instanceof ContainingCollection && ((ContainingCollection) result).isEmpty());
    }

    @Override
    public boolean checkConsistency() {
        return checkConsistency;
    }

    @Override
    public void checkConsistency(State state, O object, T pre, T post) {
        if (super.checkConsistency()) {
            super.checkConsistency(state, object, pre, post);
        }
        if (check(post)) {
            throw new EmptyMandatoryException(object, this);
        }
    }

}
