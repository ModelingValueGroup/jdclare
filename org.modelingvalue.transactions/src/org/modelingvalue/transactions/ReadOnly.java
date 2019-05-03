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

public class ReadOnly extends Leaf {

    public static ReadOnly of(Object id) {
        return new ReadOnly(id, Direction.forward, Priority.postDepth);
    }

    public static ReadOnly of(Object id, Priority priority) {
        return new ReadOnly(id, Direction.forward, priority);
    }

    public static ReadOnly of(Object id, Direction initDirection, Priority priority) {
        return new ReadOnly(id, initDirection, priority);
    }

    protected ReadOnly(Object id, Direction initDirection, Priority priority) {
        super(id, initDirection, priority);
    }

    @Override
    public ReadOnlyTransaction openTransaction(MutableTransaction parent) {
        return parent.universeTransaction().readOnlys.get().open(this, parent);
    }

    @Override
    public void closeTransaction(Transaction tx) {
        tx.universeTransaction().readOnlys.get().close((ReadOnlyTransaction) tx);
    }

    @Override
    public ReadOnlyTransaction newTransaction(UniverseTransaction universeTransaction) {
        return new ReadOnlyTransaction(universeTransaction);
    }

}
