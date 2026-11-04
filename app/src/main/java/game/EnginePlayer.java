package game;

import chess.Fen;
import chess.Position;
import engine.UciEngine;

public class EnginePlayer implements Player, AutoCloseable {

    private final UciEngine engine;

    public EnginePlayer(String executable) throws Exception {
        engine = new UciEngine(executable);
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
