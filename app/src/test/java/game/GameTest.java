package game;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import engine.EngineConfig;

public class GameTest {

    @Test 
    void play10games4threads() {
        EngineConfig config = new EngineConfig("1", "Stockfish", Path.of("../engines/stockfish.exe"));
        GameService game = new GameService();
        game.registerLogListener(message -> {
            System.out.println(message);
        });

        for (int i = 0; i < 2; i++) {
            game.registerPlayerFactory(() -> {
                try {
                    return new EnginePlayer(config);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create EnginePlayer", e);
                }
            }, i == 0);
        }

        game.start(10, 4, true);
    }
}
