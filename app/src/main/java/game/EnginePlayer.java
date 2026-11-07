package game;

import java.util.UUID;

import chess.Fen;
import chess.Position;
import engine.EngineConfig;
import engine.UciEngine;

public class EnginePlayer implements Player, AutoCloseable {

    private final UciEngine engine;
    private UUID id = UUID.randomUUID();

    public EnginePlayer(EngineConfig config) throws Exception {
        engine = new UciEngine(config);
        engine.initialize();
    }

    @Override
    public String getMove(Position position) throws Exception {
        return engine.getBestMove(Fen.toFen(position), 10);
    }

    @Override 
    public String name() {
        return engine.getConfig().name();
    }

    @Override
    public UUID id() {
        return id;
    }
    
    @Override
    public void close() throws Exception {
        engine.close();
    }

    public EngineConfig getConfig() {
        return engine.getConfig();
    }
}
