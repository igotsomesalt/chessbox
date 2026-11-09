package chessbox.game;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.UUID;

import chessbox.engine.EngineService;

public class EnginePoolPlayerProvider implements PlayerProvider, AutoCloseable {
    private EngineService engineService;
    private String engineId;
    private UUID playerId;

    public EnginePoolPlayerProvider(
        String engineId, EngineService engineService
    ) 
    throws NoSuchElementException, IOException {

        this.engineService = engineService;
        this.engineId = engineId;
        this.playerId = UUID.randomUUID();
    }

	@Override
	public Player acquire() throws InterruptedException {
        return new EnginePlayer(engineService.acquire(engineId), playerId);
	}

	@Override
	public void release(Player player) throws IllegalArgumentException {
		try {
            engineService.release(engineId, ((EnginePlayer) player).getEngine());
        }
        catch (ClassCastException exception) {
            throw new IllegalArgumentException("Player is not instance of EnginePlayer");
        }
	}

    @Override 
    public void close() {
        engineService.releaseAll(engineId);
    }
}
