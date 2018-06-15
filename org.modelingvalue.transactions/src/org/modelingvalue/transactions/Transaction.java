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

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.function.Function;

import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.StringUtil;

public abstract class Transaction implements Function<State, State>, Mergeable<Transaction> {

    private final Object     id;
    protected final Compound parent;
    private final int        hashCode;

    protected Transaction(Object id, Compound parent) {
        this.id = id;
        this.parent = parent;
        this.hashCode = parent != null ? (id.hashCode() * 31 + parent.hashCode()) : id.hashCode();
    }

    Transaction(Object id) {
        this.hashCode = id.hashCode();
        this.id = id;
        this.parent = null;
    }

    public Object getId() {
        return id;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + (parent != null ? StringUtil.toString(parent.getId()) + "." : "") + StringUtil.toString(id);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        } else {
            Transaction transaction = (Transaction) obj;
            return id.equals(transaction.id) && Objects.equals(parent, transaction.parent);
        }
    }

    public Compound parent() {
        return parent;
    }

    public Root root() {
        return parent.root();
    }

    public abstract boolean isAncestorOf(Transaction child);

    public Compound commonAncestor(Transaction transaction) {
        Compound c = this instanceof Compound ? (Compound) this : parent;
        while (!c.isAncestorOf(transaction)) {
            c = c.parent;
        }
        return c;
    }

    @Override
    public Transaction merge(Transaction[] branches) {
        Transaction r = null;
        for (Transaction b : branches) {
            if (b != null) {
                if (r == null) {
                    r = b;
                } else if (!r.equals(b)) {
                    throw new ConcurrentModificationException(this + " -> " + Arrays.toString(branches));
                }
            }
        }
        return r;
    }

    @Override
    public Transaction getMerger() {
        throw new UnsupportedOperationException();
    }

}
