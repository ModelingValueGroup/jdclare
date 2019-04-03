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

import java.util.ArrayList;
import java.util.function.Supplier;

import org.modelingvalue.transactions.Transaction.TransactionRun;

public class TransactionRunsList<T extends Transaction, R extends TransactionRun<T>> extends ArrayList<R> {

    private static final long serialVersionUID = 9116265671882887291L;

    private static final int  CHUNCK_SIZE      = 4;

    private final Supplier<R> creator;

    private int               level            = -1;

    public TransactionRunsList(Supplier<R> creator) {
        super(0);
        this.creator = creator;
    }

    public R open(T transaction, Root root) {
        if (++level >= size()) {
            ensureCapacity(size() + CHUNCK_SIZE);
            for (int i = 0; i < CHUNCK_SIZE; i++) {
                add(creator.get());
            }
        }
        R run = get(level);
        run.start(transaction, root);
        return run;
    }

    public void close(R run) {
        run.stop();
        for (; level >= 0 && !get(level).isOpen(); level--) {
        }
    }

}
