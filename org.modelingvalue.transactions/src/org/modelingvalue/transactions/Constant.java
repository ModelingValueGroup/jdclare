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
import java.util.function.BiFunction;
import java.util.function.Function;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.util.Context;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.QuadConsumer;
import org.modelingvalue.collections.util.StringUtil;
import org.modelingvalue.collections.util.Triple;

public class Constant<O, T> extends Setable<O, T> {

    private static final Object                 EMPTY   = new Object() {
                                                            @Override
                                                            public String toString() {
                                                                return "EMPTY";
                                                            }
                                                        };

    public static final Context<Constant<?, ?>> CURRENT = Context.of();

    public static <C, V> Constant<C, V> of(Object id, Function<C, V> deriver) {
        return new Constant<C, V>(id, null, deriver, null);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, Function<C, V> deriver) {
        return new Constant<C, V>(id, def, deriver, null);
    }

    public static <C, V> Constant<C, V> of(Object id, V def, Function<C, V> deriver, QuadConsumer<AbstractLeaf, C, V, V> changed) {
        return new Constant<C, V>(id, def, deriver, changed);
    }

    public static <C, V> Constant<C, V> of(Object id, Function<C, V> deriver, QuadConsumer<AbstractLeaf, C, V, V> changed) {
        return new Constant<C, V>(id, null, deriver, changed);
    }

    private final Function<O, T> deriver;
    private final T              def;

    @SuppressWarnings("unchecked")
    protected Constant(Object id, T def, Function<O, T> deriver, QuadConsumer<AbstractLeaf, O, T, T> changed) {
        super(id, (T) EMPTY, changed);
        this.def = def;
        this.deriver = deriver;
    }

    @Override
    public <E> T set(O object, BiFunction<T, E, T> function, E element) {
        AbstractLeaf leaf = AbstractLeaf.getCurrent();
        Root root = leaf.root();
        State prev = root.constantState.get();
        T val = prev.get(object, this);
        T value = function.apply(val == EMPTY ? def : val, element);
        set(object, value, leaf, root, prev, val);
        return def;
    }

    @Override
    public T set(O object, T value) {
        if (deriver != null) {
            throw new Error("Constant " + this + " is derived");
        }
        AbstractLeaf leaf = AbstractLeaf.getCurrent();
        Root root = leaf.root();
        State prev = root.constantState.get();
        T val = prev.get(object, this);
        set(object, value, leaf, root, prev, val);
        return def;
    }

    private void set(O object, T value, AbstractLeaf leaf, Root root, State prev, T val) throws NonDeterministicException {
        if (val == EMPTY) {
            set(root, leaf, prev, object, value);
        } else if (!Objects.equals(val, value)) {
            throw new NonDeterministicException("Constant is not consistent " + StringUtil.toString(object) + "." + this + "=" + StringUtil.toString(val) + "!=" + StringUtil.toString(value));
        }
    }

    public T force(O object, T value) {
        AbstractLeaf leaf = AbstractLeaf.getCurrent();
        Root root = leaf.root();
        set(root, leaf, root.constantState.get(), object, value);
        return def;
    }

    @Override
    public T get(O object) {
        AbstractLeaf leaf = AbstractLeaf.getCurrent();
        Root root = leaf.root();
        return get(root, leaf, object);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private T get(Root root, AbstractLeaf leaf, O object) throws Error {
        State prev = root.constantState.get();
        T val = prev.get(object, this);
        if (val == EMPTY) {
            if (deriver == null) {
                throw new Error("Constant " + this + " not yet set");
            }
            try {
                return CURRENT.get(this, () -> set(root, leaf, prev, object, deriver.apply(object)));
            } catch (ConstantSetableException lce) {
                if (!(lce.getCause() instanceof StackOverflowError) || CURRENT.get() != null) {
                    lce.addLazy(object, Constant.this);
                    throw lce;
                } else {
                    for (Pair<Object, Constant> lazy : lce.list) {
                        if (equals(lazy.b()) && object.equals(lazy.a())) {
                            Pair<Object, Constant> me = Pair.of(object, this);
                            throw new NonDeterministicException("Circular constant definition: " + lce.list.sublist(lce.list.lastIndexOf(me), lce.list.size()).add(me));
                        }
                        lazy.b().get(root, leaf, lazy.a());
                    }
                    return get(root, leaf, object);
                }
            } catch (Throwable t) {
                throw new ConstantSetableException(object, Constant.this, t);
            }
        }
        return val;
    }

    private T set(Root root, AbstractLeaf leaf, State prev, O object, T value) {
        State next = prev.set(object, this, value);
        while (!root.constantState.compareAndSet(prev, next)) {
            prev = root.constantState.get();
            T val = prev.get(object, this);
            if (val != EMPTY) {
                return val;
            }
            next = prev.set(object, this, value);
        }
        changed(leaf, object, def, value);
        return value;
    }

    @Override
    protected void changed(AbstractLeaf leaf, O object, T preValue, T postValue) {
        if (changed != null) {
            Leaf.of(Triple.of(object, this, "changed"), leaf.parent(), () -> super.changed(leaf, object, preValue, postValue)).trigger();
        }
    }

    @SuppressWarnings("rawtypes")
    private static final class ConstantSetableException extends RuntimeException {

        private static final long            serialVersionUID = -6980064786088373917L;

        private List<Pair<Object, Constant>> list             = List.of();

        public ConstantSetableException(Object object, Constant lazy, Throwable cause) {
            super(cause);
            addLazy(object, lazy);
        }

        private void addLazy(Object object, Constant lazy) {
            list = list.append(Pair.of(object, lazy));
        }

        @Override
        public String getMessage() {
            return "Exception while deriving Lazy Constants " + list;
        }

    }

}
