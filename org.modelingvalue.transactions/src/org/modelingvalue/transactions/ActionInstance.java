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
public class ActionInstance extends Struct2Impl<Mutable, Action> {

    private static final long serialVersionUID = -6386034125368913879L;

    public static ActionInstance of(Mutable object, Action action) {
        return new ActionInstance(object, action);
    }

    private ActionInstance(Mutable object, Action action) {
        super(object, action);
    }

    public Mutable mutable() {
        return get0();
    }

    public Mutable mutable(Mutable mutable) {
        return mutable() == This.THIS ? mutable : mutable();
    }

    public ActionInstance actionInstance(Mutable mutable) {
        return mutable() == This.THIS ? ActionInstance.of(mutable, action()) : this;
    }

    public Action action() {
        return get1();
    }

    public boolean isInternable() {
        return mutable() == This.THIS;
    }

}
