package chessbox.daemon;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.google.common.util.concurrent.UncheckedExecutionException;

import chessbox.protocol.DaemonStatus;
import chessbox.util.AppPaths;

public class DaemonMain {
    public static final DaemonContext ctx;
    private static final Logger logger = Logger.getLogger(DaemonMain.class.getName());

    static {
        try {
            ctx = new DaemonContext(AppPaths.installationDir());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void main(String[] args) throws Exception {
        try {

            FileHandler fileHandler = new FileHandler("application.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            logger.info("chessbox.home = " +System.getProperty("chessbox.home"));

            logger.info("user.dir = " + System.getProperty("user.dir"));

            logger.info("installationDir = " + AppPaths.installationDir());
            
        } catch (IOException e) {
            logger.severe("Failed to initialize file logger: " + e.getMessage());
        }

        try (DaemonContext ctx = new DaemonContext(AppPaths.installationDir());
            DaemonServer daemon = new DaemonServer(7878, ctx.requests(), ctx.requestHandlers())) {

            ctx.onShutdown(() -> {
                ctx.setStatus(DaemonStatus.STOPPING);

                try {
                    ctx.close();
                    daemon.stop();
                } 
                catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                catch (Exception e) {
                    throw new UncheckedExecutionException(e);
                }
            });

            ctx.setStatus(DaemonStatus.RUNNING);
            daemon.start();
        }
    }
}
