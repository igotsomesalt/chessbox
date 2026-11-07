package engine;

import java.util.HashMap;
import java.util.Map;

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

    public EngineConfig getConfig(String name) {
        return configs.get(name);
    }
}
