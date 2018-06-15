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

package org.modelingvalue.collections.struct.impl;

import org.modelingvalue.collections.struct.Struct13;

@SuppressWarnings("unchecked")
public class Struct13Impl<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> extends Struct12Impl<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> implements Struct13<T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> {

    private static final long serialVersionUID = -8517021813710134661L;

    public Struct13Impl(T0 t0, T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12) {
        this((Object) t0, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12);
    }

    protected Struct13Impl(Object... data) {
        super(data);
    }

    public T12 get12() {
        return (T12) get(12);
    }
}
