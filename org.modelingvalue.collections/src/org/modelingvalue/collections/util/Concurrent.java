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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.function.UnaryOperator;

public class Concurrent<T> {

    private static final int THREAD_MAX = Integer.getInteger("THREAD_MAX", 16);

    public static <V> Concurrent<V> of(V value) {
        return new Concurrent<V>(value);
    }

    public static <V> Concurrent<V> of() {
        return new Concurrent<V>();
    }

    private T   pre;
    private T[] states;

    private Concurrent(T value) {
        init(value);
    }

    protected Concurrent() {
    }

    public boolean change(UnaryOperator<T> oper) {
        if (pre == null) {
            throw new ConcurrentModificationException();
        }
        int i = ContextThread.getNr();
        T value = oper.apply(states[i]);
        if (states[i] != value) {
            states[i] = value;
            return true;
        } else {
            return false;
        }
    }

    public T get() {
        if (pre == null) {
            throw new ConcurrentModificationException();
        }
        int i = ContextThread.getNr();
        return states[i];
    }

    public boolean set(T value) {
        if (pre == null) {
            throw new ConcurrentModificationException();
        }
        int i = ContextThread.getNr();
        if (states[i] != value) {
            states[i] = value;
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public void init(T value) {
        if (pre != null) {
            throw new ConcurrentModificationException();
        }
        pre = value;
        if (states == null) {
            states = (T[]) Array.newInstance(value.getClass(), THREAD_MAX);
        }
        Arrays.fill(states, value);
    }

    public T result() {
        if (pre == null) {
            throw new ConcurrentModificationException();
        }
        int l = 0;
        for (int i = 0; i < states.length; i++) {
            if (states[i] != pre) {
                states[l++] = states[i];
            }
        }
        T result = Mergeables.merge(pre, (p, a) -> merge(p, a), states, l);
        Arrays.fill(states, null);
        pre = null;
        return result;

    }

    public void clear() {
        if (pre != null) {
            Arrays.fill(states, null);
            pre = null;
        }
    }

    @SuppressWarnings("unchecked")
    protected T merge(T base, T[] branches) {
        if (base instanceof Mergeable) {
            return ((Mergeable<T>) base).merge(branches);
        } else {
            throw new ConcurrentModificationException();
        }
    }

    public T pre() {
        if (pre == null) {
            throw new ConcurrentModificationException();
        }
        return pre;
    }

}
