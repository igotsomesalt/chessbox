package chessbox.opening;

import chessbox.chess.Position;

public interface OpeningBook {
    Position nextPosition() throws IllegalArgumentException;
}  
