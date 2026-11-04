package game;

import chess.Move;

public class IllegalMoveException extends Exception {
    public IllegalMoveException(Move move) {
        super(String.format("The move '%s' is illegal", move.toUci()));
    }

    public IllegalMoveException(String move) {
        super(String.format("The move '%s' is illegal", move));
    }
}
