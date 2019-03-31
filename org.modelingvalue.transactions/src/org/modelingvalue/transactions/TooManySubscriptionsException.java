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

import org.modelingvalue.collections.Set;

@SuppressWarnings("rawtypes")
public final class TooManySubscriptionsException extends Error {

    private static final long serialVersionUID = 2091236807252565002L;

    private final Observer    observer;
    private final Observed    observed;
    private final Set         set;

    public TooManySubscriptionsException(Observer observer, Observed observed, Set set) {
        this.observer = observer;
        this.observed = observed;
        this.set = set;
    }

    @Override
    public String getMessage() {
        if (observer != null) {
            return "Too many observed " + set.size() + " by " + observer + " " + set;
        } else {
            return "Too many observers " + set.size() + " of " + observed + " " + set;
        }
    }

    public int getNrOfSubscribtions() {
        return set.size();
    }

    public Observer getObserver() {
        return observer;
    }

    public Observed getObserved() {
        return observed;
    }

}
