package org.modelingvalue.transactions;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiFunction;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.util.Context;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.StringUtil;

@SuppressWarnings("rawtypes")
public class ConstantState {

    private static final Context<Boolean>                            WEAK    = Context.of(false);

    private static final Object                                      NULL    = new Object() {
                                                                                 @Override
                                                                                 public String toString() {
                                                                                     return "null";
                                                                                 }
                                                                             };

    private static final AtomicReferenceFieldUpdater<Constants, Map> UPDATOR = AtomicReferenceFieldUpdater.newUpdater(Constants.class, Map.class, "constants");

    private static final class ConstantDepthOverflowException extends RuntimeException {
        private static final long            serialVersionUID = -6980064786088373917L;

        private List<Pair<Object, Constant>> list             = List.of();

        public ConstantDepthOverflowException(Object object, Constant lazy) {
            addLazy(object, lazy);
        }

        private void addLazy(Object object, Constant lazy) {
            list = list.append(Pair.of(object, lazy));
        }

        @Override
        public String getMessage() {
            return "Depth overflow while deriving constants " + list;
        }
    }

    private static interface Ref<O> {
        Constants<O> constants();
    }

    public class Constants<O> {

        private class WeakRef extends WeakReference<O> implements Ref<O> {
            private WeakRef(O referent, ReferenceQueue<? super O> queue) {
                super(referent, queue);
            }

            @Override
            public Constants<O> constants() {
                return Constants.this;
            }
        }

        private class SoftRef extends SoftReference<O> implements Ref<O> {
            private SoftRef(O referent, ReferenceQueue<? super O> queue) {
                super(referent, queue);
            }

            @Override
            public Constants<O> constants() {
                return Constants.this;
            }
        }

        public volatile Map<Constant<O, ?>, Object> constants;
        private final int                           hash;
        private final Reference<O>                  ref;

        public Constants(O object, boolean weak, ReferenceQueue<? super O> queue) {
            ref = weak ? new WeakRef(object, queue) : new SoftRef(object, queue);
            UPDATOR.lazySet(this, Map.of());
            hash = object.hashCode();
        }

        @SuppressWarnings("unchecked")
        public O object() {
            O o = ref.get();
            return o == null ? (O) this : o;
        }

        @SuppressWarnings("unchecked")
        public <V> V get(LeafTransaction leafTransaction, O object, Constant<O, V> constant) {
            Map<Constant<O, ?>, Object> prev = constants;
            V ist = (V) prev.get(constant);
            if (ist == null) {
                if (constant.deriver() == null) {
                    throw new Error("Constant " + constant + " is not set and not derived");
                } else {
                    V soll = derive(leafTransaction, object, constant);
                    ist = set(leafTransaction, object, constant, prev, soll == null ? (V) NULL : soll);
                }
            }
            return ist == NULL ? null : ist;
        }

        @SuppressWarnings("unchecked")
        public <V> V set(LeafTransaction leafTransaction, O object, Constant<O, V> constant, V soll) {
            Map<Constant<O, ?>, Object> prev = constants;
            V ist = (V) prev.get(constant);
            if (ist == null) {
                ist = set(leafTransaction, object, constant, prev, soll == null ? (V) NULL : soll);
            }
            if (!Objects.equals(ist == NULL ? null : ist, soll)) {
                throw new NonDeterministicException("Constant is not consistent " + StringUtil.toString(object) + "." + this + "=" + StringUtil.toString(ist) + "!=" + StringUtil.toString(soll));
            }
            return constant.getDefault();
        }

