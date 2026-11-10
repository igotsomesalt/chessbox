package chessbox.engine;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EngineService implements AutoCloseable {

    private final EngineRegistry engines;
    private final EnginePoolRegistry pools;
    private final List<Consumer<EngineEvent>> listeners = new CopyOnWriteArrayList<>();

    public EngineService(EngineRegistry engines, EnginePoolRegistry pools) {
        this.engines = engines;
        this.pools = pools;
    }

    public void addListener(Consumer<EngineEvent> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<EngineEvent> listener) {
        listeners.remove(listener);
    }

    public void register(EngineConfig config) throws IllegalArgumentException {
        engines.register(config);
    }

    public void unregister(String engineId) throws NoSuchElementException {
        engines.unregister(engineId);
    }

    public EngineConfig getConfig(String engineId) throws NoSuchElementException {
        return engines.get(engineId);
    }

    public Collection<EngineConfig> listEngineConfigs() {
        return engines.getAll();
    }

    public Collection<EnginePool> listEnginePools() {
        return pools.getAll();
    }

    public UUID createPool(String engineId, int size) throws NoSuchElementException, IOException {
        EngineConfig config = engines.get(engineId);

        EnginePool pool = new EnginePool(config, size);
        pools.register(pool);

        emit(new EngineEvent.PoolCreated(pool.id(), config.name(), size));
        return pool.id();
    }

    public void destroyPool(UUID poolId) throws NoSuchElementException, IOException {
        EnginePool pool = pools.unregister(poolId);

        pool.close();
        emit(new EngineEvent.PoolDestroyed(poolId));
    }

    public void destroyPool(String poolId) throws NoSuchElementException, IllegalArgumentException, IOException {
        UUID uuid = chessbox.util.UUID.findKey(poolId, pools.ids());
        EnginePool pool = pools.unregister(uuid);

        pool.close();
        emit(new EngineEvent.PoolDestroyed(uuid));
    }

    public UciEngine acquire(UUID poolId) throws NoSuchElementException, InterruptedException {
        EnginePool pool = pools.get(poolId);
        return pool.acquire();
    }

    public UciEngine acquire(String poolId) throws NoSuchElementException, InterruptedException {
        EnginePool pool = pools.get(chessbox.util.UUID.findKey(poolId, pools.ids()));
        return pool.acquire();
    }

    public void release(UUID poolId, UciEngine engine) throws NoSuchElementException, IllegalArgumentException, IllegalStateException {
        EnginePool pool = pools.get(poolId);
        pool.release(engine);
    }

    public void release(String poolId, UciEngine engine) throws NoSuchElementException, IllegalArgumentException, IllegalStateException { 
        EnginePool pool = pools.get(chessbox.util.UUID.findKey(poolId, pools.ids()));
        pool.release(engine);
    }

    public void releaseAll(UUID poolId) throws NoSuchElementException, IllegalArgumentException {
        EnginePool pool = pools.get(poolId);
        pool.releaseAll();
    }

    public void releaseAll(String poolId) throws NoSuchElementException, IllegalArgumentException {
        EnginePool pool = pools.get(chessbox.util.UUID.findKey(poolId, pools.ids()));
        pool.releaseAll();
    }

    @Override 
    public void close() throws IOException {
        try {
            for (EnginePool pool : pools.getAll()) {
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
