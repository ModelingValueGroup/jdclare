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

import java.util.Arrays;

import org.modelingvalue.collections.struct.Struct;
import org.modelingvalue.collections.util.StringUtil;

public abstract class StructImpl implements Struct {

    private static final long serialVersionUID = -1849579252791770119L;

    private Object[]          data;

    protected StructImpl(Object... data) {
        this.data = postCreate(data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj.getClass() != getClass()) {
            return false;
        }
        StructImpl other = (StructImpl) obj;
        if (other.data == data) {
            return true;
        } else if (!Arrays.equals(data, other.data)) {
            return false;
        }
        data = other.data;
        return true;
    }

    @Override
    public Object get(int i) {
        return data[i];
    }

    protected Object set(int i, Object val) {
        return data[i] = val;
    }

    @Override
    public int length() {
        return data.length;
    }

    @Override
    public String toString() {
        Class<? extends StructImpl> clazz = getClass();
        Class<?>[] interfaces = clazz.getInterfaces();
        return (interfaces.length == 0 ? clazz : interfaces[0]).getSimpleName() + StringUtil.toString(data);
    }

}
