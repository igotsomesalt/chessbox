package engine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EngineConfigLoader {

    private final ObjectMapper mapper = new ObjectMapper();

    private record ConfigFile(List<EngineEntry> engines) {}

    private record EngineEntry(String id, String name, Map<String, String> paths) {}

    public List<EngineConfig> loadDefaults(Path applicationDir) throws IOException {

        try (InputStream in = getClass().getResourceAsStream(
            "/configs/default-engines.json"
        )) {
            if (in == null) {
                throw new IOException(
                    "Default engine configurations not found"
                );
            }

            ConfigFile file = mapper.readValue(
                in, ConfigFile.class
            );

            return convert(file, applicationDir);
        }
    }

    public List<EngineConfig> loadUserConfigs(
        Path configPath
    ) throws IOException {

        if (Files.notExists(configPath)) {
            return List.of();
        }

        try (InputStream in = Files.newInputStream(configPath)) {
            ConfigFile file = mapper.readValue(
                in, ConfigFile.class
            );

            return convert(file, configPath.toAbsolutePath().getParent());
        }
    }

    private List<EngineConfig> convert(ConfigFile file, Path baseDir) throws IOException {

        if (file == null || file.engines() == null) {
            throw new IOException("Missing engines array");
        }

        String os = System.getProperty("os.name").toLowerCase();

        String platform = os.contains("win") ? "windows"
            : os.contains("mac") ? "mac"
            : "linux";

        return file.engines().stream()
            .map(entry -> {
                String relativePath = entry.paths() == null
                    ? null
                    : entry.paths().get(platform);

                if (relativePath == null || relativePath.isBlank()) {
                    throw new IllegalArgumentException(
                        "No executable path for engine "
                        + entry.name() + " on " + platform
                    );
                }

                Path executable = baseDir.resolve(relativePath)
                    .toAbsolutePath()
                    .normalize();

                return new EngineConfig(
                    entry.id(),
                    entry.name(),
                    executable
                );
            })
            .toList();
    }
}