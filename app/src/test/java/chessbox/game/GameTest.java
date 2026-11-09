
package chessbox.game;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chessbox.cli.listener.GameEventPrinter;
import chessbox.engine.EngineConfig;
import chessbox.engine.EngineConfigLoader;
import chessbox.engine.EnginePoolRegistry;
import chessbox.engine.EngineRegistry;
import chessbox.engine.EngineService;
import chessbox.opening.FenOpeningBook;
import chessbox.opening.OpeningBook;

public class GameTest {

    private GameService game;
    private EngineService engineService;
    private MatchRegistry matchRegistry;
    private OpeningBook openingBook;
    private EngineConfigLoader loader;

    @BeforeEach
    void setUp() throws IOException {
        engineService = new EngineService(
            new EngineRegistry(),
            new EnginePoolRegistry()
        );

        matchRegistry = new MatchRegistry();
        openingBook = getOpeningBook("/openings/4mvs_+90_+99.epd");
        loader = new EngineConfigLoader();

        game = new GameService(matchRegistry);
        game.addListener(new GameEventPrinter(System.out, System.err));
    }

    @AfterEach
    void tearDown() throws Exception {
        try {
            game.close();
        } finally {
            engineService.close();
        }
    }

    @Test
    void play10games4threadsEnvVsStockfish() throws Exception {
        Path baseDir = getBaseDir();

        List<EngineConfig> defaultConfigs =
            loader.loadDefaults(baseDir);

        String userConfigPath =
            System.getenv("ENGINE_USER_CONFIG_PATH");

        if (userConfigPath == null) {
            throw new IllegalStateException(
                "ENGINE_USER_CONFIG_PATH is not configured"
            );
        }

        List<EngineConfig> userConfigs =
            loader.loadUserConfigs(
                Path.of(userConfigPath),
                baseDir
            );

        UUID matchId = register2engines(
            defaultConfigs.get(0),
            userConfigs.get(0),
            10,
            4
        );

        GameService.Result result = game.submit(matchId, false);
        assertCompletedMatch(result, 10, 4);

        // Verify the same match configuration can be reused.
        GameService.Result second = game.submit(matchId, false);
        assertCompletedMatch(second, 10, 4);
    }

    @Test
    void play10games4threadsVerboseFalse() throws Exception {
        UUID matchId = register2stockfishs(10, 4);

        GameService.Result result = game.submit(matchId, false);

        assertCompletedMatch(result, 10, 4);
    }

    @Test
    void play10games4threadsVerboseTrue() throws Exception {
        UUID matchId = register2stockfishs(10, 4);

        GameService.Result result = game.submit(matchId, true);

        assertCompletedMatch(result, 10, 4);
    }

    @Test
    void play10games1threadVerboseFalse() throws Exception {
        UUID matchId = register2stockfishs(10, 1);

        GameService.Result result = game.submit(matchId, false);

        assertCompletedMatch(result, 10, 1);
    }

    @Test
    void play100games4threadsVerboseFalse() throws Exception {
        UUID matchId = register2stockfishs(100, 4);

        GameService.Result result = game.submit(matchId, false);

        assertCompletedMatch(result, 100, 4);
    }

    @Test
    void submitAsyncReturnsResult() throws Exception {
        UUID matchId = register2stockfishs(10, 4);

        GameService.Result result =
            game.submitAsync(matchId, false)
                .get(60, TimeUnit.SECONDS);

        assertCompletedMatch(result, 10, 4);
    }

    @Test
    void cannotSubmitSameMatchConcurrently() throws Exception {
        UUID matchId = register2stockfishs(100, 1);

        var future = game.submitAsync(matchId, false);

        try {
            assertThrows(
                IllegalStateException.class,
                () -> game.submitAsync(matchId, false)
            );
        } finally {
            // Stop the first match so the test can clean up.
            if (!future.isDone()) {
                game.stop(matchId);
            }
        }
    }

    @Test
    void submitAfterCloseThrows() {
        game.close();

        assertThrows(
            IllegalStateException.class,
            () -> game.submitAsync(UUID.randomUUID(), false)
        );
    }

    private void assertCompletedMatch(GameService.Result result, int expectedGames, int expectedThreads) {
        assertNotNull(result);

        assertAll(
            () -> assertEquals(expectedGames, result.games()),
            () -> assertEquals(expectedThreads, result.threads()),
            () -> assertEquals(MatchStatus.COMPLETED, result.status()),
            () -> assertEquals(0, result.cancelled()),
            () -> assertEquals(
                result.games(),
                result.completed() + result.failed() + result.cancelled()
            ),
            () -> assertEquals(
                result.completed(),
                result.outcomes().values()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum()
            ),
            () -> assertNotNull(result.duration()),
            () -> assertNotNull(result.timestamp()),
            () -> assertNotNull(result.openingBook())
        );
    }

    private UUID register2stockfishs(int games, int threads) throws Exception {

        List<EngineConfig> configs =
            loader.loadDefaults(getBaseDir());

        EngineConfig config = configs.get(0);

        engineService.register(config);

        UUID id1 = engineService.createPool(config.id(), threads);
        UUID id2 = engineService.createPool(config.id(), threads);

        PlayerProvider p1 =
            new EnginePoolPlayerProvider(id1, engineService);

        PlayerProvider p2 =
            new EnginePoolPlayerProvider(id2, engineService);

        return registerMatch(games, threads, p1, p2);
    }

    private UUID register2engines(EngineConfig ec1, EngineConfig ec2, int games, int threads) throws Exception {

        engineService.register(ec1);
        engineService.register(ec2);

        UUID id1 = engineService.createPool(ec1.id(), threads);
        UUID id2 = engineService.createPool(ec2.id(), threads);

        PlayerProvider p1 = new EnginePoolPlayerProvider(id1, engineService);

        PlayerProvider p2 = new EnginePoolPlayerProvider(id2, engineService);

        return registerMatch(games, threads, p1, p2);
    }

    private UUID registerMatch(int games, int threads, PlayerProvider p1, PlayerProvider p2) {
        MatchConfig config = new MatchConfig(UUID.randomUUID(), p1, p2, games, threads, openingBook);
        matchRegistry.register(config);
        return config.id();
    }

    private OpeningBook getOpeningBook(String path) throws IOException {

        var stream = getClass().getResourceAsStream(path);

        if (stream == null) {
            throw new IOException(
                "Opening book not found: " + path
            );
        }

        try (stream) {
            return new FenOpeningBook(path, stream);
        }
    }

    private Path getBaseDir() {
        return Path.of(System.getProperty("user.dir"))
            .toAbsolutePath()
            .normalize();
    }
}
