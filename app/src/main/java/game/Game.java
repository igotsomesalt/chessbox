package game;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import chess.Position;
import chess.UciMoveConverter;

public class Game implements Callable<Game.Result> {

    public static record Result(
        Outcome outcome, Termination termination, List<String> moves, String startingFen, String endingFen, 
        Instant startedAt, Duration duration
    ) {} 

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
    private final String startingFen;

    private volatile boolean running = true;

    public Game(Position startingPosition, Player white, Player black) {
        this.position = startingPosition;
        this.white = white;
        this.black = black;
        this.moves = new ArrayList<>();
        this.startingFen = startingPosition.toFen();
    }

    public void stop() {
        running = false;
    }

    @Override
    public Game.Result call() throws GameRuntimeException {
        Instant startedAt = Instant.now();
        long startNanos = System.nanoTime();

        while (running && !Thread.currentThread().isInterrupted()) {

            Player player = position.whiteToMove ? white : black;
            String uci;

            try {
                uci = player.getMove(position);
            }
            catch(Exception e) {
                throw new GameRuntimeException(this, e);
            }

            int move = UciMoveConverter.toMoveInt(uci, position);

            if (!position.isMoveLegal(move)) {
                throw new GameRuntimeException(this, new IllegalMoveException(uci));
            }

            position.makeMove(move);
            moves.add(uci);

            if (position.isInCheckmate()) {
                return new Result(
                    position.whiteToMove ? Outcome.BLACK_WIN : Outcome.WHITE_WIN, Termination.CHECKMATE,
                    moves, startingFen, position.toFen(), startedAt, 
                    Duration.ofNanos(System.nanoTime() - startNanos)
                );
            }
            if (position.isInStalemate()) {
                return new Result(
                    Outcome.DRAW, Termination.STALEMATE,
                    moves, startingFen, position.toFen(), startedAt, 
                    Duration.ofNanos(System.nanoTime() - startNanos)
                );
            }
        }

        return new Result(
            Outcome.DRAW, Termination.STOPPED,
            moves, startingFen, position.toFen(), startedAt, 
            Duration.ofNanos(System.currentTimeMillis() - startNanos)
        );
    }

    public String getFen() {
        return position.toFen();
    }

    public String getStartFen() {
        return startingFen;
    }

    public List<String> getMoves() {
        return moves;
    }
}