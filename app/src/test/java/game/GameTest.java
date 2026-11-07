package game;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import engine.EngineConfig;
import opening.FenOpeningBook;
import opening.OpeningBook;

public class GameTest {

    @Test 
    void play10games4threadsVerboseFalse() {
        GameService game = new GameService();
        OpeningBook openingBook = getOpeningBook("/openings/4mvs_+90_+99.epd");
        register2stockfishs(game, 4);
        game.start(10, 4, openingBook, false);
    }

    @Test
    void play10games4threadVerboseTrue() {
        GameService game = new GameService();
        OpeningBook openingBook = getOpeningBook("/openings/4mvs_+90_+99.epd");
        register2stockfishs(game, 4);
        game.start(10, 4, openingBook, true);
    }

    @Test
    void play10games1threadVerboseFalse() {
        GameService game = new GameService();
        OpeningBook openingBook = getOpeningBook("/openings/4mvs_+90_+99.epd");
        register2stockfishs(game, 4);
        game.start(10, 1, openingBook, false);
    }

    @Test 
    void play100games4threadsVerboseFalse() {
        GameService game = new GameService();
        OpeningBook openingBook = getOpeningBook("/openings/4mvs_+90_+99.epd");
        register2stockfishs(game, 4);
        game.start(100, 4, openingBook, false);
    }

    @Test 
    void play100games1threadsVerboseFalse() {
        GameService game = new GameService();
        OpeningBook openingBook = getOpeningBook("/openings/4mvs_+90_+99.epd");
        register2stockfishs(game, 1);
        game.start(100, 1, openingBook, false);
    }

    private OpeningBook getOpeningBook(String path) {
        OpeningBook openingBook;

        try {
            openingBook = new FenOpeningBook(path, getClass().getResourceAsStream(path));
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return openingBook;
    }

    private void register2stockfishs(GameService game, int poolSize) {
        EngineConfig config1 = new EngineConfig(0, "Stockfish", Path.of("../engines/stockfish.exe"));
        EngineConfig config2 = new EngineConfig(1, "Stockfish", Path.of("../engines/stockfish.exe"));

        game.registerLogListener(message -> {
            System.out.print(message);
            System.out.flush();
        });

        for (int i = 0; i < 2; i++) {
            EngineConfig config = i == 0 ? config1 : config2;
            EnginePoolPlayerProvider playerProvider = null;
            try {
                playerProvider = new EnginePoolPlayerProvider(config, poolSize);
            }
            catch (Exception e) {
                System.out.println("Could not create engine pool");
                return;
            }
            game.registerPlayerProvider(playerProvider, i == 0);
        }
    }
}
