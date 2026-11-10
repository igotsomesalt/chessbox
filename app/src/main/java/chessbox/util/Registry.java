package chessbox.util;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Registry<ID, T extends Indentifiable<ID>> {

    private final Map<ID, T> items = new ConcurrentHashMap<>();

    public Registry(Collection<? extends T> items) {
        this();
        registerAll(items);
    }

    @SafeVarargs
    public Registry(T... items) {
        for (T item : items) {
            register(item);
        }
    }

    public void register(T item) throws IllegalArgumentException {
        Objects.requireNonNull(item, "Item cannot be null");

        if (items.putIfAbsent(item.id(), item) != null) {
            throw new IllegalArgumentException("ID already registered: " + item.id());
        }
    }

    public void registerAll(Collection<? extends T> items) throws IllegalArgumentException {
        for (T item : items) {
            register(item);
        }
    }

    public void registerAll(T... items) throws IllegalArgumentException {
        for (T item : items) {
            register(item);
        }
    }

    public T unregister(ID id) {
        T item = items.remove(id);

        if (item == null) {
            throw new NoSuchElementException("No item found with ID: " + id);
        }

        return item;
    }

    public T get(ID id) throws NoSuchElementException {
        T item = items.get(id);

        if (item == null) {
            throw new NoSuchElementException("No item found with ID: " + id);
        }

        return item;
    }

    public Collection<T> getAll() {
        return items.values();
    }

    public Set<ID> ids() {
        return items.keySet();
    }

    public void clear() {
        items.clear();
    }
}
