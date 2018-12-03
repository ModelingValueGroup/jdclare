package org.modelingvalue.transactions;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiFunction;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.StringUtil;

@SuppressWarnings("rawtypes")
public class ConstantState {

    private static final Object                                      NULL    = new Object() {
                                                                                 @Override
                                                                                 public String toString() {
                                                                                     return "null";
                                                                                 }
                                                                             };

    private static final AtomicReferenceFieldUpdater<Constants, Map> UPDATOR = AtomicReferenceFieldUpdater.newUpdater(Constants.class, Map.class, "constants");

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

    private class Constants<O> extends WeakReference<O> {

        protected volatile Map<Constant<O, ?>, Object> constants;
        private final int                              hash;

        public Constants(O object, ReferenceQueue<? super O> queue) {
            super(object, queue);
            UPDATOR.lazySet(this, Map.of());
            hash = object.hashCode();
        }

        @SuppressWarnings("unchecked")
        public O object() {
            O o = get();
            return o == null ? (O) this : o;
        }

        @SuppressWarnings("unchecked")
        public <V> V get(AbstractLeaf leaf, Constant<O, V> constant) {
            Map<Constant<O, ?>, Object> prev = constants;
            V ist = (V) prev.get(constant);
            if (ist == null) {
                if (constant.deriver() == null) {
                    throw new Error("Constant " + constant + " is not set and not derived");
                } else {
                    V soll = derive(leaf, object(), constant);
                    ist = set(leaf, constant, prev, soll == null ? (V) NULL : soll);
                }
            }
            return ist == NULL ? null : ist;
        }

        @SuppressWarnings("unchecked")
        public <V> V set(AbstractLeaf leaf, Constant<O, V> constant, V soll) {
            Map<Constant<O, ?>, Object> prev = constants;
            V ist = (V) prev.get(constant);
            if (ist == null) {
                ist = set(leaf, constant, prev, soll == null ? (V) NULL : soll);
            }
            if (!Objects.equals(ist == NULL ? null : ist, soll)) {
                throw new NonDeterministicException("Constant is not consistent " + StringUtil.toString(object()) + "." + this + "=" + StringUtil.toString(ist) + "!=" + StringUtil.toString(soll));
            }
            return constant.getDefault();
        }

        @SuppressWarnings("unchecked")
        public <V, E> V set(AbstractLeaf leaf, Constant<O, V> constant, BiFunction<V, E, V> function, E element) {
            Map<Constant<O, ?>, Object> prev = constants;
            V ist = (V) prev.get(constant);
            V soll = function.apply(constant.getDefault(), element);
            if (ist == null) {
                ist = set(leaf, constant, prev, soll == null ? (V) NULL : soll);
            }
            if (!Objects.equals(ist == NULL ? null : ist, soll)) {
                throw new NonDeterministicException("Constant is not consistent " + StringUtil.toString(object()) + "." + this + "=" + StringUtil.toString(ist) + "!=" + StringUtil.toString(soll));
            }
            return ist == NULL ? null : ist;
        }

        @Override
        public boolean equals(Object o) {
            return o == this;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return "Constants:" + get();
        }

        @SuppressWarnings("unchecked")
        private <V> V set(AbstractLeaf leaf, Constant<O, V> constant, Map<Constant<O, ?>, Object> prev, V soll) {
            V ist;
            Map<Constant<O, ?>, Object> next = prev.put(constant, soll);
            while (!UPDATOR.compareAndSet(this, prev, next)) {
                prev = constants;
                ist = (V) prev.get(constant);
                if (ist != null) {
                    return ist;
                }
                next = prev.put(constant, soll);
            }
            constant.changed(leaf, object(), constant.getDefault(), soll == NULL ? null : soll);
            return soll;
        }

        @SuppressWarnings("unchecked")
        private <V> V derive(AbstractLeaf leaf, O object, Constant<O, V> constant) throws NonDeterministicException {
            try {
                return Constant.CURRENT.get(constant, () -> constant.deriver().apply(object));
            } catch (ConstantSetableException lce) {
                if (!(lce.getCause() instanceof StackOverflowError) || Constant.CURRENT.get() != null) {
                    lce.addLazy(object, constant);
                    throw lce;
                } else {
                    for (Pair<Object, Constant> lazy : lce.list) {
                        if (constant.equals(lazy.b()) && object.equals(lazy.a())) {
                            Pair<Object, Constant> me = Pair.of(object, constant);
                            throw new NonDeterministicException("Circular constant definition: " + lce.list.sublist(lce.list.lastIndexOf(me), lce.list.size()).add(me));
                        }
                        ConstantState.this.get(leaf, lazy.a(), lazy.b());
                    }
                    return Constant.CURRENT.get(constant, () -> constant.deriver().apply(object));
                }
            } catch (Throwable t) {
                throw new ConstantSetableException(object, constant, t);
            }
        }
    }

    private final ReferenceQueue<Object>                           queue = new ReferenceQueue<>();
    private final AtomicReference<QualifiedSet<Object, Constants>> state = new AtomicReference<>(QualifiedSet.of(cs -> cs.object()));

    public ConstantState() {
        Thread remover = new Thread(() -> {
            try {
                while (true) {
                    removeConstants((Constants) queue.remove());
                }
            } catch (InterruptedException e) {
                throw new Error(e);
            }
        });
        remover.setDaemon(true);
        remover.start();
    }

    public <O, V> V get(AbstractLeaf leaf, O object, Constant<O, V> constant) {
        return getConstants(object).get(leaf, constant);
    }

    public <O, V> V set(AbstractLeaf leaf, O object, Constant<O, V> constant, V value) {
        return getConstants(object).set(leaf, constant, value);
    }

    public <O, V, E> V set(AbstractLeaf leaf, O object, Constant<O, V> constant, BiFunction<V, E, V> deriver, E element) {
        return getConstants(object).set(leaf, constant, deriver, element);
    }

    @SuppressWarnings("unchecked")
    private <O> Constants<O> getConstants(O object) {
        QualifiedSet<Object, Constants> prev = state.get();
        Constants constants = prev.get(object);
        if (constants == null) {
            constants = new Constants<O>(object, queue);
            QualifiedSet<Object, Constants> next = prev.add(constants);
            Constants<O> now;
            while (!state.compareAndSet(prev, next)) {
                prev = state.get();
                now = prev.get(object);
                if (now != null) {
                    constants.clear();
                    return now;
                }
                next = prev.add(constants);
            }
        }
        return constants;
    }

    private void removeConstants(Constants constants) {
        QualifiedSet<Object, Constants> prev = state.get();
        Object object = constants.object();
        constants = prev.get(object);
        if (constants != null) {
            QualifiedSet<Object, Constants> next = prev.removeKey(object);
            while (!state.compareAndSet(prev, next)) {
                prev = state.get();
                if (prev.get(object) == null) {
                    return;
                }
                next = prev.removeKey(object);
            }
            constants.clear();
        }
    }

}
