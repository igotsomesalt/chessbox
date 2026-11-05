package game;

import chess.Position;

public interface Player {
    String getMove(Position position) throws Exception;
    default String name() {
        return "unkown";
    }
}