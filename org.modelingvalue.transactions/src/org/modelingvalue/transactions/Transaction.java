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

import java.util.ConcurrentModificationException;
import java.util.Objects;

import org.modelingvalue.collections.util.StringUtil;

public abstract class Transaction {

    private final Object     id;
    protected final Compound parent;
    private final int        hashCode;

    protected Transaction(Object id, Compound parent) {
        this.id = id;
        this.parent = parent;
        this.hashCode = parent != null ? (id.hashCode() * 31 + parent.hashCode()) : id.hashCode();
    }

    protected final Object getId() {
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
        Compound p = parent;
        while (!(p instanceof Root)) {
            p = p.parent;
        }
        return (Root) p;
    }

    protected abstract State run(State state, Root root);

    public abstract boolean isAncestorOf(Compound child);

    public abstract static class TransactionRun<T extends Transaction> {

        private T    transaction;
        private Root root;

        protected TransactionRun() {
        }

        public boolean isOpen() {
            return transaction != null;
        }

        public Root root() {
            if (root == null) {
                throw new ConcurrentModificationException();
            }
            return root;
        }

        public Compound parent() {
            return transaction().parent();
        }

        public T transaction() {
            if (transaction == null) {
                throw new ConcurrentModificationException();
            }
            return transaction;
        }

        protected void start(T transaction, Root root) {
            if (this.transaction != null) {
                throw new ConcurrentModificationException();
            }
            this.transaction = transaction;
            this.root = root;
        }

        protected void stop() {
            if (transaction == null) {
                throw new ConcurrentModificationException();
            }
            transaction = null;
            root = null;
        }

        @Override
        public String toString() {
            return transaction != null ? (transaction + "#Run") : super.toString();
        }

    }

}
