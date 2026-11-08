package game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import engine.EngineConfig;
import engine.EngineConfigLoader;

public class EngineConfigTest {

    @Test
    void loadDefaultStockfishEngineConfig() throws IOException {
        EngineConfigLoader loader = new EngineConfigLoader();

        Path applicationDir = Path.of(
            System.getProperty("user.dir")
        );

        List<EngineConfig> configs = loader.loadDefaults(applicationDir);

        assertFalse(configs.isEmpty());

        EngineConfig stockfish = configs.stream()
            .filter(c -> c.name().equals("Stockfish"))
            .findFirst()
            .orElseThrow();

        assertTrue(stockfish.path().isAbsolute());
    }
}
