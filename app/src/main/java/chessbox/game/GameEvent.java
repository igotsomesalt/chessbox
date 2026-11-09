package chessbox.game;

import java.time.Instant;

import chessbox.opening.OpeningBook;

public sealed interface GameEvent {

    record MatchStarted(
        int games,
        int threads,
        OpeningBook opening,
        Instant timestamp
    ) implements GameEvent {}

    record MatchFinished(
        GameService.Result result
    ) implements GameEvent {}

    record AllGamesSubmitted(
        Instant timestamp
    ) implements GameEvent {}

    record GameFinishedVerbose(
        Game.Result result,
        Instant timestamp
    ) implements GameEvent {}

    record GameFinished(
        int completed,
        int percent,
        double numGames, 
        double throughput,
        Instant timestamp
    ) implements GameEvent {}

    record GameFailed(
        Throwable cause
    ) implements GameEvent {}
}
