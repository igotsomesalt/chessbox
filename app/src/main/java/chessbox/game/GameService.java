package chessbox.game;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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

public class GameService implements AutoCloseable {

    private static final int MAX_GAMES = 9999;
    private static final int MAX_THREADS = 64;

    private static final class ActiveMatch {

        final ExecutorService executor;
        final Set<Game> games = ConcurrentHashMap.newKeySet();
        final CopyOnWriteArrayList<Future<Game.Result>> futures = new CopyOnWriteArrayList<>();
        final AtomicBoolean cancelled = new AtomicBoolean(false);
        volatile CompletableFuture<Result> completion;

        ActiveMatch(int threads) {
            executor = Executors.newFixedThreadPool(threads);
        }

        boolean isCancelled() {
            return cancelled.get();
        }

        void checkCancelled() {
            if (isCancelled()) {
                throw new CancellationException("Match stopped");
            }
        }

        void cancel() {
            if (!cancelled.compareAndSet(false, true)) {
                return;
            }

            executor.shutdownNow();

            for (Future<Game.Result> future : futures) {
                future.cancel(true);
            }

            for (Game game : games) {
                try {
                    game.stop();
                } catch (RuntimeException e) {
                    continue;
                }
            }
        }
    }

    public static record Result(
        int games, int threads, int completed, int failed, int cancelled,
        MatchStatus status, Map<String, Integer> outcomes, 
        OpeningBook openingBook, Duration duration, Instant timestamp
    ) {}

    private final List<Consumer<GameEvent>> listeners  = new CopyOnWriteArrayList<>();
    private final Map<UUID, ActiveMatch> activeMatches = new ConcurrentHashMap<>();
    private final MatchRegistry matches;

    private final ExecutorService coordinatorExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public GameService(MatchRegistry matches) {
        this.matches = matches;
    }

    public void addListener(Consumer<GameEvent> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<GameEvent> listener) {
        listeners.remove(listener);
    }

    public Result submit(UUID matchId, boolean verbose) throws Exception {
        try {
            return submitAsync(matchId, verbose).get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();

            if (cause instanceof Exception exception) {
                throw exception;
            }

            if (cause instanceof Error error) {
                throw error;
            }

            throw new RuntimeException(cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    public CompletableFuture<Result> submitAsync(UUID matchId, boolean verbose) {
        if (closed.get()) {
            throw new IllegalStateException("GameService is closed");
        }

        MatchConfig config = matches.get(matchId);
        validate(config);

        ActiveMatch match = new ActiveMatch(config.threads());

        if (activeMatches.putIfAbsent(matchId, match) != null) {
            match.executor.shutdown();
            throw new IllegalStateException(
                "Match is already running: " + matchId
            );
        }

        try {
            CompletableFuture<Result> future =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return runMatch(match, config, verbose);

                    } catch (Exception e) {
                        match.cancel();
                        throw new CompletionException(e);

                    } finally {
                        activeMatches.remove(matchId, match);
                        match.executor.shutdown();
                    }
                }, coordinatorExecutor);

            match.completion = future;

            // Cover a concurrent service shutdown.
            if (closed.get()) {
                match.cancel();
            }

            return future;

        } catch (RejectedExecutionException e) {
            activeMatches.remove(matchId, match);
            match.cancel();
            throw e;
        }
    }

