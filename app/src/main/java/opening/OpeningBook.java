package opening;

import chess.Position;

public interface OpeningBook {
    Position nextPosition() throws IllegalArgumentException;
}  
