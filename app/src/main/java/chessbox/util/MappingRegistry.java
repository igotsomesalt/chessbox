package chessbox.util;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MappingRegistry<K, V> {
    
    private final Map<K, V> values = new ConcurrentHashMap<>();

    public void register(K key, V value) throws IllegalArgumentException {

        if (values.putIfAbsent(key, value) != null) {
            throw new IllegalArgumentException("Item already registered: " + key);
        }
    }

    public V unregister(K key) throws NoSuchElementException {
        V value = values.remove(key);

        if (value == null) {
            throw new NoSuchElementException(
                "No mapping found for: " + key
            );
        }

        return value;
    }

    public V get(K key) {
        return values.get(key);
    }

    public Collection<V> getAll() {
        return values.values();
    }

    public Set<K> keys() {
        return values.keySet();
    }

    public void clear() {
        values.clear();
    }
}
