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

package org.modelingvalue.collections.util;

import org.modelingvalue.collections.struct.impl.Struct2Impl;

public class Pair<A, B> extends Struct2Impl<A, B> {

    private static final long serialVersionUID = -4958481941140818352L;

    public static <X, Y> Pair<X, Y> of(X a, Y b) {
        return new Pair<X, Y>(a, b);
    }

    protected Pair(A a, B b) {
        super(a, b);
    }

    public A a() {
        return get0();
    }

    public B b() {
        return get1();
    }

}
