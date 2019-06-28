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

import java.util.ArrayList;

public class Reusable<U, C, T, P> extends ArrayList<T> {

    private static final long                      serialVersionUID = 9116265671882887291L;

    private static final int                       CHUNCK_SIZE      = 4;

    private final U                                init;
    private final SerializableBiFunction<C, U, T>  construct;
    private final SerializableTriConsumer<T, C, P> start;
    private final SerializableConsumer<T>          stop;
    private final SerializableFunction<T, Boolean> isOpen;

    private int                                    level            = -1;

    public Reusable(U init, SerializableBiFunction<C, U, T> construct, SerializableTriConsumer<T, C, P> start, SerializableConsumer<T> stop, SerializableFunction<T, Boolean> isOpen) {
        super(0);
        this.init = init;
        this.construct = construct;
        this.start = start;
        this.stop = stop;
        this.isOpen = isOpen;
    }

    public T open(C cls, P parent) {
        if (++level >= size()) {
            ensureCapacity(size() + CHUNCK_SIZE);
            for (int i = 0; i < CHUNCK_SIZE; i++) {
                add(construct.apply(cls, init));
            }
        }
        T tx = get(level);
        start.accept(tx, cls, parent);
        return tx;
    }

    public void close(T tx) {
        stop.accept(tx);
        for (; level >= 0 && !isOpen.apply(get(level)); level--) {
        }
    }

}
