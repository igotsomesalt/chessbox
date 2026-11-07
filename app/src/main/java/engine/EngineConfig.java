package engine;

import java.nio.file.Path;
import java.util.Map;

public record EngineConfig(String id, String name, Path executable, Map<String, String> options) {
    public EngineConfig {
        if (id == null) {
            throw new IllegalArgumentException("Engine ID is required");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Engine name is required");
        }

        if (executable == null) {
            throw new IllegalArgumentException("Executable path is required");
        }

        options = options == null ? Map.of() : Map.copyOf(options);
    }

    public EngineConfig(String id, String name, Path executable) {
        this(id, name, executable, Map.of());
    }
}
