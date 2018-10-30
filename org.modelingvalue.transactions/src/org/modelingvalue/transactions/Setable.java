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

import org.modelingvalue.collections.ContainingCollection;
import org.modelingvalue.collections.util.QuadConsumer;

public class Setable<O, T> extends Getable<O, T> {

    public static <C, V> Setable<C, V> of(Object id, V def) {
        return of(id, def, null);
    }

    public static <C, V> Setable<C, V> of(Object id, V def, QuadConsumer<AbstractLeaf, C, V, V> changed) {
        return new Setable<C, V>(id, def, changed);
    }

    protected QuadConsumer<AbstractLeaf, O, T, T> changed;

    protected Setable(Object id, T def, QuadConsumer<AbstractLeaf, O, T, T> changed) {
        super(id, def);
        this.changed = changed;
    }

    protected void changed(AbstractLeaf $, O object, T preValue, T postValue) {
        if (changed != null) {
            changed.accept($, object, preValue, postValue);
        }
    }

    public T set(O object, T value) {
        return currentLeaf(object).set(object, this, value);
    }

    public <E> T set(O object, BiFunction<T, E, T> function, E element) {
        return currentLeaf(object).set(object, this, function, element);
    }

    @SuppressWarnings("unchecked")
    public static <T, E> void diff(T def, T pre, T post, Consumer<E> added, Consumer<E> removed) {
        if (def instanceof ContainingCollection) {
            ((ContainingCollection<Object>) pre).compare((ContainingCollection<Object>) post).forEach(d -> {
                if (d[0] == null) {
                    d[1].forEach(a -> added.accept((E) a));
                }
                if (d[1] == null) {
                    d[0].forEach(r -> removed.accept((E) r));
                }
            });
        } else {
            if (pre != null) {
                removed.accept((E) pre);
            }
            if (post != null) {
                added.accept((E) post);
            }
        }
    }

    public boolean isDerived() {
        return false;
    }
}
