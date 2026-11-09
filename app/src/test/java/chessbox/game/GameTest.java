package chessbox.game;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import chessbox.engine.EngineConfig;
import chessbox.engine.EngineConfigLoader;
import chessbox.engine.EngineRegistry;
import chessbox.engine.EngineService;
import chessbox.opening.FenOpeningBook;
import chessbox.opening.OpeningBook;

public class GameTest {

    @Test
    void playe10games4threadsEnvVsStockfish() throws Exception {
        GameService game = new GameService();
        EngineService engineService = new EngineService(new EngineRegistry());
        OpeningBook openingBook = getOpeningBook("/openings/4mvs_+90_+99.epd");
        EngineConfigLoader loader = new EngineConfigLoader();

        Path baseDir = Path.of(
            System.getProperty("user.dir")
        ).toAbsolutePath().normalize();

        List<EngineConfig> defaultConfigs = loader.loadDefaults(baseDir);

        List<EngineConfig> userConfigs = loader.loadUserConfigs(
            Path.of(System.getenv("ENGINE_USER_CONFIG_PATH")), baseDir
        );

        EngineConfig ec1 = defaultConfigs.get(0);
        EngineConfig ec2 = userConfigs.get(0);
        register2engines(ec1, ec2, game, engineService, 4);
        
        game.start(10, 4, openingBook, false);
    }

    @Test 
    void play10games4threadsVerboseFalse() throws Exception  {
        GameService game = new GameService();
        EngineService engineService = new EngineService(new EngineRegistry());
        OpeningBook openingBook = getOpeningBook("/openings/4mvs_+90_+99.epd");
        register2stockfishs(game, engineService, 4);
        game.start(10, 4, openingBook, false);
    }

    @Test
    void play10games4threadVerboseTrue() throws Exception  {
        GameService game = new GameService();
        EngineService engineService = new EngineService(new EngineRegistry());
        OpeningBook openingBook = getOpeningBook("/openings/4mvs_+90_+99.epd");
        register2stockfishs(game, engineService, 4);
        game.start(10, 4, openingBook, true);
    }

    @Test
    void play10games1threadVerboseFalse() throws Exception  {
        GameService game = new GameService();
        EngineService engineService = new EngineService(new EngineRegistry());
        OpeningBook openingBook = getOpeningBook("/openings/4mvs_+90_+99.epd");
        register2stockfishs(game, engineService, 1);
        game.start(10, 1, openingBook, false);
    }

    @Test 
    void play100games4threadsVerboseFalse() throws Exception {
        GameService game = new GameService();
        EngineService engineService = new EngineService(new EngineRegistry());
        OpeningBook openingBook = getOpeningBook("/openings/4mvs_+90_+99.epd");
        register2stockfishs(game, engineService, 4);
        game.start(100, 4, openingBook, false);
    }

    @Test 
    void play100games1threadsVerboseFalse() throws Exception {
        GameService game = new GameService();
        EngineService engineService = new EngineService(new EngineRegistry());
        OpeningBook openingBook = getOpeningBook("/openings/4mvs_+90_+99.epd");
        register2stockfishs(game, engineService, 1);
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

    private void register2stockfishs(GameService game, EngineService engineService, int poolSize) throws Exception {
        EngineConfig config1 = new EngineConfig("0", "Stockfish", Path.of("../engines/stockfish.exe"));
        EngineConfig config2 = new EngineConfig("1", "Stockfish", Path.of("../engines/stockfish.exe"));
        register2engines(config1, config2, game, engineService, poolSize);

        game.registerLogListener(message -> {
            System.out.print(message);
            System.out.flush();
        });

        engineService.register(config1);
        engineService.register(config2);
        engineService.createPool(config1.id(), poolSize);
        engineService.createPool(config2.id(), poolSize);
        PlayerProvider playerProvider1 = new EnginePoolPlayerProvider(config1.id(), engineService);
        PlayerProvider playerProvider2 = new EnginePoolPlayerProvider(config2.id(), engineService);
        game.registerPlayerProvider(playerProvider1, true);
        game.registerPlayerProvider(playerProvider2, false);
    }

    private void register2engines(
        EngineConfig ec1, EngineConfig ec2, GameService game, EngineService engineService, int poolSize
    ) {
        game.registerLogListener(message -> {
            System.out.print(message);
            System.out.flush();
        });

        for (int i = 0; i < 2; i++) {
            EngineConfig config = i == 0 ? ec1 : ec2;
            EnginePoolPlayerProvider playerProvider = null;
            try {
                engineService.register(config);
                engineService.createPool(config.id(), poolSize);
                playerProvider = new EnginePoolPlayerProvider(config.id(), engineService);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
                return;
            }
            game.registerPlayerProvider(playerProvider, i == 0);
        }
    }
}
