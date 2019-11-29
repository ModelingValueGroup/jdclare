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

import java.util.stream.Collectors;

import org.modelingvalue.collections.DefaultMap;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.StringUtil;

public final class TooManyObservedException extends ConsistencyError {

    private static final long                        serialVersionUID = 2091236807252565002L;

    private final Observer<?>                        observer;
    @SuppressWarnings("rawtypes")
    private final DefaultMap<Observed, Set<Mutable>> observed;
    private final UniverseTransaction                universeTransaction;

    @SuppressWarnings("rawtypes")
    public TooManyObservedException(Mutable mutable, Observer<?> observer, DefaultMap<Observed, Set<Mutable>> observed, UniverseTransaction universeTransaction) {
        super(mutable, observer, universeTransaction.preState().get(() -> {
            return "Too many observed (" + LeafTransaction.size(observed) + ") by " + StringUtil.toString(mutable) + "." + StringUtil.toString(observer);
        }));
        this.observer = observer;
        this.observed = observed;
        this.universeTransaction = universeTransaction;
    }

    @Override
    public String getMessage() {
        String observedMap = universeTransaction.preState().get(() -> {
            return observed.map(String::valueOf).collect(Collectors.joining("\n  "));
        });
        return getSimpleMessage() + ":\n  " + observedMap;
    }

    public String getSimpleMessage() {
        return super.getMessage();
    }

    public int getNrOfObserved() {
        return LeafTransaction.size(observed);
    }

    public Observer<?> getObserver() {
        return observer;
    }

    @SuppressWarnings("rawtypes")
    public DefaultMap<Observed, Set<Mutable>> getObserved() {
        return observed;
    }
}
