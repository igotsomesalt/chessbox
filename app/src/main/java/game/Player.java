package game;

import chess.Position;

@FunctionalInterface
public interface Player {
    String getMove(Position position) throws Exception;
}