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

import org.modelingvalue.collections.util.QuadConsumer;

public class InternableObserved<O, T> extends Observed<O, T> {

    public static <C, V> InternableObserved<C, V> of(Object id, V def) {
        return new InternableObserved<C, V>(id, def, false, null, null);
    }

    public static <C, V> InternableObserved<C, V> of(Object id, V def, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new InternableObserved<C, V>(id, def, false, null, changed);
    }

    public static <C, V> InternableObserved<C, V> of(Object id, V def, boolean containment) {
        return new InternableObserved<C, V>(id, def, containment, null, null);
    }

    public static <C, V> InternableObserved<C, V> of(Object id, V def, Supplier<Setable<?, ?>> opposite) {
        return new InternableObserved<C, V>(id, def, false, opposite, null);
    }

    public static <C, V> InternableObserved<C, V> of(Object id, V def, boolean containment, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new InternableObserved<C, V>(id, def, containment, null, changed);
    }

    public static <C, V> InternableObserved<C, V> of(Object id, V def, Supplier<Setable<?, ?>> opposite, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new InternableObserved<C, V>(id, def, false, opposite, changed);
    }

    protected InternableObserved(Object id, T def, boolean containment, Supplier<Setable<?, ?>> opposite, QuadConsumer<LeafTransaction, O, T, T> changed) {
        super(id, def, containment, opposite, changed);
    }

    @Override
    public boolean isInternable(T value) {
        return true;
    }

}