        @SuppressWarnings("unchecked")
        public <V, E> V set(LeafTransaction leafTransaction, O object, Constant<O, V> constant, BiFunction<V, E, V> function, E element) {
            Map<Constant<O, ?>, Object> prev = constants;
            V ist = (V) prev.get(constant);
            V soll = function.apply(ist, element);
            if (ist == null) {
                ist = set(leafTransaction, object, constant, prev, soll == null ? (V) NULL : soll);
            }
            if (!Objects.equals(ist == NULL ? null : ist, soll)) {
                throw new NonDeterministicException("Constant is not consistent " + StringUtil.toString(object) + "." + this + "=" + StringUtil.toString(ist) + "!=" + StringUtil.toString(soll));
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
            return "Constants:" + ref.get();
        }

        @SuppressWarnings("unchecked")
        private <V> V set(LeafTransaction tx, O object, Constant<O, V> constant, Map<Constant<O, ?>, Object> prev, V soll) {
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
            if (!Objects.equals(constant.getDefault(), soll == NULL ? null : soll)) {
                tx.changed(object, constant, constant.getDefault(), soll == NULL ? null : soll);
            }
            return soll;
        }

        @SuppressWarnings({"unchecked", "resource"})
        private <V> V derive(LeafTransaction leafTransaction, O object, Constant<O, V> constant) {
            List<Pair<Object, Constant>> list = List.of();
            while (true) {
                try {
                    if (!list.isEmpty()) {
                        boolean weak = WEAK.get();
                        WEAK.set(true);
                        try {
                            for (Pair<Object, Constant> lazy : list) {
                                if (constant.equals(lazy.b()) && object.equals(lazy.a())) {
                                    Pair<Object, Constant> me = Pair.of(object, constant);
                                    throw new NonDeterministicException("Circular constant definition: " + list.sublist(list.lastIndexOf(me), list.size()).add(me));
                                }
                                ConstantState.this.get(leafTransaction, lazy.a(), lazy.b());
                            }
                        } finally {
                            WEAK.set(weak);
                        }
                    }
                    return Constant.DERIVED.get(constant, () -> constant.deriver().apply(object));
                } catch (StackOverflowError soe) {
                    throw new ConstantDepthOverflowException(object, constant);
                } catch (ConstantDepthOverflowException lce) {
                    if (Constant.DERIVED.get() != null) {
                        lce.addLazy(object, constant);
                        throw lce;
                    } else {
                        list = list.prependList(lce.list);
                    }
                }
            }
        }
    }

    private final ReferenceQueue<Object>                           queue = new ReferenceQueue<>();
    private final AtomicReference<QualifiedSet<Object, Constants>> state = new AtomicReference<>(QualifiedSet.of(cs -> cs.object()));
    private final Thread                                           remover;

    public ConstantState() {
        remover = new Thread(() -> {
            try {
                while (true) {
                    removeConstants(((Ref<?>) queue.remove()).constants());
                }
            } catch (InterruptedException e) {
            }
        });
        remover.setDaemon(true);
        remover.start();
    }

    public void stop() {
        remover.interrupt();
    }

    public <O, V> V get(LeafTransaction leafTransaction, O object, Constant<O, V> constant) {
        return getConstants(leafTransaction, object).get(leafTransaction, object, constant);
    }

    public <O, V> V set(LeafTransaction leafTransaction, O object, Constant<O, V> constant, V value) {
        return getConstants(leafTransaction, object).set(leafTransaction, object, constant, value);
    }

    public <O, V, E> V set(LeafTransaction leafTransaction, O object, Constant<O, V> constant, BiFunction<V, E, V> deriver, E element) {
        return getConstants(leafTransaction, object).set(leafTransaction, object, constant, deriver, element);
    }

    @SuppressWarnings("unchecked")
    private <O> Constants<O> getConstants(LeafTransaction leafTransaction, O object) {
        QualifiedSet<Object, Constants> prev = state.get();
        Constants constants = prev.get(object);
        if (constants == null) {
            object = leafTransaction.state().canonical(object);
            constants = new Constants<O>(object, WEAK.get(), queue);
            QualifiedSet<Object, Constants> next = prev.add(constants);
            Constants<O> now;
            while (!state.compareAndSet(prev, next)) {
                prev = state.get();
                now = prev.get(object);
                if (now != null) {
                    constants.ref.clear();
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
        }
    }

}
