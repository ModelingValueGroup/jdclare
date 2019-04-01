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

public final class TooManyChangesException extends Error {
    private static final long   serialVersionUID = 7857822332170335179L;

    private final State         state;
    private final int           nrOfChanges;
    private final ObserverTrace last;

    public TooManyChangesException(State state, ObserverTrace last, int nrOfChanges) {
        this.state = state;
        this.last = last;
        this.nrOfChanges = nrOfChanges;

    }

    @Override
    public String getMessage() {
        String message = "" + nrOfChanges;
        return state.get(() -> last.trace("\n  ", message, last.observer().root().maxNrOfChanges()));
    }

    public State getState() {
        return state;
    }

    public Observer getObserver() {
        return last.observer();
    }

    public ObserverTrace getLast() {
        return last;
    }

    public int getNrOfChanges() {
        return nrOfChanges;
    }

}
