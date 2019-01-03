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

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.StringUtil;

public abstract class Getable<O, T> {

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
        return currentLeaf(object).root().preState().get(object, this);
    }

    protected AbstractLeaf currentLeaf(O object) {
        AbstractLeaf current = AbstractLeaf.getCurrent();
        if (current == null) {
            throw new NullPointerException("No current transaction in " + Thread.currentThread() + " , while reading " + toString());
        } else if (object == null) {
            throw new NullPointerException("Object is null, while reading " + current.state().get((this::toString)));
        }
        return current;
    }

    public Collection<?> getCollection(O object) {
        T v = get(object);
        return v instanceof Collection ? (Collection<?>) v : v == null ? Set.of() : Set.of(v);
    }

}
