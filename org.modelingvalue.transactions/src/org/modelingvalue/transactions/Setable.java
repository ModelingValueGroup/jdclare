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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.modelingvalue.collections.ContainingCollection;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Context;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.QuadConsumer;

public class Setable<O, T> extends Getable<O, T> {

    private static final Context<Pair<Mutable, Setable<Mutable, ?>>> POST = Context.of();

    public static <C, V> Setable<C, V> of(Object id, V def) {
        return new Setable<C, V>(id, def, false, null, null);
    }

    public static <C, V> Setable<C, V> of(Object id, V def, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Setable<C, V>(id, def, false, null, changed);
    }

    public static <C, V> Setable<C, V> of(Object id, V def, boolean containment) {
        return new Setable<C, V>(id, def, containment, null, null);
    }

    public static <C, V> Setable<C, V> of(Object id, V def, Setable<?, ?> opposite) {
        return new Setable<C, V>(id, def, false, opposite, null);
    }

    public static <C, V> Setable<C, V> of(Object id, V def, boolean containment, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Setable<C, V>(id, def, containment, null, changed);
    }

    public static <C, V> Setable<C, V> of(Object id, V def, Setable<?, ?> opposite, QuadConsumer<LeafTransaction, C, V, V> changed) {
        return new Setable<C, V>(id, def, false, opposite, changed);
    }

    protected QuadConsumer<LeafTransaction, O, T, T> changed;
    protected final boolean                          containment;
    private Setable<Object, ?>                       opposite;

    @SuppressWarnings("unchecked")
    protected Setable(Object id, T def, boolean containment, Setable<?, ?> opposite, QuadConsumer<LeafTransaction, O, T, T> changed) {
        super(id, def);
        this.containment = containment;
        this.changed = changed;
        if (this.opposite != null && opposite != null && !this.opposite.equals(opposite)) {
            throw new Error("Opposite inconsistency " + this.opposite + " != " + opposite);
        }
        this.opposite = (Setable<Object, ?>) opposite;
        if (opposite != null) {
            opposite.opposite = (Setable<Object, ?>) this;
        }
    }

    public boolean containment() {
        return containment;
    }

    @SuppressWarnings("unchecked")
    protected void changed(LeafTransaction tx, O object, T preValue, T postValue) {
        if (changed != null) {
            changed.accept(tx, object, preValue, postValue);
        }
        if (containment) {
            Pair<Mutable, Setable<Mutable, ?>> now = Pair.of((Mutable) object, (Setable<Mutable, ?>) this);
            Pair<Mutable, Setable<Mutable, ?>> post = POST.get();
            Setable.<T, Mutable> diff(preValue, postValue, added -> {
                Pair<Mutable, Setable<Mutable, ?>> pre = Mutable.D_PARENT_CONTAINING.get(added);
                if (pre != null) {
                    POST.run(now, () -> pre.b().remove(pre.a(), added));
                }
                Mutable.D_PARENT_CONTAINING.set(added, now);
                if (pre == null || !pre.a().equals(now.a())) {
                    Mutable.D_CHILDREN.set(now.a(), Set::add, added);
                }
            }, removed -> {
                if (post == null || !post.a().equals(now.a())) {
                    Mutable.D_CHILDREN.set(now.a(), Set::remove, removed);
                }
                if (post == null) {
                    Mutable.D_PARENT_CONTAINING.set(removed, null);
                }
            });
        } else if (opposite != null) {
            Setable.<T, Object> diff(preValue, postValue, added -> {
                opposite.add(added, object);
            }, removed -> {
                opposite.remove(removed, object);
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
        } else if (pre instanceof java.util.Collection && post instanceof java.util.Collection) {
            ((java.util.Collection<Object>) post).stream().forEach(a -> {
                if (!((java.util.Collection<Object>) pre).contains(a)) {
                    added.accept((E) a);
                }
            });
            ((java.util.Collection<Object>) pre).stream().forEach(r -> {
                if (!((java.util.Collection<Object>) post).contains(r)) {
                    removed.accept((E) r);
                }
            });
        } else {
            if (pre != null) {
                if (pre instanceof Iterable) {
                    for (E e : (Iterable<E>) pre) {
                        removed.accept(e);
                    }
                } else {
                    removed.accept((E) pre);
                }
            }
            if (post != null) {
                if (post instanceof Iterable) {
                    for (E e : (Iterable<E>) post) {
                        added.accept(e);
                    }
                } else {
                    added.accept((E) post);
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "unlikely-arg-type"})
    public <E> void add(O obj, E e) {
        set(obj, (v, r) -> {
            if (v instanceof ContainingCollection) {
                return (T) ((ContainingCollection<E>) v).add(r);
            } else if (v instanceof java.util.List) {
                if (!((java.util.List<E>) v).contains(r)) {
                    java.util.List<E> l = new ArrayList<E>((java.util.List<E>) v);
                    l.add(r);
                    return (T) Collections.unmodifiableList(l);
                }
            } else if (v instanceof java.util.Set) {
                if (!((java.util.Set<E>) v).contains(r)) {
                    java.util.Set<E> s = new HashSet<E>((java.util.Set<E>) v);
                    s.add(r);
                    return (T) Collections.unmodifiableSet(s);
                }
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
            } else if (v instanceof java.util.List) {
                if (((java.util.List<E>) v).contains(r)) {
                    java.util.List<E> l = new ArrayList<E>((java.util.List<E>) v);
                    l.remove(r);
                    return (T) Collections.unmodifiableList(l);
                }
            } else if (v instanceof java.util.Set) {
                if (((java.util.Set<E>) v).contains(r)) {
                    java.util.Set<E> s = new HashSet<E>((java.util.Set<E>) v);
                    s.remove(r);
                    return (T) Collections.unmodifiableSet(s);
                }
            } else if (r.equals(v)) {
                return null;
            }
            return v;
        }, e);
    }

}
