package chessbox.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class EngineRegistry {
    Map<String, EngineConfig> configs;

    public EngineRegistry() {
        configs = new HashMap<>();
    }

    public void register(EngineConfig config) throws IllegalArgumentException {
        if (configs.putIfAbsent(config.id(), config) != null) {
            throw new IllegalArgumentException(
                "Engine ID already registered: " + config.id()
            );
        }
    }

    public void unregister(String id) {
        if (configs.remove(id) == null) {
            throw new NoSuchElementException(
                "No engine registered with ID: " + id
            );
        }
    }

    public EngineConfig getConfig(String id) throws NoSuchElementException {
        EngineConfig config = configs.get(id);
        if (config == null) throw new NoSuchElementException("No engine registered with ID: " + id);
        return config;
    }

    public Collection<EngineConfig> getConfigs() {
        return configs.values();
    }
}
