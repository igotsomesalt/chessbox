package chessbox.engine;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EngineService implements AutoCloseable {

    private EngineRegistry registry;
    private final Map<UUID, EnginePool> pools = new HashMap<>();
    private final List<Consumer<EngineEvent>> listeners = new CopyOnWriteArrayList<>();

    public EngineService(EngineRegistry registry) {
        this.registry = registry;
    }

    public void addListener(Consumer<EngineEvent> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<EngineEvent> listener) {
        listeners.remove(listener);
    }

    public void register(EngineConfig config) throws IllegalArgumentException {
        registry.register(config);
    }

    public void unregister(String engineId) throws NoSuchElementException {
        registry.unregister(engineId);
    }

    public EngineConfig getConfig(String engineId) throws NoSuchElementException {
        return registry.getConfig(engineId);
    }

    public Collection<EngineConfig> listEngines() {
        return registry.getConfigs();
    }

    public UUID createPool(String engineId, int size) throws NoSuchElementException, IOException {
        EngineConfig config = registry.getConfig(engineId);

        EnginePool pool = new EnginePool(config, size);
        UUID uuid = UUID.randomUUID();
        pools.put(uuid, pool);

        emit(new EngineEvent.PoolCreated(uuid, config.name(), size));
        return uuid;
    }

    public void destroyPool(UUID poolId) throws NoSuchElementException, IOException {
        EnginePool pool = pools.remove(poolId);
        if (pool == null) {
            throw new NoSuchElementException("Pool does not exist: " + poolId);
        }

        pool.close();
        emit(new EngineEvent.PoolDestroyed(poolId));
    }

    public void destroyPool(String poolId) throws NoSuchElementException, IllegalArgumentException, IOException {
        UUID uuid = chessbox.util.UUID.findKey(poolId, pools.keySet());
        EnginePool pool = pools.remove(uuid);

        
        pool.close();
        emit(new EngineEvent.PoolDestroyed(uuid));
    }

    public UciEngine acquire(UUID poolId) throws NoSuchElementException, InterruptedException {
        EnginePool pool = pools.get(poolId);
        if (pool == null) {
            throw new NoSuchElementException("Pool does not exist: " + poolId);
        }

        return pool.acquire();
    }

    public UciEngine acquire(String poolId) throws NoSuchElementException, InterruptedException {
        EnginePool pool = pools.get(chessbox.util.UUID.findKey(poolId, pools.keySet()));
        return pool.acquire();
    }

    public void release(UUID poolId, UciEngine engine) throws NoSuchElementException, IllegalArgumentException, IllegalStateException {
        EnginePool pool = pools.get(poolId);
        if (pool == null) {
            throw new NoSuchElementException("Pool does not exist: " + poolId);
        }

        pool.release(engine);
    }

    public void release(String poolId, UciEngine engine) throws NoSuchElementException, IllegalArgumentException, IllegalStateException { 
        EnginePool pool = pools.get(chessbox.util.UUID.findKey(poolId, pools.keySet()));
        pool.release(engine);
    }

    public void releaseAll(UUID poolId) throws NoSuchElementException, IllegalArgumentException {
        EnginePool pool = pools.get(poolId);
        if (pool == null) {
            throw new NoSuchElementException("Pool does not exist: " + poolId);
        }

        pool.releaseAll();
    }

    public void releaseAll(String poolId) throws NoSuchElementException, IllegalArgumentException {
        EnginePool pool = pools.get(chessbox.util.UUID.findKey(poolId, pools.keySet()));
        pool.releaseAll();
    }

    @Override 
    public void close() throws IOException {
        try {
            for (EnginePool pool : pools.values()) {
                pool.close();
            }
        } finally {
            pools.clear();
        }
    }

    private void emit(EngineEvent event) {
        for (Consumer<EngineEvent> listener : listeners) {
            listener.accept(event);
        }
    }
}
