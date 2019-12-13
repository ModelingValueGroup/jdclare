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

package org.modelingvalue.dclare;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.modelingvalue.collections.ContainingCollection;
import org.modelingvalue.collections.DefaultMap;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Context;
import org.modelingvalue.collections.util.Internable;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.QuadConsumer;

public class Setable<O, T> extends Getable<O, T> {

    private static final Context<Boolean> MOVING = Context.of(false);

    public static <C, V> Setable<C, V> of(Object id, V def) {
        return new Setable<C, V>(id, def, false, null, null, null, true);
    }

    public static <C, V> Setable<C, V> of(Object id, V def, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Setable<C, V>(id, def, false, null, null, changed, true);
    }

    public static <C, V> Setable<C, V> of(Object id, V def, Supplier<Setable<?, ?>> opposite) {
        return new Setable<C, V>(id, def, false, opposite, null, null, true);
    }

    public static <C, V> Setable<C, V> of(Object id, V def, boolean containment) {
        return new Setable<C, V>(id, def, containment, null, null, null, true);
    }

    protected QuadConsumer<LeafTransaction, O, T, T>  changed;
    protected final boolean                           containment;
    private final Supplier<Setable<?, ?>>             opposite;
    private final Supplier<Setable<O, Set<?>>>        scope;
    @SuppressWarnings("rawtypes")
    private final Constant<T, Entry<Setable, Object>> internal;
    protected final boolean                           checkConsistency;
    private boolean                                   isReference;

    protected Setable(Object id, T def, boolean containment, Supplier<Setable<?, ?>> opposite, Supplier<Setable<O, Set<?>>> scope, QuadConsumer<LeafTransaction, O, T, T> changed, boolean checkConsistency) {
        super(id, def);
        this.checkConsistency = checkConsistency;
        this.containment = containment;
        this.changed = changed;
        this.opposite = opposite;
        this.scope = scope;
        if (containment && opposite != null) {
            throw new Error("The containment setable " + this + " has an opposite");
        }
        this.internal = this instanceof Constant ? null : Constant.of(Pair.of(this, "internalEntry"), v -> Entry.of(this, v));
    }

    @SuppressWarnings("rawtypes")
    protected Entry<Setable, Object> entry(T value, DefaultMap<Setable, Object> properties) {
        if (Internable.isInternable(value)) {
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
    protected void deduplicate(Entry e1, DefaultMap<?, ?> map2) {
        Object v1 = e1.getValue();
        if (v1 instanceof DefaultMap) {
            for (Entry e3 : (DefaultMap<?, ?>) v1) {
                deduplicate(e3, map2);
            }
        } else {
            for (Entry e2 : map2) {
                Object v2 = e2.getValue();
                if (v2 instanceof DefaultMap) {
                    deduplicate(e1, (DefaultMap) v2);
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
    public Setable<?, ?> opposite() {
        return opposite != null ? opposite.get() : null;
    }

    @Override
    public Setable<O, Set<?>> scope() {
        return scope != null ? scope.get() : null;
    }

    @SuppressWarnings("unchecked")
    protected void changed(LeafTransaction tx, O object, T preValue, T postValue) {
        if (changed != null) {
            changed.accept(tx, object, preValue, postValue);
        }
        if (containment) {
            Setable.<T, Mutable> diff(preValue, postValue, added -> {
                Pair<Mutable, Setable<Mutable, ?>> prePair = Mutable.D_PARENT_CONTAINING.get(added);
                if (prePair != null) {
                    MOVING.run(true, () -> prePair.b().remove(prePair.a(), added));
                }
                Mutable.D_PARENT_CONTAINING.set(added, Pair.of((Mutable) object, (Setable<Mutable, ?>) this));
                if (prePair == null) {
                    added.dActivate();
                }
            }, removed -> {
                if (!MOVING.get()) {
                    removed.dDeactivate();
                    Mutable.D_PARENT_CONTAINING.set(removed, null);
                }
            });
        } else if (opposite != null) {
            Setable<Object, ?> opp = (Setable<Object, ?>) opposite.get();
            Setable.<T, Object> diff(preValue, postValue, added -> {
                opp.add(added, object);
            }, removed -> {
                opp.remove(removed, object);
            });
        } else if (this != Mutable.D_PARENT_CONTAINING && !isReference) {
            Object v = postValue;
            if (v instanceof ContainingCollection) {
                v = ((ContainingCollection<?>) v).isEmpty() ? null : ((ContainingCollection<?>) v).get(0);
            }
            if (v instanceof Mutable) {
                isReference = true;
            }
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
        set(obj, (v, a) -> {
            if (v instanceof ContainingCollection) {
                return (T) ((ContainingCollection<E>) v).addUnique(a);
            } else if (!a.equals(v)) {
                return (T) a;
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

    @SuppressWarnings("unchecked")
    public static <T, E> void diff(T pre, T post, Consumer<E> added, Consumer<E> removed) {
        if (pre instanceof ContainingCollection && post instanceof ContainingCollection) {
            ((ContainingCollection<E>) pre).compare((ContainingCollection<E>) post).forEachOrdered(d -> {
                if (d[0] == null) {
                    for (E a : d[1]) {
                        added.accept(a);
                    }
                }
                if (d[1] == null) {
                    for (E e : d[0]) {
                        removed.accept(e);
                    }
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

    public boolean checkConsistency() {
        return checkConsistency && (scope != null || isReference);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void checkConsistency(State state, O object, T post) {
        if (isReference) {
            for (Mutable m : mutables(post)) {
                if (isOrphan(state, m)) {
                    throw new ReferencedOrphanException(object, this, m);
                }
            }
        }
        if (scope != null) {
            Set s = state.get(object, scope.get());
            if (post instanceof ContainingCollection) {
                if (!s.containsAll((ContainingCollection) post)) {
                    throw new OutOfScopeException(object, this, post, s);
                }
            } else if (post != null) {
                if (!s.contains(post)) {
                    throw new OutOfScopeException(object, this, post, s);
                }
            }
        }
    }

    protected boolean isOrphan(State state, Mutable m) {
        return !(m instanceof Universe) && state.get(m, Mutable.D_PARENT_CONTAINING) == null;
    }

}
