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

public final class TransactionException extends RuntimeException {

    private static final long      serialVersionUID = 4787416569147173605L;

    protected static final int     MAX_STACK_DEPTH  = Integer.getInteger("MAX_STACK_DEPTH", 4);

    private final TransactionClass cls;

    public TransactionException(State state, TransactionClass cls, Throwable cause) {
        super("Exception in transaction \"" + state.get(() -> cls.toString()) + "\"", cause);
        this.cls = cls;
        StackTraceElement[] est = getStackTrace();
        setStackTrace(Arrays.copyOf(est, Math.min(est.length, MAX_STACK_DEPTH)));
        if (!(cause instanceof TransactionException)) {
            est = getStackTrace();
            StackTraceElement[] tst = cause.getStackTrace();
            cause.setStackTrace(Arrays.copyOf(tst, Math.min(tst.length, cause.getCause() instanceof TransactionException ? MAX_STACK_DEPTH : reduceStackLength(est, tst))));
        }

    }

    public TransactionClass getTransactionClass() {
        return cls;
    }

    private int reduceStackLength(StackTraceElement[] outer, StackTraceElement[] inner) {
        for (int i = 0; i < inner.length; i++) {
            for (int o = 0; o < outer.length; o++) {
                if (inner[i].equals(outer[o])) {
                    return i + 2;
                }
            }
        }
        return inner.length;
    }

    public TransactionException getTransactionCause() {
        TransactionException t = this;
        while (t.getCause() instanceof TransactionException) {
            t = (TransactionException) t.getCause();
        }
        return t;
    }

    public Throwable getNonTransactionCause() {
        Throwable t = this;
        while (t instanceof TransactionException) {
            t = t.getCause();
        }
        return t;
    }

}
