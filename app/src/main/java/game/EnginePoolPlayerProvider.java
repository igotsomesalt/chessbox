package game;

import engine.EngineConfig;
import engine.EnginePool;

public class EnginePoolPlayerProvider implements PlayerProvider {
    private EnginePool pool;

    public EnginePoolPlayerProvider(EngineConfig config, int poolSize) throws Exception {
        pool = new EnginePool(config, poolSize);
    }

	@Override
	public Player acquire() throws InterruptedException {
        return pool.acquire();
	}

	@Override
	public void release(Player player) throws IllegalArgumentException {
		try {
            pool.release((EnginePlayer) player);
        }
        catch (ClassCastException exception) {
            throw new IllegalArgumentException("Player is not instance of EnginePlayer");
        }
	}
}
