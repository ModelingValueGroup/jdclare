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

import java.util.Objects;
import java.util.function.Function;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.util.Context;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.QuadConsumer;
import org.modelingvalue.collections.util.StringUtil;
import org.modelingvalue.collections.util.TraceTimer;

public class ConstantSetable<O, T> extends Setable<O, T> {

    public static final Context<ConstantSetable<?, ?>> CURRENT = Context.of();

    public static <C, V> ConstantSetable<C, V> of(Object id, Function<C, V> deriver) {
        return new ConstantSetable<C, V>(id, null, deriver, null);
    }

    public static <C, V> ConstantSetable<C, V> of(Object id, V def, Function<C, V> deriver) {
        return new ConstantSetable<C, V>(id, def, deriver, null);
    }

    public static <C, V> ConstantSetable<C, V> of(Object id, V def, Function<C, V> deriver, QuadConsumer<AbstractLeaf, C, V, V> changed) {
        return new ConstantSetable<C, V>(id, def, deriver, changed);
    }

    public static <C, V> ConstantSetable<C, V> of(Object id, Function<C, V> deriver, QuadConsumer<AbstractLeaf, C, V, V> changed) {
        return new ConstantSetable<C, V>(id, null, deriver, changed);
    }

    private final Function<O, T>            deriver;
    private final Setable<O, Identified<T>> delegate;

    protected ConstantSetable(Object id, T def, Function<O, T> deriver, QuadConsumer<AbstractLeaf, O, T, T> changed) {
        super(id, def, null);
        delegate = new ValueHolder<O, Identified<T>>(id, //
                changed != null ? ($, o, b, a) -> changed.accept($, o, b == null ? def : b.get(), //
                        a == null ? def : a.get()) : null);
        this.deriver = deriver;
    }

    @Override
    public T set(O object, T value) {
        if (deriver != null) {
            throw new Error("Constant " + this + " is derived");
        }
        Identified<T> val = delegate.get(object);
        if (val == null) {
            delegate.set(object, Identified.of(object, value));
            return def;
        } else if (Objects.equals(val.get(), value)) {
            return val.get();
        } else {
            throw new Error("Constant " + this + " already set");
        }
    }

    public T force(O object, T value) {
        delegate.set(object, Identified.of(object, value));
        return def;
    }

    @Override
    public T get(O object) {
        Identified<T> val = delegate.get(object);
        if (val == null) {
            if (deriver == null) {
                throw new Error("Constant " + this + " not yet set");
            }
            val = CURRENT.get(this, () -> {
                TraceTimer.traceBegin("lazy");
                try {
                    Identified<T> v = Identified.of(object, deriver.apply(object));
                    delegate.set(object, v);
                    return v;
                } catch (ConstantSetableException lce) {
                    lce.addLazy(object, ConstantSetable.this);
                    throw lce;
                } catch (Throwable t) {
                    throw new ConstantSetableException(object, ConstantSetable.this, t);
                } finally {
                    TraceTimer.traceEnd("lazy");
                }
            });
        }
        return val.get();

    }

    public Setable<O, Identified<T>> getDelegate() {
        return delegate;
    }

    private final class ValueHolder<A, B> extends Setable<A, B> {

        private ValueHolder(Object id, QuadConsumer<AbstractLeaf, A, B, B> changed) {
            super(id, null, changed);
        }

        @Override
        public boolean isDerived() {
            return deriver != null;
        }
    }

    public static final class Identified<T> {

        public static <O> Identified<O> of(Object id, O obj) {
            return new Identified<O>(id, obj);
        }

        private final Object id;
        private final T      obj;

        private Identified(Object id, T obj) {
            this.id = id;
            this.obj = obj;
        }

        public T get() {
            return obj;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (!(obj instanceof Identified)) {
                return false;
            }
            Identified other = (Identified) obj;
            return id.equals(other.id);
        }

        @Override
        public String toString() {
            return StringUtil.toString(obj);
        }

    }

    public static final class ConstantSetableException extends RuntimeException {

        private static final long                         serialVersionUID = -6980064786088373917L;

        private List<Pair<Object, ConstantSetable<?, ?>>> list             = List.of();

        public ConstantSetableException(Object object, ConstantSetable<?, ?> lazy, Throwable cause) {
            super(cause);
            addLazy(object, lazy);
        }

        private void addLazy(Object object, ConstantSetable<?, ?> lazy) {
            list = list.prepend(Pair.of(object, lazy));
        }

        @Override
        public String getMessage() {
            return "Exception while deriving Lazy Constants " + list;
        }

    }

}
