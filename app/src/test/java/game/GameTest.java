package game;

import org.junit.jupiter.api.Test;

public class GameTest {

    @Test 
    void play() {
        String exe = "../engines/stockfish.exe";
        GameService game = new GameService();
        game.registerLogListener(message -> {
            System.out.println(message);
        });

        for (int i = 0; i < 2; i++) {
            game.registerPlayerFactory(() -> {
                try {
                    return new EnginePlayer(exe);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create EnginePlayer", e);
                }
            }, i == 0);
        }

        game.start(10, 4);
    }
}
