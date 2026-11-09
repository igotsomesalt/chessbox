package chessbox.game;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import chessbox.chess.Position;
import chessbox.opening.OpeningBook;

public class GameService implements AutoCloseable {

    public static record Result(
        int games, int threads, Map<String, Integer> outcomes, 
        OpeningBook openingBook, Duration duration, Instant timestamp
    ) {}

    private final Map<Boolean, PlayerProvider> playerProviders = new HashMap<>();
    private final List<Consumer<GameEvent>> listeners  = new CopyOnWriteArrayList<>();

    private final List<Game> runningGames = new ArrayList<>();
    private final MatchRegistry matches;

    public GameService(MatchRegistry matches) {
        this.matches = matches;
    }

    public void registerPlayerProvider(PlayerProvider provider, boolean white) {
        playerProviders.put(white, provider); 
    }

    public void addListener(Consumer<GameEvent> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<GameEvent> listener) {
        listeners.remove(listener);
    }

    @SuppressWarnings("null")
    public Result submit(
        UUID matchId, boolean verbose
    ) throws IllegalArgumentException, IllegalStateException, Exception {

        MatchConfig match = matches.get(matchId);
        int numGames = match.games();
        int numThreads = match.threads();
        OpeningBook openingBook = match.openingBook();

        if (numGames < 1) {
            throw new IllegalArgumentException(String.format(
                "Number of games must be positive: %d\n", numGames
            ));
        }

        if (numThreads < 1) {
            throw new IllegalArgumentException(String.format(
                "Number of threads must be positive: %d\n", numThreads
            ));
        }

        if (playerProviders.size() < 2) {
            throw new IllegalStateException(
                String.format("Number of players must be 2, instead found %s\n", playerProviders.size())
            );
        }

        emit(new GameEvent.MatchStarted(numGames, numThreads, openingBook, Instant.now()));

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        Result result = null;

        try {
            ExecutorCompletionService<Game.Result> completionService = new ExecutorCompletionService<>(executor);

            ConcurrentHashMap<String, Integer> outcomes = new ConcurrentHashMap<>();

            for (int i = 0; i < numGames; i++) {
                boolean swap = i % 2 == 0;

                Position position = openingBook.nextPosition();

                PlayerProvider whiteFactory = playerProviders.get(swap);
                PlayerProvider blackFactory = playerProviders.get(!swap);

                completionService.submit(() -> {
                    Player white = null;
                    Player black = null;

                    try {
                        white = whiteFactory.acquire();
                        black = blackFactory.acquire();

                        Game game = new Game(position, white, black);
                        return game.call();

                    } 
                    
                    finally {
                        if (black != null) {
                            blackFactory.release(black);
                        }

                        if (white != null) {
                            whiteFactory.release(white);
                        }
                    }
                });
            }

            emit(new GameEvent.AllGamesSubmitted(Instant.now()));
            long startNanos = System.nanoTime();

            for (int i = 0; i < numGames; i++) {
                int completed = i + 1;
                int percent = completed * 100 / numGames;

                double seconds = (System.nanoTime() - startNanos) / 1_000_000_000.0;
                double throughput = completed / seconds;

                try {
                    Future<Game.Result> future = completionService.take();
                    Game.Result gameResult = future.get();

                    Game.Outcome outcome = gameResult.outcome();

                    String whiteName = String.format("%s(%s)", 
                        gameResult.white().name(), gameResult.white().id()
                        .toString().replace("-", "").substring(0, 7));

                    String blackName = String.format("%s(%s)", 
                        gameResult.black().name(), gameResult.black().id()
                        .toString().replace("-", "").substring(0, 7));

                    String name = outcome == Game.Outcome.DRAW ? "draw" : 
                        (outcome == Game.Outcome.WHITE_WIN ? whiteName : blackName);

                    outcomes.merge(name, 1, Integer::sum);

                    if (verbose) emit(new GameEvent.GameFinishedVerbose(gameResult, Instant.now()));

                    emit(new GameEvent.GameFinished(completed, percent, numGames, throughput, Instant.now()));
                    
                } catch (ExecutionException e) {
                    emit(new GameEvent.GameFinished(completed, percent, numGames, throughput, Instant.now()));
                    emit(new GameEvent.GameFailed(e.getCause()));

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            Duration totalDuration = Duration.ofNanos(System.nanoTime() - startNanos);
            result = new Result(numGames, numThreads, outcomes, openingBook, totalDuration, Instant.now());

            emit(new GameEvent.MatchFinished(result));
        }

        catch (Exception e) {
            executor.close();
            throw e;
        }

        return result;
    }

    public void stop() {
        for (Game game : runningGames) {
            game.stop();
        }
    }

    private void emit(GameEvent event) {
        for (Consumer<GameEvent> listener : listeners) {
            listener.accept(event);
        }
    }

	@Override
	public void close() throws Exception {
        for (Game game : runningGames) {
            game.stop();
        }

        for (PlayerProvider provider : playerProviders.values()) {
            if (provider instanceof AutoCloseable p) {
                p.close();
            }
        }
	}
}
