package game;

import chess.Fen;
import chess.Position;
import engine.EngineConfig;
import engine.UciEngine;

public class EnginePlayer implements Player, AutoCloseable {

    private final UciEngine engine;

    public EnginePlayer(EngineConfig config) throws Exception {
        engine = new UciEngine(config);
        engine.initialize();
    }

    @Override
    public String getMove(Position position) throws Exception {
        return engine.getBestMove(Fen.toFen(position), 10);
    }

    @Override
    public void close() throws Exception {
        engine.close();
    }
}
