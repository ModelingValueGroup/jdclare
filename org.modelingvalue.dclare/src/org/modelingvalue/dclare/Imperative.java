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

package org.modelingvalue.dclare;

public class Imperative extends Leaf {

    public static Imperative of(Object id) {
        return new Imperative(id, Direction.forward, Priority.postDepth);
    }

    public static Imperative of(Object id, Priority priority) {
        return new Imperative(id, Direction.forward, priority);
    }

    public static Imperative of(Object id, Direction initDirection, Priority priority) {
        return new Imperative(id, initDirection, priority);
    }

    protected Imperative(Object id, Direction initDirection, Priority priority) {
        super(id, initDirection, priority);
    }

    @Override
    public Transaction openTransaction(MutableTransaction parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void closeTransaction(Transaction tx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImperativeTransaction newTransaction(UniverseTransaction universeTransaction) {
        throw new UnsupportedOperationException();
    }

}
