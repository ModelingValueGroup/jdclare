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
import org.modelingvalue.collections.struct.impl.Struct2Impl;
import org.modelingvalue.collections.util.Mergeables;

public final class EntryImpl<K, V> extends Struct2Impl<K, V> implements Entry<K, V> {

    private static final long  serialVersionUID = 3714329073858453623L;

    @SuppressWarnings("rawtypes")
    private static final Entry NULL             = Entry.of(null, null);

    public EntryImpl(K key, V value) {
        super(key, value);
    }

    @Override
    public K getKey() {
        return get0();
    }

    @Override
    public V getValue() {
        return get1();
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

    @SuppressWarnings("unchecked")
    @Override
    public Entry<K, V> getMerger() {
        return NULL;
    }

    @Override
    public void prune(V value) {
        if (getValue().equals(value)) {
            set(1, value);
        }
    }

}
