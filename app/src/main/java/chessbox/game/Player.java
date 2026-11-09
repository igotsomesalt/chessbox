package chessbox.game;

import java.util.UUID;

import chessbox.chess.Position;
import chessbox.util.Indentifiable;

public interface Player extends Indentifiable<UUID> {
    String getMove(Position position) throws Exception;

    default String name() {
        return "unknown";
    }
}