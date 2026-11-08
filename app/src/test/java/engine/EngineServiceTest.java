
package engine;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class EngineServiceTest {

    private EngineService engineService;
    private EngineConfig config;

    @BeforeEach
    void setUp() {
        engineService = new EngineService(new EngineRegistry());
        config = new EngineConfig(
            "0",
            "Stockfish",
            Path.of("../engines/stockfish.exe")
        );
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
            engineService.listEngines().stream()
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

        assertEquals(2, engineService.listEngines().size());
    }

    // UNREGISTRATION

    @Test
    void unregisterEngine() {
        engineService.register(config);
        engineService.unregister(config.id());

        assertTrue(engineService.listEngines().isEmpty());
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
    void listEnginesInitiallyEmpty() {
        assertTrue(engineService.listEngines().isEmpty());
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
    void createDuplicatePool() throws IOException {
        engineService.register(config);
        engineService.createPool(config.id(), 2);

        assertThrows(
            IllegalArgumentException.class,
            () -> engineService.createPool(config.id(), 2)
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
        engineService.createPool(config.id(), 2);

        engineService.destroyPool(config.id());

        assertThrows(
            NoSuchElementException.class,
            () -> engineService.acquire(config.id())
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
        engineService.createPool(config.id(), 2);

        engineService.destroyPool(config.id());

        assertThrows(
            NoSuchElementException.class,
            () -> engineService.destroyPool(config.id())
        );
    }

    // ENGINE ACQUISITION

    @Test
    void acquireEngine() throws IOException, InterruptedException {
        engineService.register(config);
        engineService.createPool(config.id(), 2);

        UciEngine engine = engineService.acquire(config.id());

        assertNotNull(engine);

        engineService.release(config.id(), engine);
    }

    @Test
    void acquireMultipleEngines() throws IOException, InterruptedException {
        engineService.register(config);
        engineService.createPool(config.id(), 2);

        UciEngine first = engineService.acquire(config.id());
        UciEngine second = engineService.acquire(config.id());

        assertNotNull(first);
        assertNotNull(second);
        assertNotSame(first, second);

        engineService.release(config.id(), first);
        engineService.release(config.id(), second);
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
        engineService.createPool(config.id(), 1);

        UciEngine first = engineService.acquire(config.id());

        engineService.release(config.id(), first);

        UciEngine second = engineService.acquire(config.id());

        assertSame(first, second);

        engineService.release(config.id(), second);
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
        engineService.createPool(config.id(), 1);

        engineService.destroyPool(config.id());

        assertDoesNotThrow(
            () -> engineService.createPool(config.id(), 1)
        );
    }

    @Test
    void closeWithoutPools() {
        assertDoesNotThrow(() -> engineService.close());
    }

    @Test
    void closeWithMultiplePools() throws IOException {
        EngineConfig second = new EngineConfig(
            "1",
            "Stockfish 2",
            Path.of("../engines/stockfish.exe")
        );

        engineService.register(config);
        engineService.register(second);

        engineService.createPool(config.id(), 1);
        engineService.createPool(second.id(), 1);

        assertDoesNotThrow(() -> engineService.close());
    }
}
