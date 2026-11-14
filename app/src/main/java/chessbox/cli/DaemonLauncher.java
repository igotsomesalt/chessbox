package chessbox.cli;

import java.io.IOException;

import chessbox.util.AppPaths;

public class DaemonLauncher {

    public static void start() throws IOException {
        String java = ProcessHandle.current()
            .info()
            .command()
            .orElseThrow();

        new ProcessBuilder(
            java,
            "-Dchessbox.home=" + AppPaths.installationDir(),
            "-cp",
            System.getProperty("java.class.path"),
            "chessbox.daemon.DaemonMain"
        )
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start();
    }
}
