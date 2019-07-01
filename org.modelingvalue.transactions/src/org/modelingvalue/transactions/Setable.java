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

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.modelingvalue.collections.ContainingCollection;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.util.Context;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.QuadConsumer;

public class Setable<O, T> extends Getable<O, T> {

    private static final Context<Boolean> MOVING = Context.of(false);

    public static <C, V> Setable<C, V> of(Object id, V def) {
        return new Setable<C, V>(id, def, false, null, null);
    }

    public static <C, V> Setable<C, V> of(Object id, V def, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Setable<C, V>(id, def, false, null, changed);
    }

    public static <C, V> Setable<C, V> of(Object id, V def, boolean containment) {
        return new Setable<C, V>(id, def, containment, null, null);
    }

    public static <C, V> Setable<C, V> of(Object id, V def, Supplier<Setable<?, ?>> opposite) {
        return new Setable<C, V>(id, def, false, opposite, null);
    }

    public static <C, V> Setable<C, V> of(Object id, V def, boolean containment, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Setable<C, V>(id, def, containment, null, changed);
    }

    public static <C, V> Setable<C, V> of(Object id, V def, Supplier<Setable<?, ?>> opposite, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Setable<C, V>(id, def, false, opposite, changed);
    }

    protected QuadConsumer<LeafTransaction, O, T, T>  changed;
    protected final boolean                           containment;
    private final Supplier<Setable<?, ?>>             opposite;
    @SuppressWarnings("rawtypes")
    private final Constant<T, Entry<Setable, Object>> internal;

    protected Setable(Object id, T def, boolean containment, Supplier<Setable<?, ?>> opposite, QuadConsumer<LeafTransaction, O, T, T> changed) {
        super(id, def);
        this.containment = containment;
        this.changed = changed;
        this.opposite = opposite;
        if (containment && opposite != null) {
            throw new Error("The containment setable " + this + " has an opposite");
        }
        this.internal = this instanceof Constant ? null : Constant.of(Pair.of(this, "internalEntry"), v -> Entry.of(this, v));
    }

    @SuppressWarnings("rawtypes")
    protected Entry<Setable, Object> entry(T value, Map<Setable, Object> properties) {
        if (isInternable(value)) {
            return internal.get(value);
        } else {
            Entry<Setable, Object> entry = Entry.of(this, value);
            if (properties != null) {
                deduplicate(entry, properties);
            }
            return entry;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void deduplicate(Entry e1, Map<?, ?> map2) {
        Object v1 = e1.getValue();
        if (v1 instanceof Map) {
            for (Entry e3 : (Map<?, ?>) v1) {
                deduplicate(e3, map2);
            }
        } else {
            for (Entry e2 : map2) {
                Object v2 = e2.getValue();
                if (v2 instanceof Map) {
                    deduplicate(e1, (Map) v2);
                } else {
                    e1.setValueIfEqual(v2);
                }
            }
        }
    }

    @Override
    public boolean containment() {
        return containment;
    }

    @Override
    public Supplier<Setable<?, ?>> opposite() {
        return opposite;
    }

    @SuppressWarnings("unchecked")
    protected void changed(LeafTransaction tx, O object, T preValue, T postValue) {
        if (changed != null) {
            changed.accept(tx, object, preValue, postValue);
        }
        if (containment) {
            Setable.<T, Mutable> diff(preValue, postValue, added -> {
                Mutable preParent = Mutable.D_PARENT.get(added);
                Setable<Mutable, ?> preContaining = Mutable.D_CONTAINING.get(added);
                if (preParent != null) {
                    MOVING.run(true, () -> preContaining.remove(preParent, added));
                }
                Mutable.D_PARENT.set(added, (Mutable) object);
                Mutable.D_CONTAINING.set(added, (Setable<Mutable, ?>) this);
                if (preParent == null) {
                    added.dActivate();
                }
            }, removed -> {
                if (!MOVING.get()) {
                    removed.dDeactivate();
                    Mutable.D_PARENT.set(removed, null);
                    Mutable.D_CONTAINING.set(removed, null);
                }
            });
        } else if (opposite != null) {
            Setable<Object, ?> opp = (Setable<Object, ?>) opposite.get();
            Setable.<T, Object> diff(preValue, postValue, added -> {
                opp.add(added, object);
            }, removed -> {
                opp.remove(removed, object);
            });
        }
    }

    public T setDefault(O object) {
        return currentLeaf(object).set(object, this, getDefault());
    }

    public T set(O object, T value) {
        return currentLeaf(object).set(object, this, value);
    }

    public <E> T set(O object, BiFunction<T, E, T> function, E element) {
        return currentLeaf(object).set(object, this, function, element);
    }

    @SuppressWarnings({"unchecked", "unlikely-arg-type"})
    public <E> void add(O obj, E e) {
        set(obj, (v, r) -> {
            if (v instanceof ContainingCollection) {
                return (T) ((ContainingCollection<E>) v).addUnique(r);
            } else if (!r.equals(v)) {
                return (T) r;
            }
            return v;
        }, e);
    }

    @SuppressWarnings({"unchecked", "unlikely-arg-type"})
    public <E> void remove(O obj, E e) {
        set(obj, (v, r) -> {
            if (v instanceof ContainingCollection) {
                return (T) ((ContainingCollection<E>) v).remove(r);
            } else if (r.equals(v)) {
                return null;
            }
            return v;
        }, e);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean isInternable(Object value) {
        if (value instanceof Boolean || value instanceof Byte || value instanceof Character) {
            return true;
        } else if (value instanceof Internable) {
            return ((Internable) value).isInternable();
        } else if (value instanceof Integer) {
            int i = (int) value;
            return i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE;
        } else if (value instanceof Short) {
            short s = (short) value;
            return s >= Byte.MIN_VALUE && s <= Byte.MAX_VALUE;
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map) value;
            return map.allMatch(e -> (e.getKey() instanceof Internable && ((Internable) e.getKey()).isInternable()) && //
                    (e.getValue() instanceof Internable && ((Internable) e.getValue()).isInternable()));
        } else if (value instanceof ContainingCollection) {
            ContainingCollection cc = (ContainingCollection) value;
            return cc.allMatch(e -> e instanceof Internable && ((Internable) e).isInternable());
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, E> void diff(T pre, T post, Consumer<E> added, Consumer<E> removed) {
        if (pre instanceof ContainingCollection && post instanceof ContainingCollection) {
            ((ContainingCollection<Object>) pre).compare((ContainingCollection<Object>) post).forEach(d -> {
                if (d[0] == null) {
                    d[1].forEach(a -> added.accept((E) a));
                }
                if (d[1] == null) {
                    d[0].forEach(r -> removed.accept((E) r));
                }
            });
        } else {
            if (pre instanceof ContainingCollection) {
                for (E e : (ContainingCollection<E>) pre) {
                    removed.accept(e);
                }
            } else if (pre != null) {
                removed.accept((E) pre);
            }
            if (post instanceof ContainingCollection) {
                for (E e : (ContainingCollection<E>) post) {
                    added.accept(e);
                }
            } else if (post != null) {
                added.accept((E) post);
            }
        }
    }

}
