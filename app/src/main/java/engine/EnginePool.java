package engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import game.EnginePlayer;

public class EnginePool implements AutoCloseable {

    private final BlockingQueue<EnginePlayer> available;
    private final List<EnginePlayer> engines;

    public EnginePool(EngineConfig config, int size) throws IOException {

        available = new ArrayBlockingQueue<>(size);
        engines = new ArrayList<>(size);
        UUID id = UUID.randomUUID();

        for (int i = 0; i < size; i++) {
            EnginePlayer engine = new EnginePlayer(config, id);

            engines.add(engine);
            available.add(engine);
        }
    }

    public EnginePlayer acquire() throws InterruptedException {
        return available.take();
    }

    public void release(EnginePlayer engine) {
        available.add(engine);
    }

    @Override
    public void close() throws Exception {
        for (EnginePlayer engine : engines) {
            engine.close();
        }
    }
}
