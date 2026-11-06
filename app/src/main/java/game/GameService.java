package game;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

import opening.OpeningBook;

public class GameService {

    public static record Result(
        int games, int threads, Map<String, Integer> outcomes, 
        Duration duration, OpeningBook openingBook
    ) {}

    private final Map<Boolean, Supplier<Player>> playerFactories = new HashMap<>();
    private final List<Consumer<String>> logListeners  = new ArrayList<>();

    private List<Game> games = new ArrayList<>();

    public GameService() {
    }

    public void registerPlayerFactory(Supplier<Player> factory, boolean white) {
        playerFactories.put(white, factory); 
    }

    public void registerLogListener(Consumer<String> listener) {
        logListeners.add(listener);
    }

    @SuppressWarnings("null")
    public void start(int numGames, int numThreads, OpeningBook openingBook, boolean verbose) {
        if (numGames < 1) {
            sendMessage(String.format("Number of games must be positive, instead got %d", numGames));
            return;
        }

        if (numThreads < 1) {
            sendMessage(String.format("Number of threads must be positive, instead got %d", numThreads));
            return;
        }

        if (playerFactories.size() < 2) {
            sendMessage(String.format("Number of players must be 2, instead found %s", playerFactories.size()));
            return;
        }

        sendMessage(String.format("Starting %d games on %d worker threads", numGames, numThreads));

        ExecutorCompletionService<Game.Result> completionService = new ExecutorCompletionService<>(
            Executors.newFixedThreadPool(numThreads)
        );

        ConcurrentHashMap<String, Integer> outcomes = new ConcurrentHashMap<>();

        long startNanos = System.nanoTime();

        for (int i = 0; i < numGames; i++) {

            Game game = new Game(
                openingBook.nextPosition(),
                playerFactories.get(true).get(),
                playerFactories.get(false).get()
            );

            games.add(game);
            completionService.submit(game);
        }

        for (int i = 0; i < numGames; i++) {
            try {
                Future<Game.Result> future = completionService.take();
                Game.Result result = future.get();

                Game.Outcome outcome = result.outcome();
                String whiteName = String.format("%s(%d)", result.white().name(), result.white().id());
                String blackName = String.format("%s(%d)", result.black().name(), result.black().id());

                String name = outcome == Game.Outcome.DRAW ? "draw" : 
                    (outcome == Game.Outcome.WHITE_WIN ? whiteName : blackName);

                outcomes.merge(name, 1, Integer::sum);

                sendMessage(String.format(
                    "Game finished:\n%s\n",
                    formatGameResult(result, verbose)
                ));

            } catch (ExecutionException e) {
                sendMessage("Game failed: " + e.getCause() + "\n");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        Duration totalDuration = Duration.ofNanos(System.nanoTime() - startNanos);

        sendMessage(formatResult(new Result(
            numGames, numThreads, outcomes, totalDuration, openingBook
        )));
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

    private String formatGameResult(Game.Result result, boolean verbose) {
        return String.format(
            "outcome = %s\ntermination = %s\n%s%s%sdate = %s\nduration = %d ms", 
            result.outcome().toString(), result.termination().toString(),
            verbose ? "moves = " + result.moves().toString() + "\n" : "",
            verbose ? "starting_fen = " + result.startingFen() + "\n" : "",
            verbose ? "ending_fen = " + result.endingFen() + "\n" : "",
            result.startedAt().toString(), result.duration().toMillis()
        );
    }

    private String formatResult(Result result) {
        return String.format(
            "Worker threads finished in total elasped time of %d ms at %s\n\nopening_book = %s\n" +
            "games = %d\nthreads = %d\noutcomes = %s\ntotal_games_duration = %d ms\nthroughput = %.1f games/s\n",
            result.duration().toMillis(), Instant.now().toString(), result.openingBook().toString(),
            result.games, result.threads, result.outcomes.toString(), result.duration().toMillis(), 
            ((float)result.games) / result.duration().toSeconds()
        );
    }
}
