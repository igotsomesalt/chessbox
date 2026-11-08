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
        return new EnginePlayer(pool.acquire(), pool.id());
	}

	@Override
	public void release(Player player) throws IllegalArgumentException {
		try {
            pool.release(((EnginePlayer) player).getEngine());
        }
        catch (ClassCastException exception) {
            throw new IllegalArgumentException("Player is not instance of EnginePlayer");
        }
	}
}
