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

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.ContainingCollection;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Internable;
import org.modelingvalue.collections.util.StringUtil;

public abstract class Getable<O, T> implements Internable {

    protected final Object id;
    protected final T      def;

    protected Getable(Object id, T def) {
        this.id = id;
        this.def = def;
    }

    public T getDefault() {
        return def;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        } else {
            return id.equals(((Getable) obj).id);
        }
    }

    @Override
    public String toString() {
        return StringUtil.toString(id);
    }

    public Object id() {
        return id;
    }

    public T get(O object) {
        return currentLeaf(object).get(object, this);
    }

    public T pre(O object) {
        return currentLeaf(object).pre(object, this);
    }

    protected LeafTransaction currentLeaf(O object) {
        LeafTransaction current = LeafTransaction.getCurrent();
        if (current == null) {
            throw new NullPointerException("No current transaction in " + Thread.currentThread() + " , while accessing " + toString());
        } else if (object == null) {
            throw new NullPointerException("Object is null, while accessing " + current.state().get((this::toString)));
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    public <E> Collection<E> getCollection(O object) {
        T v = get(object);
        return v instanceof Collection ? (Collection<E>) v : v == null ? Set.of() : Set.of((E) v);
    }

    public boolean containment() {
        return false;
    }

    public Supplier<Setable<?, ?>> opposite() {
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T, E> Collection<Mutable> mutables(T value) {
        if (value instanceof ContainingCollection) {
            return ((ContainingCollection<Object>) value).filter(Mutable.class);
        } else if (value instanceof Mutable) {
            return Set.of((Mutable) value);
        } else {
            return Set.of();
        }
    }

}
