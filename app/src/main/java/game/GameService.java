package game;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

import chess.Position;
import game.Game.Result;

public class GameService {

    private Position position;
    private final Map<Boolean, Supplier<Player>> playerFactories = new HashMap<>();
    private final List<Consumer<String>> logListeners  = new ArrayList<>();

    private ExecutorService executor;
    private List<Game> games = new ArrayList<>();

    public GameService() {
        this.position = new Position();
    }

    public void registerPlayerFactory(Supplier<Player> factory, boolean white) {
        playerFactories.put(white, factory); 
    }

    public void registerLogListener(Consumer<String> listener) {
        logListeners.add(listener);
    }

    public void start(int numGames, int numThreads, boolean verbose) {
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
        executor = Executors.newFixedThreadPool(numThreads);
        long startNanos = System.nanoTime();

        for (int i = 0; i < numGames; i++) {

            Game game = new Game(
                new Position(position),
                playerFactories.get(true).get(),
                playerFactories.get(false).get()
            );

            games.add(game);
            Future<Result> future = executor.submit(game);

            try {
                Result result = future.get();
                sendMessage(String.format("Game %d finished:\n" + formatResult(result, verbose) + "\n", i));

            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                sendMessage("Game failed: " + cause.getMessage());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        sendMessage(String.format(
            "Worker threads finished in %d ms at %s", 
            Duration.ofNanos(System.nanoTime() - startNanos).toMillis(), Instant.now().toString()
        ));
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

    private String formatResult(Result result, boolean verbose) {
        return String.format(
            "outcome=%s\ntermination=%s\n%s%sdate=%s\nduration=%d ms", 
            result.outcome().toString(), result.termination().toString(),
            verbose ? "moves=" + result.moves().toString() + "\n" : "",
            verbose ? "fen=" + result.fen() + "\n" : "",
            result.startedAt().toString(), result.duration().toMillis()
        );
    }
}
