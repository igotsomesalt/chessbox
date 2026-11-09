package chessbox.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class Registry<ID, T extends Indentifiable<ID>> {

    Map<ID, T> items;

    public Registry(Collection<? extends T> items) {
        this();
        for (T item : items) {
            register(item);
        }
    }

    @SafeVarargs
    public Registry(T... items) {
        this();
        for (T item : items) {
            register(item);
        }
    }

    public Registry() {
        items = new HashMap<>();
    }

    public void register(T item) throws IllegalArgumentException {
        Objects.requireNonNull(item, "Item cannot be null");

        if (items.putIfAbsent(item.id(), item) != null) {
            throw new IllegalArgumentException("ID already registered: " + item.id());
        }
    }

    public T unregister(ID id) {
        T item = items.remove(id);

        if (item == null) {
            throw new NoSuchElementException("No item found with ID: " + id);
        }

        return item;
    }

    public T get(String id) throws NoSuchElementException {
        T item = items.get(id);

        if (item == null) {
            throw new NoSuchElementException("No item found with ID: " + id);
        }

        return item;
    }

    public Collection<T> getAll() {
        return items.values();
    }
}
