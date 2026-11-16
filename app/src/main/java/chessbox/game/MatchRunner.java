package chessbox.game;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import chessbox.chess.Position;
import chessbox.opening.OpeningBook;

public final class MatchRunner implements Callable<MatchResult>, AutoCloseable {

    private final MatchConfig config;
    private final boolean verbose;
    private final Consumer<GameEvent> eventSink;

    private final ExecutorService executor;
    private final Set<Game> activeGames = ConcurrentHashMap.newKeySet();
    private final Set<Future<Game.Result>> futures = ConcurrentHashMap.newKeySet();

    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);

    public MatchRunner(MatchConfig config, boolean verbose, Consumer<GameEvent> eventSink) {
        this.config = Objects.requireNonNull(config);
        this.verbose = verbose;
        this.eventSink = Objects.requireNonNull(eventSink);
        this.executor = Executors.newFixedThreadPool(config.threads());
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void cancel() {
        if (!cancelled.compareAndSet(false, true)) {
            return;
        }

        for (Future<Game.Result> future : futures) {
            future.cancel(true);
        }

        executor.shutdownNow();

        for (Game game : activeGames) {
            try {
                game.stop();
            } catch (RuntimeException ignored) {
                // Continue stopping remaining activeGames.
            }
        }
    }

    private void checkCancelled() {
        if (isCancelled()) {
            throw new CancellationException("Match stopped");
        }
    }

    private void emit(GameEvent event) {
        eventSink.accept(event);
    }

    @Override
    public MatchResult call() throws Exception {    
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException(
                "MatchRunner has already been started"
            );
        }

        return runMatch();
    }

    private MatchResult runMatch() throws Exception {

        int numGames = config.games();
        int numThreads = config.threads();
        OpeningBook openingBook = config.openingBook();

        long startNanos = System.nanoTime();

        Map<String, Integer> outcomes = new HashMap<>();

        int completed = 0;
        int failed = 0;
        int submitted = 0;

        ExecutorCompletionService<Game.Result> completion = new ExecutorCompletionService<>(executor);

        try {
            emit(new GameEvent.MatchStarted(numGames, numThreads, openingBook, Instant.now()));

            // Submit activeGames until all are submitted or cancellation occurs.
            for (int i = 0; i < numGames; i++) {
                if (isCancelled()) {
                    break;
                }

                boolean swap = i % 2 == 0;
                Position position = openingBook.nextPosition();
                PlayerProvider whiteProvider = swap ? config.p1() : config.p2();
                PlayerProvider blackProvider = swap ? config.p2() : config.p1();

                try {
                    Future<Game.Result> future = completion.submit(() ->
                        playGame(position, whiteProvider, blackProvider)
                    );

                    futures.add(future);
                    submitted++;

                    if (isCancelled()) {
                        future.cancel(true);
                    }

                } catch (RejectedExecutionException e) {
                    if (isCancelled()) {
                        break;
                    }
                    throw e;
                }
            }

            if (!isCancelled()) {
                emit(new GameEvent.AllGamesSubmitted(Instant.now()));
            }

            // Collect results, without blocking indefinitely on take().
            while (completed + failed < submitted) {
                if (isCancelled()) break;

                Future<Game.Result> future = completion.poll(100, TimeUnit.MILLISECONDS);

                if (future == null) continue;

                try {
                    Game.Result gameMatchResult = future.get();

                    completed++;

                    String name = outcomeName(gameMatchResult);
                    outcomes.merge(name, 1, Integer::sum);

                    if (verbose) {
                        emit(new GameEvent.GameFinishedVerbose(
                            gameMatchResult, Instant.now()
                        ));
                    }

                } catch (CancellationException e) {
                    if (!isCancelled()) {
                        failed++;
                        emit(new GameEvent.GameFailed(e));
                    }

                } catch (ExecutionException e) {
                    if (!isCancelled()) {
                        failed++;
                        emit(new GameEvent.GameFailed(e.getCause()));
                    }
                }

                int processed = completed + failed;

                double seconds = (System.nanoTime() - startNanos) / 1_000_000_000.0;

                double throughput = seconds > 0 ? processed / seconds : 0;

                emit(new GameEvent.GameFinished(
                    processed,
                    processed * 100 / numGames,
                    numGames,
                    throughput,
                    Instant.now()
                ));
            }

        } finally {
            // No new activeGames may begin after this point.
            if (isCancelled()) {
                executor.shutdownNow();
            } else {
                executor.shutdown();
            }

            // Do not declare a match finished while engines are
            // still using player providers.
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                cancel();

                throw new IllegalStateException(
                    "Match workers did not terminate within 30 seconds"
                );
            }
        }

        int cancelled = numGames - completed - failed;

        MatchStatus status = isCancelled()
            ? MatchStatus.CANCELLED
            : MatchStatus.COMPLETED;

        Duration duration = Duration.ofNanos(
            System.nanoTime() - startNanos
        );

        MatchResult result = new MatchResult(
            numGames, numThreads, completed, failed, cancelled, 
            status, Map.copyOf(outcomes), openingBook, duration, Instant.now()
        );

        emit(new GameEvent.MatchFinished(result));

        return result;
    }

    private Game.Result playGame(Position position, PlayerProvider whiteProvider, PlayerProvider blackProvider) throws Exception {

        Player white = null;
        Player black = null;
        Game game = null;

        try {
            checkCancelled();

            white = whiteProvider.acquire();
            checkCancelled();

            black = blackProvider.acquire();
            checkCancelled();

            game = new Game(position, white, black);
            activeGames.add(game);

            if (isCancelled()) {
                game.stop();
                throw new CancellationException("Match stopped");
            }

            Game.Result result = game.call();

            if (result.termination() == Game.Termination.STOPPED) {
                throw new CancellationException("Game stopped");
            }

            return result;

        } finally {
            if (game != null) {
                activeGames.remove(game);
            }

            try {
                if (black != null) {
                    blackProvider.release(black);
                }
            } finally {
                if (white != null) {
                    whiteProvider.release(white);
                }
            }
        }
    }

    private String outcomeName(Game.Result result) {
        if (result.outcome() != Game.Outcome.DRAW) return result.outcome().toString();
        return result.termination().toString();
    }

    @Override
    public void close() {
        cancel();
    }
}
