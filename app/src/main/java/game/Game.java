package game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import chess.Position;
import chess.UciMoveConverter;

public class Game implements Callable<Game.Result> {

    public static record Result(Outcome outcome, Termination termination, List<String> moves, String fen) {} 

    public enum Outcome {
        WHITE_WIN,
        BLACK_WIN,
        DRAW
    }

    public enum Termination {
        STOPPED,
        CHECKMATE,
        STALEMATE,
        REPETITION, // TODO: do repition and below later
        FIFTY_MOVE_RULE, 
        SEVENTY_FIVE_MOVE_RULE,
        DEAD_POSITION,
        RESIGNATION,
        TIMEOUT,
        AGREEMENT
    }

    private final Position position;
    private final Player white;
    private final Player black;

    private final List<String> moves;

    private volatile boolean running = true;

    public Game(
        Position startingPosition,
        Player white,
        Player black
    ) {

        this.position = new Position(startingPosition);
        this.white = white;
        this.black = black;
        this.moves = new ArrayList<>();
    }

    public void stop() {
        running = false;
    }

    @Override
    public Game.Result call() throws Exception {
        while (running && !Thread.currentThread().isInterrupted()) {

            Player player = position.whiteToMove ? white : black;
            String uci = player.getMove(position);
            int move = UciMoveConverter.toMoveInt(uci, position);

            if (!position.isMoveLegal(move)) {
                throw new IllegalMoveException(uci);
            }

            position.makeMove(move);
            moves.add(uci);

            if (position.isInCheckmate()) {
                return new Result(
                    position.whiteToMove ? Outcome.BLACK_WIN : Outcome.WHITE_WIN, Termination.CHECKMATE,
                    moves, position.toFen()
                );
            }
            if (position.isInStalemate()) {
                return new Result(Outcome.DRAW, Termination.STALEMATE, moves, position.toFen());
            }
        }

        return new Result(Outcome.DRAW, Termination.STOPPED, moves, position.toFen());
    }
}