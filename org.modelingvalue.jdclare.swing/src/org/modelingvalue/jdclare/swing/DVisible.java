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

package org.modelingvalue.jdclare.swing;

import static org.modelingvalue.jdclare.DClare.*;

import java.util.function.BiFunction;

import org.modelingvalue.collections.util.SerializableFunction;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DNative;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.Native;
import org.modelingvalue.jdclare.swing.DVisible.VisibleNative;

@Native(VisibleNative.class)
public interface DVisible extends DObject {

    class VisibleNative<N extends DVisible> implements DNative<N> {

        protected final N  visible;

        protected DVisible parent;

        public VisibleNative(N visible) {
            this.visible = visible;
        }

        protected static <O extends DVisible, V> void set(O object, SerializableFunction<O, V> propert, V value) {
            VisibleNative<O> n = dNative(object);
            n.set(propert, value);
        }

        protected <V> void set(SerializableFunction<N, V> property, V value) {
            DClare.set(visible, property, value);
        }

        protected static <O extends DVisible, E, V> void set(O object, SerializableFunction<O, V> property, BiFunction<V, E, V> function, E value) {
            VisibleNative<O> n = dNative(object);
            n.set(property, function, value);
        }

        protected <E, V> void set(SerializableFunction<N, V> property, BiFunction<V, E, V> function, E value) {
            DClare.set(visible, property, function, value);
        }

        @Override
        public void init(DObject parent) {
            if (parent instanceof DVisible) {
                this.parent = (DVisible) parent;
            }
        }

        @Override
        public void exit(DObject parent) {
            if (parent instanceof DVisible) {
                this.parent = null;
            }
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        protected <V extends VisibleNative> V ancestor(Class<V> cls) {
            return cls.isInstance(this) ? (V) this : ((VisibleNative<?>) dNative(parent)).ancestor(cls);
        }

        @Override
        public int hashCode() {
            return visible.hashCode();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (getClass() != obj.getClass()) {
                return false;
            } else {
                VisibleNative other = (VisibleNative) obj;
                return visible.equals(other.visible);
            }
        }

    }

}
