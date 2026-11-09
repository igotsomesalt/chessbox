package chessbox.game;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.UUID;

import chessbox.engine.EngineService;

public class EnginePoolPlayerProvider implements PlayerProvider, AutoCloseable {
    private EngineService engineService;
    private UUID poolId;

    public EnginePoolPlayerProvider(
        UUID poolId, EngineService engineService
    ) 
    throws NoSuchElementException, IOException {
        this.engineService = engineService;
        this.poolId = poolId;
    }

	@Override
	public Player acquire() throws InterruptedException, NoSuchElementException {
        return new EnginePlayer(engineService.acquire(poolId), poolId);
	}

	@Override
	public void release(Player player) throws IllegalArgumentException {
		try {
            engineService.release(poolId, ((EnginePlayer) player).getEngine());
        }
        catch (ClassCastException exception) {
            throw new IllegalArgumentException("Player is not instance of EnginePlayer");
        }
	}

    @Override 
    public void close() {
        engineService.releaseAll(poolId);
    }
}
