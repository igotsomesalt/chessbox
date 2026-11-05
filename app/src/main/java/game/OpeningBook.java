package game;

import chess.Position;

public interface OpeningBook {
    Position nextPosition() throws IllegalArgumentException;
}  
