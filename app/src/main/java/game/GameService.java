package game;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import chess.Position;
import opening.OpeningBook;

public class GameService {

    public static record Result(
        int games, int threads, Map<String, Integer> outcomes, 
        Duration duration, OpeningBook openingBook
    ) {}

    private final Map<Boolean, PlayerProvider> playerProviders = new HashMap<>();
    private final List<Consumer<String>> logListeners  = new CopyOnWriteArrayList<>();

    private List<Game> games = new ArrayList<>();

    public GameService() {
    }

    public void registerPlayerProvider(PlayerProvider provider, boolean white) {
        playerProviders.put(white, provider); 
    }

    public void registerLogListener(Consumer<String> listener) {
        logListeners.add(listener);
    }

    @SuppressWarnings("null")
    public void start(
        int numGames, int numThreads, OpeningBook openingBook, boolean verbose
    ) throws IllegalArgumentException, IllegalStateException, Exception {

        if (numGames < 1) {
            throw new IllegalArgumentException(String.format(
                "Number of games must be positive, instead got %d\n", numGames
            ));
        }

        if (playerProviders.size() < 2) {
            throw new IllegalStateException(
                String.format("Number of players must be 2, instead found %s\n", playerProviders.size())
            );
        }

        sendMessage(String.format("Starting %d games on %d worker threads\n", numGames, numThreads));

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        try {
            ExecutorCompletionService<Game.Result> completionService = new ExecutorCompletionService<>(
                Executors.newFixedThreadPool(numThreads)
            );

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
                    } finally {
                        if (black != null) {
                            blackFactory.release(black);
                        }

                        if (white != null) {
                            whiteFactory.release(white);
                        }
                    }
                });
            }

            sendMessage("All games submitted at " + Instant.now() + "\n");
            long startNanos = System.nanoTime();

            for (int i = 0; i < numGames; i++) {
                try {
                    Future<Game.Result> future = completionService.take();
                    Game.Result result = future.get();

                    Game.Outcome outcome = result.outcome();

                    String whiteName = String.format("%s(%s)", 
                        result.white().name(), result.white().id()
                        .toString().replace("-", "").substring(0, 7));

                    String blackName = String.format("%s(%s)", 
                        result.black().name(), result.black().id()
                        .toString().replace("-", "").substring(0, 7));

                    String name = outcome == Game.Outcome.DRAW ? "draw" : 
                        (outcome == Game.Outcome.WHITE_WIN ? whiteName : blackName);

                    outcomes.merge(name, 1, Integer::sum);

                    if (verbose) {
                        sendMessage(String.format(
                            "Game finished:\n%s\n",
                            formatGameResult(result)
                        ));
                    }

                    else {
                        int completed = i + 1;
                        int percent = completed * 100 / numGames;

                        double seconds = (System.nanoTime() - startNanos) / 1_000_000_000.0;
                        double throughput = completed / seconds;

                        sendMessage(String.format(
                            "\rPlaying games: [%3d%%] (%d/%d games) | %.2f games/s ",
                            percent, completed, numGames, throughput
                        ));
                    }
                    
                } catch (ExecutionException e) {
                    sendMessage("Game failed: " + e.getCause() + "\n");

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            Duration totalDuration = Duration.ofNanos(System.nanoTime() - startNanos);

            sendMessage("\n");
            sendMessage(formatResult(new Result(
                numGames, numThreads, outcomes, totalDuration, openingBook
            )));
        }
        catch (Exception e) {
            executor.close();
            throw e;
        }
    }

    public void stop() {
        for (Game game : games) {
            game.stop();
        }
    }

    private void sendMessage(String message) {
        for (Consumer<String> listener : logListeners) {
            listener.accept(message);
        }
    }

    private String formatGameResult(Game.Result result) {
        return String.format(
            "outcome = %s\ntermination = %s\nmoves = %s\nstarting_fen = %s\nending_fen = %s\ndate = %s\nduration = %d ms\n", 
            result.outcome().toString(), result.termination().toString(), result.moves().toString(),
            result.startingFen(),result.endingFen(),
            result.startedAt().toString(), result.duration().toMillis()
        );
    }

    private String formatResult(Result result) {
        return String.format(
            "\nWorker threads finished in total elasped time of %d ms at %s\n\nopening_book = %s\n" +
            "games = %d\nthreads = %d\noutcomes = %s\ntotal_games_duration = %d ms\nthroughput = %.2f games/s\n\n",
            result.duration().toMillis(), Instant.now().toString(), result.openingBook().toString(),
            result.games, result.threads, result.outcomes.toString(), result.duration().toMillis(), 
            ((float)result.games) / result.duration().toSeconds()
        );
    }
}
