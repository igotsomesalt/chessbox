package chessbox.game;

import java.util.UUID;

import chessbox.chess.Position;

public interface Player {
    String getMove(Position position) throws Exception;

    default String name() {
        return "unknown";
    }

    UUID id();
}