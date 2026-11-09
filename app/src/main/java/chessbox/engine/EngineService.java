package chessbox.engine;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import chessbox.App;

public class EngineService implements AutoCloseable {

    private EngineRegistry registry;
    private final Map<UUID, EnginePool> pools = new HashMap<>();

    public EngineService(EngineRegistry registry) {
        this.registry = registry;
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
        UUID id = UUID.randomUUID();
        String shortId = chessbox.util.UUID.shortId(id);
        pools.put(id, pool);

        sendMessage(String.format("""
            Pool created:
                id = %s
                size = %d 
            """, shortId, size
        ));

        return id;
    }

    public void destroyPool(UUID poolId) throws NoSuchElementException, IOException {
        EnginePool pool = pools.remove(poolId);
        if (pool == null) {
            throw new NoSuchElementException("Pool does not exist: " + poolId);
        }

        pool.close();
        sendMessage("Pool destroyed: " + poolId);
    }

    public void destroyPool(String poolId) throws NoSuchElementException, IllegalArgumentException, IOException {
        UUID uuid = chessbox.util.UUID.findKey(poolId, pools.keySet());
        EnginePool pool = pools.remove(uuid);

        
        pool.close();
        sendMessage("Pool destroyed: " + poolId);
    }

    public UciEngine acquire(UUID poolId) throws NoSuchElementException, InterruptedException {
        EnginePool pool = pools.get(poolId);
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

    private void sendMessage(String message) {
        App.ctx.out().print(message);
    }
}
