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

import java.util.stream.Collectors;

import org.modelingvalue.collections.Set;

@SuppressWarnings("rawtypes")
public final class TooManyObserversException extends Error {

    private static final long   serialVersionUID = -1059588522731393631L;
    private final Set<Observer> observers;
    private final Observed      observed;

    public TooManyObserversException(Observed observed, Set<Observer> observers) {
        this.observers = observers;
        this.observed = observed;
    }

    @Override
    public String getMessage() {
        return getSimpleMessage() + ":\n" + observers.map(String::valueOf).collect(Collectors.joining("\n  "));
    }

    public String getSimpleMessage() {
        return "Too many observers (" + observers.size() + ") of " + observed;
    }

    public int getNrOfObservers() {
        return observers.size();
    }

    public Set<Observer> getObservers() {
        return observers;
    }

    public Observed getObserved() {
        return observed;
    }

}
