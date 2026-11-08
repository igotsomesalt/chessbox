package chessbox.game;

import java.io.IOException;
import java.util.UUID;

import chessbox.chess.Fen;
import chessbox.chess.Position;
import chessbox.engine.EngineConfig;
import chessbox.engine.UciEngine;

public class EnginePlayer implements Player, AutoCloseable {

    private final UciEngine engine;
    private UUID id;

    public EnginePlayer(EngineConfig config, UUID id) throws IOException {
        this.engine = new UciEngine(config);
        this.engine.initialize();
        this.id = id;
    }

    public EnginePlayer(UciEngine engine, UUID id) {
        this.engine = engine;
        this.id = id;
    }

    public UciEngine getEngine() {
        return engine;
    }

    @Override
    public String getMove(Position position) throws IOException {
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
