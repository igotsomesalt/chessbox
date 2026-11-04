package engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class EngineRegistry {
    Map<String, EngineConfig> configs;

    public EngineRegistry() {
        configs = new HashMap<>();
    }

    public void register(@SuppressWarnings("unchecked") Entry<String, EngineConfig>... entries) {
        for (Entry<String, EngineConfig> entry : entries) {
            configs.put(entry.getKey(), entry.getValue());
        }
    }

    public EngineConfig getConfig(String name) {
        return configs.get(name);
    }
}
