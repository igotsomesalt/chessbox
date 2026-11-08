package chessbox.config;

import java.nio.file.Path;

public final class ConfigPaths {

    private static final Path USER_CONFIG_DIR =
        Path.of(System.getProperty("user.home"), ".chessbox");

    public static Path getEngineConfig() {
        return USER_CONFIG_DIR.resolve("engines.json");
    }
}