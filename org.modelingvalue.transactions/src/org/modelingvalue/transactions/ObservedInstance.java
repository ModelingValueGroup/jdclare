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

import org.modelingvalue.collections.struct.impl.Struct2Impl;

@SuppressWarnings("rawtypes")
public class ObservedInstance extends Struct2Impl<Mutable, Observed> {

    private static final long serialVersionUID = 5217882450935295451L;

    public static ObservedInstance of(Mutable mutable, Observed property) {
        return new ObservedInstance(mutable, property);
    }

    private ObservedInstance(Object object, Observed property) {
        super(object, property);
    }

    public Mutable mutable() {
        return get0();
    }

    public Mutable mutable(Mutable mutable) {
        return mutable() == This.THIS ? mutable : mutable();
    }

    public ActionInstance observerInstance(Mutable mutable, Observer observer) {
        return mutable() == This.THIS ? observer.thisInstance : ActionInstance.of(mutable, observer);
    }

    public Observed property() {
        return get1();
    }

    public Observed<?, ?> observed() {
        return get1();
    }

    public boolean isInternable() {
        return mutable() == This.THIS || mutable() instanceof Universe;
    }

}
