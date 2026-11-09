package chessbox.cli.listener;

import java.io.PrintStream;
import java.util.function.Consumer;

import chessbox.game.GameEvent;
import chessbox.game.GameService;

public class GameEventPrinter implements Consumer<GameEvent> {


    private final PrintStream out;
    private final PrintStream err;

    public GameEventPrinter(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
    }

    @Override
    public synchronized void accept(GameEvent event) {
        switch (event) {
            case GameEvent.MatchStarted e -> {
                out.printf("""
                    Starting %d games on %d worker threads:
                        %-8s [%s]
                        %-8s [%s]
                    """,
                    e.games(), e.threads(), "opening", e.opening(), "timestamp", e.timestamp()
                );
                out.flush();
            }

            case GameEvent.AllGamesSubmitted e -> {
                out.printf("All games submitted at %s%n", e.timestamp());
                out.flush();
            }

            case GameEvent.GameFinished e -> {
                String message = String.format(
                    "\rPlaying games: [%3d%%] (%d/%.0f games) | %.2f games/s ",
                    e.percent(), e.completed(), e.numGames(), e.throughput()
                );
                out.printf("\r%-80s", message);
                out.flush();
            }

            case GameEvent.GameFinishedVerbose e -> {
                out.printf("""
                    %nGame finished at %s:
                        %-12s [%s]
                        %-12s [%s]
                        %-12s [%s]
                        %-12s [%s]
                        %-12s [%s]
                        %-12s [%s]
                        %-12s %s
                    """, e.timestamp(),
                    "white", e.result().white().name(), "black", e.result().black().name(),
                    "outcome", e.result().outcome(), "termination", e.result().termination(),
                    "start fen", e.result().startingFen(), "end fen", e.result().endingFen(),
                    "moves", e.result().moves()
                );
                out.flush();
            }

            case GameEvent.GameFailed e -> {
                err.printf(
                    "%nGame failed: %s%n",
                    e.cause().getMessage()
                );
                err.flush();
            }

            case GameEvent.MatchFinished e -> {
                GameService.Result result = e.result();

                out.printf("""

                    Match finished
                        %-8s [%d]
                        %-8s [%d]
                        %-8s [%s ms]
                        %-8s [%s]
                    """,
                    "games", result.games(), "threads", result.threads(), 
                    "duration", result.duration().toMillis(), "outcomes", result.outcomes()
                );
                out.flush();
            }
        }
    }
}