    private Result runMatch(ActiveMatch match, MatchConfig config, boolean verbose) throws Exception {

        int numGames = config.games();
        int numThreads = config.threads();
        OpeningBook openingBook = config.openingBook();

        long startNanos = System.nanoTime();

        Map<String, Integer> outcomes = new HashMap<>();

        int completed = 0;
        int failed = 0;
        int submitted = 0;

        ExecutorCompletionService<Game.Result> completion =
            new ExecutorCompletionService<>(match.executor);

        try {
            emit(new GameEvent.MatchStarted(
                numGames, numThreads, openingBook, Instant.now()
            ));

            // Submit games until all are submitted or cancellation occurs.
            for (int i = 0; i < numGames; i++) {
                if (match.isCancelled()) {
                    break;
                }

                boolean swap = i % 2 == 0;

                Position position = openingBook.nextPosition();

                PlayerProvider whiteProvider =
                    swap ? config.p1() : config.p2();

                PlayerProvider blackProvider =
                    swap ? config.p2() : config.p1();

                try {
                    Future<Game.Result> future = completion.submit(() ->
                        playGame(match, position, whiteProvider, blackProvider)
                    );

                    match.futures.add(future);
                    submitted++;

                    if (match.isCancelled()) {
                        future.cancel(true);
                    }

                } catch (RejectedExecutionException e) {
                    if (match.isCancelled()) {
                        break;
                    }
                    throw e;
                }
            }

            if (!match.isCancelled()) {
                emit(new GameEvent.AllGamesSubmitted(Instant.now()));
            }

            // Collect results, without blocking indefinitely on take().
            while (completed + failed < submitted) {
                if (match.isCancelled()) {
                    break;
                }

                Future<Game.Result> future =
                    completion.poll(100, TimeUnit.MILLISECONDS);

                if (future == null) {
                    continue;
                }

                try {
                    Game.Result gameResult = future.get();

                    completed++;

                    String name = outcomeName(gameResult);
                    outcomes.merge(name, 1, Integer::sum);

                    if (verbose) {
                        emit(new GameEvent.GameFinishedVerbose(
                            gameResult, Instant.now()
                        ));
                    }

                } catch (CancellationException e) {
                    if (!match.isCancelled()) {
                        failed++;
                        emit(new GameEvent.GameFailed(e));
                    }

                } catch (ExecutionException e) {
                    if (!match.isCancelled()) {
                        failed++;
                        emit(new GameEvent.GameFailed(e.getCause()));
                    }
                }

                int processed = completed + failed;

                double seconds =
                    (System.nanoTime() - startNanos) / 1_000_000_000.0;

                double throughput =
                    seconds > 0 ? processed / seconds : 0;

                emit(new GameEvent.GameFinished(
                    processed,
                    processed * 100 / numGames,
                    numGames,
                    throughput,
                    Instant.now()
                ));
            }

        } finally {
            // No new games may begin after this point.
            if (match.isCancelled()) {
                match.executor.shutdownNow();
            } else {
                match.executor.shutdown();
            }

            // Do not declare a match finished while engines are
            // still using player providers.
            if (!match.executor.awaitTermination(30, TimeUnit.SECONDS)) {
                match.cancel();

                throw new IllegalStateException(
                    "Match workers did not terminate within 30 seconds"
                );
            }
        }

        int cancelled = numGames - completed - failed;

        MatchStatus status = match.isCancelled()
            ? MatchStatus.CANCELLED
            : MatchStatus.COMPLETED;

        Duration duration = Duration.ofNanos(
            System.nanoTime() - startNanos
        );

        Result result = new Result(
            numGames, numThreads, completed, failed, cancelled, 
            status, Map.copyOf(outcomes), openingBook, duration, Instant.now()
        );

        emit(new GameEvent.MatchFinished(result));

        return result;
    }

    public void stop(UUID matchId) {
        matches.get(matchId);

        ActiveMatch match = activeMatches.get(matchId);

        if (match == null) {
            throw new IllegalStateException(
                "Match is not running: " + matchId
            );
        }

        match.cancel();
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        for (ActiveMatch match : activeMatches.values()) {
            match.cancel();
        }

        coordinatorExecutor.close();
    }

    private void validate(MatchConfig config) {
        if (config.games() > MAX_GAMES) {
            throw new IllegalArgumentException("Number of games exceeds game limit of " + MAX_GAMES);
        }

        if (config.threads() > MAX_THREADS) {
            throw new IllegalArgumentException("Number of threads exceeds thread limit of " + MAX_THREADS);
        }

        int pcount = (config.p1() == null ? 0 : 1) + (config.p2() == null ? 0 : 1);

        if (pcount != 2) {
            String s = pcount == 1 ? "1 was" : (pcount == 0 ? "0 were" : "2 were");
            throw new IllegalStateException(String.format("Match expects 2 players, but %s found", s));
        }
    }

    private Game.Result playGame(
        ActiveMatch match, Position position, PlayerProvider whiteProvider, PlayerProvider blackProvider
    ) throws Exception {

        Player white = null;
        Player black = null;
        Game game = null;

        try {
            match.checkCancelled();

            white = whiteProvider.acquire();
            match.checkCancelled();

            black = blackProvider.acquire();
            match.checkCancelled();

            game = new Game(position, white, black);
            match.games.add(game);

            if (match.isCancelled()) {
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
                match.games.remove(game);
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

    private void emit(GameEvent event) {
        for (Consumer<GameEvent> listener : listeners) {
            listener.accept(event);
        }
    }
}
