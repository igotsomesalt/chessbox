
package chessbox.engine;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class EngineServiceTest {

    private EngineService engineService;
    private EngineConfig config;

    @BeforeEach
    void setUp() throws IOException {
        engineService = new EngineService(new EngineRegistry(), new EnginePoolRegistry());

        Path appDir = Path.of(
            System.getProperty("user.dir")
        ).toAbsolutePath().normalize();

        config = (new EngineConfigLoader()).loadDefaults(appDir).get(0);
    }

    @AfterEach
    void tearDown() throws IOException {
        engineService.close();
    }

    // REGISTRATION

    @Test
    void registerEngine() {
        engineService.register(config);

        assertTrue(
            engineService.listEngineConfigs().stream()
                .anyMatch(e -> config.id().equals(e.id()))
        );
    }

    @Test
    void registerDuplicateEngine() {
        engineService.register(config);

        assertThrows(
            IllegalArgumentException.class,
            () -> engineService.register(config)
        );
    }

    @Test
    void registerMultipleEngines() {
        EngineConfig second = new EngineConfig(
            "1",
            "Stockfish 2",
            Path.of("../engines/stockfish.exe")
        );

        engineService.register(config);
        engineService.register(second);

        assertEquals(2, engineService.listEngineConfigs().size());
    }

    // UNREGISTRATION

    @Test
    void unregisterEngine() {
        engineService.register(config);
        engineService.unregister(config.id());

        assertTrue(engineService.listEngineConfigs().isEmpty());
    }

    @Test
    void unregisterNonexistentEngine() {
        assertThrows(
            NoSuchElementException.class,
            () -> engineService.unregister("missing")
        );
    }

    // CONFIGURATION LOOKUP

    @Test
    void getConfig() {
        engineService.register(config);

        EngineConfig result = engineService.getConfig(config.id());

        assertEquals(config.id(), result.id());
        assertEquals(config.name(), result.name());
        assertEquals(config.path(), result.path());
    }

    @Test
    void getNonexistentConfig() {
        assertThrows(
            NoSuchElementException.class,
            () -> engineService.getConfig("missing")
        );
    }

    @Test
    void listEngineConfigsInitiallyEmpty() {
        assertTrue(engineService.listEngineConfigs().isEmpty());
    }

    // POOL CREATION

    @Test
    void createPool() throws IOException {
        engineService.register(config);

        assertDoesNotThrow(
            () -> engineService.createPool(config.id(), 2)
        );
    }

    @Test
    void createPoolForNonexistentEngine() {
        assertThrows(
            NoSuchElementException.class,
            () -> engineService.createPool("missing", 2)
        );
    }

    @Test
    void createPoolWithInvalidSize() {
        engineService.register(config);

        assertThrows(
            IllegalArgumentException.class,
            () -> engineService.createPool(config.id(), 0)
        );
    }

    // POOL DESTRUCTION

    @Test
    void destroyPool() throws IOException {
        engineService.register(config);
        UUID uuid = engineService.createPool(config.id(), 2);

        engineService.destroyPool(uuid);

        assertThrows(
            NoSuchElementException.class,
            () -> engineService.acquire(uuid)
        );
    }

    @Test
    void destroyNonexistentPool() {
        assertThrows(
            NoSuchElementException.class,
            () -> engineService.destroyPool("missing")
        );
    }

    @Test
    void destroyPoolTwice() throws IOException {
        engineService.register(config);
        UUID uuid = engineService.createPool(config.id(), 2);

        engineService.destroyPool(uuid);

        assertThrows(
            NoSuchElementException.class,
            () -> engineService.destroyPool(uuid)
        );
    }

    // ENGINE ACQUISITION

    @Test
    void acquireEngine() throws IOException, InterruptedException {
        engineService.register(config);
        UUID uuid = engineService.createPool(config.id(), 2);

        UciEngine engine = engineService.acquire(uuid);

        assertNotNull(engine);

        engineService.release(uuid, engine);
    }

    @Test
    void acquireMultipleEngines() throws IOException, InterruptedException {
        engineService.register(config);
        UUID uuid = engineService.createPool(config.id(), 2);

        UciEngine first = engineService.acquire(uuid);
        UciEngine second = engineService.acquire(uuid);

        assertNotNull(first);
        assertNotNull(second);
        assertNotSame(first, second);

        engineService.release(uuid, first);
        engineService.release(uuid, second);
    }

    @Test
    void acquireFromNonexistentPool() {
        assertThrows(
            NoSuchElementException.class,
            () -> engineService.acquire("missing")
        );
    }

    @Test
    void releaseAndReacquireEngine() throws IOException, InterruptedException {
        engineService.register(config);
        UUID uuid = engineService.createPool(config.id(), 1);

        UciEngine first = engineService.acquire(uuid);

        engineService.release(uuid, first);

        UciEngine second = engineService.acquire(uuid);

        assertSame(first, second);

        engineService.release(uuid, second);
    }

    @Test
    void releaseToNonexistentPool() {
        assertThrows(
            NoSuchElementException.class,
            () -> engineService.release("missing", null)
        );
    }

    // LIFECYCLE

    @Test
    void recreatePoolAfterDestroy() throws IOException {
        engineService.register(config);
        UUID uuid = engineService.createPool(config.id(), 1);

        engineService.destroyPool(uuid);

        assertDoesNotThrow(
            () -> engineService.createPool(config.id(), 1)
        );
    }

    @Test
    void closeWithoutPools() {
        assertDoesNotThrow(() -> engineService.close());
    }

    @Test
    void close() throws IOException {

        engineService.register(config);
        engineService.createPool(config.id(), 1);
        assertDoesNotThrow(() -> engineService.close());
    }
}
