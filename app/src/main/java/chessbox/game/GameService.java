package chessbox.game;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class GameService implements AutoCloseable {

    private static final int MAX_GAMES = 9999;
    private static final int MAX_THREADS = 64;

    private final MatchRegistry matches;

    private final Map<UUID, MatchRunner> activeMatches = new ConcurrentHashMap<>();

    private final List<Consumer<GameEvent>> listeners = new CopyOnWriteArrayList<>();

    private final ExecutorService coordinatorExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public GameService(MatchRegistry matches) {
        this.matches = Objects.requireNonNull(matches);
    }

    public void addListener(Consumer<GameEvent> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<GameEvent> listener) {
        listeners.remove(listener);
    }

    public MatchResult submit(UUID matchId, boolean verbose) throws Exception {
        try {
            return submitAsync(matchId, verbose).get();
        } 

        catch (ExecutionException e) {
            Throwable cause = e.getCause();

            if (cause instanceof Exception exception) {
                throw exception;
            }

            if (cause instanceof Error error) {
                throw error;
            }

            throw new RuntimeException(cause);

        } 
        
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    public CompletableFuture<MatchResult> submitAsync(UUID matchId, boolean verbose) {
        if (closed.get()) {
            throw new IllegalStateException("GameService is closed");
        }

        MatchConfig config = matches.get(matchId);
        validate(config);

        MatchRunner runner = new MatchRunner(config, verbose, this::emit);

        if (activeMatches.putIfAbsent(matchId, runner) != null) {
            runner.close();
            throw new IllegalStateException(
                "Match is already running: " + matchId
            );
        }

        try {
            CompletableFuture<MatchResult> future =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return runner.call();

                    } catch (Exception e) {
                        runner.cancel();
                        throw new CompletionException(e);

                    } finally {
                        activeMatches.remove(matchId, runner);
                    }
                }, coordinatorExecutor);

            // Handle concurrent shutdown.
            if (closed.get()) {
                runner.cancel();
            }

            return future;

        } catch (RejectedExecutionException e) {
            activeMatches.remove(matchId, runner);
            runner.cancel();
            throw e;
        }
    }

    public void stop(UUID matchId) {
        matches.get(matchId);

        MatchRunner runner = activeMatches.get(matchId);

        if (runner == null) {
            throw new IllegalStateException(
                "Match is not running: " + matchId
            );
        }

        runner.cancel();
    }

    private void emit(GameEvent event) {
        for (Consumer<GameEvent> listener : listeners) {
            try {
                listener.accept(event);
            } 

            catch (RuntimeException ignored) {
                // A listener must not crash the match.
            }
        }
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        for (MatchRunner runner : activeMatches.values()) {
            runner.cancel();
        }

        coordinatorExecutor.close();
    }

    private void validate(MatchConfig config) {
        if (config == null) {
            throw new NoSuchElementException("Match not found");
        }

        if (config.games() < 1 || config.games() > MAX_GAMES) {
            throw new IllegalArgumentException(
                "Number of games must be between 1 and " + MAX_GAMES
            );
        }

        if (config.threads() < 1 || config.threads() > MAX_THREADS) {
            throw new IllegalArgumentException(
                "Number of threads must be between 1 and " + MAX_THREADS
            );
        }

        if (config.p1() == null || config.p2() == null) {
            throw new IllegalStateException(
                "Match requires two player providers"
            );
        }

        if (config.openingBook() == null) {
            throw new IllegalArgumentException(
                "Match requires an opening book"
            );
        }
    }
}