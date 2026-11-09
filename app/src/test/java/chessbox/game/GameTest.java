package chessbox.game;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chessbox.cli.listener.GameEventPrinter;
import chessbox.engine.EngineConfig;
import chessbox.engine.EngineConfigLoader;
import chessbox.engine.EngineRegistry;
import chessbox.engine.EngineService;
import chessbox.opening.FenOpeningBook;
import chessbox.opening.OpeningBook;

public class GameTest {

    private GameService game;
    private EngineService engineService;
    private OpeningBook openingBook;
    private EngineConfigLoader loader;

    @BeforeEach
    void setUp() {
        engineService = new EngineService(new EngineRegistry());
        openingBook = getOpeningBook("/openings/4mvs_+90_+99.epd");
        loader = new EngineConfigLoader();
        game = new GameService();
        game.addListener(new GameEventPrinter(System.out, System.err));
    }

    @AfterEach
    void tearDown() throws IOException {
        game = new GameService();        
        engineService.close();
    }

    @Test
    void playe10games4threadsEnvVsStockfish() throws Exception {

        Path baseDir = Path.of(
            System.getProperty("user.dir")
        ).toAbsolutePath().normalize();

        List<EngineConfig> defaultConfigs = loader.loadDefaults(baseDir);

        List<EngineConfig> userConfigs = loader.loadUserConfigs(
            Path.of(System.getenv("ENGINE_USER_CONFIG_PATH")), baseDir
        );

        EngineConfig ec1 = defaultConfigs.get(0);
        EngineConfig ec2 = userConfigs.get(0);
        assertDoesNotThrow(() -> register2engines(ec1, ec2, game, engineService, 4));
        assertDoesNotThrow(() -> game.start(10, 4, openingBook, false));
    }

    @Test 
    void play10games4threadsVerboseFalse() throws Exception  {
        assertDoesNotThrow(() -> register2stockfishs(game, engineService, 4));
        assertDoesNotThrow(() -> game.start(10, 4, openingBook, false));
    }

    @Test
    void play10games4threadVerboseTrue() throws Exception  {
        assertDoesNotThrow(() -> register2stockfishs(game, engineService, 4));
        assertDoesNotThrow(() -> game.start(10, 4, openingBook, true));
    }

    @Test
    void play10games1threadVerboseFalse() throws Exception  {
        assertDoesNotThrow(() -> register2stockfishs(game, engineService, 1));
        assertDoesNotThrow(() -> game.start(10, 1, openingBook, false));
    }

    @Test 
    void play100games4threadsVerboseFalse() throws Exception {
        assertDoesNotThrow(() -> register2stockfishs(game, engineService, 4));
        assertDoesNotThrow(() -> game.start(100, 4, openingBook, false));
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
        
        Path appDir = Path.of(
            System.getProperty("user.dir")
        ).toAbsolutePath().normalize();

        List<EngineConfig> configs = loader.loadDefaults(appDir);
        EngineConfig config = configs.get(0);

        engineService.register(config);
        UUID id1 = engineService.createPool(config.id(), poolSize);
        UUID id2 = engineService.createPool(config.id(), poolSize);
        PlayerProvider playerProvider1 = new EnginePoolPlayerProvider(id1, engineService);
        PlayerProvider playerProvider2 = new EnginePoolPlayerProvider(id2, engineService);
        game.registerPlayerProvider(playerProvider1, true);
        game.registerPlayerProvider(playerProvider2, false);
    }

    private void register2engines(
        EngineConfig ec1, EngineConfig ec2, GameService game, EngineService engineService, int poolSize
    ) {

        for (int i = 0; i < 2; i++) {
            EngineConfig config = i == 0 ? ec1 : ec2;
            EnginePoolPlayerProvider playerProvider = null;
            try {
                engineService.register(config);
                UUID id = engineService.createPool(config.id(), poolSize);
                playerProvider = new EnginePoolPlayerProvider(id, engineService);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
                return;
            }
            game.registerPlayerProvider(playerProvider, i == 0);
        }
    }
}
