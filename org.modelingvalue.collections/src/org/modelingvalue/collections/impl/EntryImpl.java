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

package org.modelingvalue.collections.impl;

import java.util.Objects;

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.util.Age;
import org.modelingvalue.collections.util.Internable;
import org.modelingvalue.collections.util.StringUtil;

public final class EntryImpl<K, V> implements Entry<K, V> {

    private static final long serialVersionUID = 3714329073858453623L;

    private final K           key;
    private V                 value;

    public EntryImpl(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return (key == null ? 0 : key.hashCode() * 31) + (value == null ? 0 : value.hashCode());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        EntryImpl<K, V> other = (EntryImpl) obj;
        if (!Objects.equals(key, other.key)) {
            return false;
        } else if (value == other.value) {
            return true;
        } else if (value == null || !value.equals(other.value)) {
            return false;
        } else if (Age.age(value) > Age.age(other.value)) {
            other.value = value;
            return true;
        } else {
            value = other.value;
            return true;
        }
    }

    @Override
    public String toString() {
        return "Entry[" + StringUtil.toString(key) + "," + StringUtil.toString(value) + "]";
    }

    @Override
    public void setValueIfEqual(V value) {
        if (this.value != value && Objects.equals(this.value, value)) {
            this.value = value;
        }
    }

    @Override
    public boolean isInternable() {
        return Internable.isInternable(key) && Internable.isInternable(value);
    }

}
