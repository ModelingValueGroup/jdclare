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

import java.lang.reflect.Array;
import java.util.Objects;

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.util.Age;
import org.modelingvalue.collections.util.Internable;
import org.modelingvalue.collections.util.Mergeables;
import org.modelingvalue.collections.util.StringUtil;

public final class EntryImpl<K, V> implements Entry<K, V> {

    private static final long  serialVersionUID = 3714329073858453623L;

    @SuppressWarnings("rawtypes")
    private static final Entry NULL             = Entry.of(null, null);

    private final K            key;
    private V                  value;

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

    @SuppressWarnings("unchecked")
    @Override
    public Entry<K, V> merge(Entry<K, V>[] branches) {
        V[] vs = null;
        K key = getKey();
        if (getValue() != null) {
            Class<?> vc = getValue().getClass();
            vs = (V[]) Array.newInstance(vc, branches.length);
        }
        for (int i = 0; i < branches.length; i++) {
            if (branches[i] != null) {
                if (key == null) {
                    key = branches[i].getKey();
                }
                if (branches[i].getValue() != null) {
                    if (vs == null) {
                        Class<?> vc = branches[i].getValue().getClass();
                        vs = (V[]) Array.newInstance(vc, branches.length);
                    }
                    vs[i] = branches[i].getValue();
                }
            }
        }
        V v = Mergeables.merge(getValue(), vs, vs.length);
        if (Objects.equals(getValue(), v) && Objects.equals(getKey(), key)) {
            return this;
        } else {
            for (int i = 0; i < branches.length; i++) {
                if (branches[i] != null) {
                    if (Objects.equals(branches[i].getValue(), v)) {
                        return branches[i];
                    }
                }
            }
        }
        return Entry.of(key, v);
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

    @SuppressWarnings("unchecked")
    @Override
    public Entry<K, V> getMerger() {
        return NULL;
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
