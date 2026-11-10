package chessbox.util;


import java.net.URISyntaxException;
import java.nio.file.Path;

public final class AppPaths {

    private AppPaths() {}

    public static Path installationDir() {
        String override = System.getProperty("chessbox.home");

        if (override != null && !override.isBlank()) {
            return Path.of(override).toAbsolutePath().normalize();
        }

        try {
            Path location = Path.of(
                AppPaths.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
            );

            if (location.toString().endsWith(".jar")) {
                return location.getParent();
            }

            return Path.of(System.getProperty("user.dir"))
                .toAbsolutePath()
                .normalize();

        } catch (URISyntaxException e) {
            throw new IllegalStateException(
                "Unable to determine installation directory", e
            );
        }
    }
}
