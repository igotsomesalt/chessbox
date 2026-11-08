package game;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.UUID;

import engine.EngineService;

public class EnginePoolPlayerProvider implements PlayerProvider {
    private EngineService engineService;
    private String engineId;
    private UUID playerId;

    public EnginePoolPlayerProvider(
        String engineId, int poolSize, EngineService engineService
    ) 
    throws NoSuchElementException, IOException {

        this.engineService = engineService;
        this.engineId = engineId;
        this.playerId = UUID.randomUUID();
        engineService.createPool(engineId, poolSize);
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
}
