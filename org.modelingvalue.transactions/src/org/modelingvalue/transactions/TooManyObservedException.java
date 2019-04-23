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
import org.modelingvalue.collections.util.StringUtil;

public final class TooManyObservedException extends Error {

    private static final long serialVersionUID = 2091236807252565002L;

    private final Observer    observer;
    private final Set<Slot>   observed;

    public TooManyObservedException(Observer observer, Set<Slot> observed) {
        this.observer = observer;
        this.observed = observed;
    }

    @Override
    public String getMessage() {
        String observedMap = observer.root().preState().get(() -> {
            return observed.map(String::valueOf).collect(Collectors.joining("\n  "));
        });
        return getSimpleMessage() + ":\n  " + observedMap;
    }

    public String getSimpleMessage() {
        return observer.root().preState().get(() -> {
            return "Too many observed (" + observed.size() + ") by " + (observer.parent != null ? StringUtil.toString(observer.parent.getId()) + "." : "") + StringUtil.toString(observer.getId());
        });
    }

    public int getNrOfObserved() {
        return observed.size();
    }

    public Observer getObserver() {
        return observer;
    }

    public Set<Slot> getObserved() {
        return observed;
    }
}
