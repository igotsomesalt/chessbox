package engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

public class EngineConfigTest {

    @Test
    void loadDefaultStockfishEngineConfig() throws IOException {
        EngineConfigLoader loader = new EngineConfigLoader();

        Path applicationDir = Path.of(System.getProperty("user.dir"));

        List<EngineConfig> configs = loader.loadDefaults(applicationDir);

        assertFalse(configs.isEmpty());

        EngineConfig stockfish = configs.stream()
            .filter(c -> c.name().equals("Stockfish"))
            .findFirst()
            .orElseThrow();

        assertTrue(stockfish.path().isAbsolute());
    }

    @Test
    void loadUserConfigs() throws IOException, URISyntaxException {
        EngineConfigLoader loader = new EngineConfigLoader();

        Path configPath = Path.of(
            getClass()
                .getResource("/configs/user-engines.json")
                .toURI()
        );

        Path baseDir = configPath.getParent();

        List<EngineConfig> configs = loader.loadUserConfigs(configPath, baseDir);

        assertEquals(2, configs.size());
        assertEquals("Test Engine", configs.get(0).name());
        assertTrue(configs.get(0).path().isAbsolute());
    }
}
