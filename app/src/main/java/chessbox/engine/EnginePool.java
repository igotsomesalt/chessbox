package chessbox.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EnginePool implements AutoCloseable {

    private final BlockingQueue<UciEngine> available;
    private final List<UciEngine> engines;
    private UUID id = UUID.randomUUID();

    public EnginePool(EngineConfig config, int size) throws IOException {

        if (size <= 0) {
            throw new IllegalArgumentException(
                "Pool size must be positive"
            );
        }

        available = new ArrayBlockingQueue<>(size);
        engines = new ArrayList<>(size);

        try {
            for (int i = 0; i < size; i++) {
                UciEngine engine = new UciEngine(config);

                try {
                    engine.initialize();
                } catch (IOException e) {
                    engine.close();
                    throw e;
                }

                engines.add(engine);
                available.add(engine);
            }
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    public UciEngine acquire() throws InterruptedException {
        return available.take();
    }

    public void release(UciEngine engine) {
        if (!engines.contains(engine)) {
            throw new IllegalArgumentException(
                "Engine does not belong to this pool"
            );
        }

        if (!available.offer(engine)) {
            throw new IllegalStateException(
                "Engine already released or pool is full"
            );
        }
    }

    public void releaseAll() {
        for (UciEngine engine : engines) {
            available.offer(engine);
        }
    }

    public UUID id() {
        return id;
    }

    @Override
    public void close() throws IOException {
        IOException failure = null;

        for (UciEngine engine : engines) {
            try {
                engine.close();
            } catch (IOException e) {
                if (failure == null) {
                    failure = e;
                } else {
                    failure.addSuppressed(e);
                }
            }
        }

        if (failure != null) {
            throw failure;
        }
    }
}