package chessbox.engine;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import chessbox.App;

public class EngineService implements AutoCloseable {

    private EngineRegistry registry;
    private final Map<String, EnginePool> pools = new HashMap<>();

    public EngineService(EngineRegistry registry) {
        this.registry = registry;
    }

    public void register(EngineConfig config) throws IllegalArgumentException {
        registry.register(config);
    }

    public void unregister(String id) throws NoSuchElementException {
        registry.unregister(id);
    }

    public EngineConfig getConfig(String id) throws NoSuchElementException {
        return registry.getConfig(id);
    }

    public Collection<EngineConfig> listEngines() {
        return registry.getConfigs();
    }

    public void createPool(String engineId, int size) throws NoSuchElementException, IOException {
        EngineConfig config = registry.getConfig(engineId);

        if (pools.containsKey(engineId)) {
            throw new IllegalArgumentException("Pool already exists: " + engineId);
        }

        EnginePool pool = new EnginePool(config, size);
        pools.put(engineId, pool);
        sendMessage(String.format("Pool created with size %d for engine ID: %s", size, engineId));
    }

    public void destroyPool(String engineId) throws NoSuchElementException, IOException {
        EnginePool pool = pools.remove(engineId);
        if (pool == null) {
            throw new NoSuchElementException("Pool does not exist: " + engineId);
        }

        pool.close();
        sendMessage("Pool destroyed for engine ID: " + engineId);
    }

    public UciEngine acquire(String engineId) throws NoSuchElementException, InterruptedException {
        EnginePool pool = pools.get(engineId);
        if (pool == null) {
            throw new NoSuchElementException("Pool does not exist: " + engineId);
        }

        return pool.acquire();
    }

    public void release(String engineId, UciEngine engine) {
        EnginePool pool = pools.get(engineId);
        if (pool == null) {
            throw new NoSuchElementException("Pool does not exist: " + engineId);
        }

        pool.release(engine);
    }

    public void releaseAll(String engineId) {
        EnginePool pool = pools.get(engineId);
        if (pool == null) {
            throw new NoSuchElementException("Pool does not exist: " + engineId);
        }

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

    private void sendMessage(String message) {
        App.ctx.out().print(message);
    }
}
