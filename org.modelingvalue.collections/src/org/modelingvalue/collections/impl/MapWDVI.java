package org.modelingvalue.collections.impl;

import java.util.Objects;
import java.util.function.BinaryOperator;

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.util.SerializableFunction;
import org.modelingvalue.collections.util.TriFunction;

public class MapWDVI<K, V> extends MapImpl<K, V> {

    private static final long                serialVersionUID = 2424304733060404412L;

    private final SerializableFunction<K, V> defaultFunction;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public MapWDVI(Entry[] es, SerializableFunction<K, V> defaultFunction) {
        super(es);
        this.defaultFunction = defaultFunction;
    }

    protected MapWDVI(Object value, SerializableFunction<K, V> defaultFunction) {
        super(value);
        this.defaultFunction = defaultFunction;
    }

    @Override
    protected MapWDVI<K, V> create(Object val) {
        return val != value ? new MapWDVI<>(val, defaultFunction) : this;
    }

    @Override
    public Map<K, V> put(Entry<K, V> entry) {
        return Objects.equals(entry.getValue(), defaultFunction.apply(entry.getKey())) ? super.removeKey(entry.getKey()) : super.put(entry);
    }

    @Override
    public Map<K, V> add(Entry<K, V> entry) {
        return Objects.equals(entry.getValue(), defaultFunction.apply(entry.getKey())) ? super.removeKey(entry.getKey()) : super.add(entry);
    }

    @Override
    protected Object mergeEntry(Map<K, V> map1, Map<K, V> map2, BinaryOperator<V> merger) {
        return ((MapImpl<K, V>) map1.map(e1 -> {
            Entry<K, V> e2 = map2.getEntry(e1.getKey());
            V val = merger.apply(e1.getValue(), e2.getValue());
            return Objects.equals(val, defaultFunction.apply(e1.getKey())) ? null : //
            Objects.equals(val, e1.getValue()) ? e1 : Objects.equals(val, e2.getValue()) ? e2 : Entry.of(e1.getKey(), val);
        }).notNull().toMap(e -> e)).value;
    }

    @Override
    public V get(K key) {
        Entry<K, V> result = getEntry(key);
        return result != null ? result.getValue() : defaultFunction.apply(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Entry<K, V> mergeEntry(Entry<K, V> e, TriFunction<K, Entry<K, V>, Entry<K, V>[], Entry<K, V>> merger, Object[] es) {
        Entry<K, V>[] vs = new Entry[es.length];
        System.arraycopy(es, 0, vs, 0, es.length);
        K key = e != null ? e.getKey() : null;
        for (int i = 0; key == null && i < es.length; i++) {
            if (vs[i] != null) {
                key = vs[i].getKey();
            }
        }
        Entry<K, V> result = merger.apply(key, e, vs);
        if (result == null || Objects.equals(result.getValue(), defaultFunction.apply(key))) {
            return null;
        } else if (Objects.equals(result, e)) {
            return e;
        } else {
            for (int i = 0; i < es.length; i++) {
                if (Objects.equals(result, es[i])) {
                    return (Entry<K, V>) es[i];
                }
            }
        }
        return result;
    }

}
