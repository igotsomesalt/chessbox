
package chessbox.daemon;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import chessbox.daemon.handler.RequestHandlerRegistry;
import chessbox.daemon.handler.global.ShutdownDaemonHandler;
import chessbox.daemon.handler.pool.CreatePoolHandler;
import chessbox.daemon.handler.pool.ListPoolsHandler;
import chessbox.engine.*;
import chessbox.game.*;
import chessbox.protocol.DaemonStatus;
import chessbox.protocol.req.global.ShutdownDaemonRequest;
import chessbox.protocol.req.pool.CreatePoolRequest;
import chessbox.protocol.req.pool.ListPoolsRequest;

public class DaemonContext implements AutoCloseable {
    private final EngineRegistry engineRegistry;
    private final EnginePoolRegistry enginePoolRegistry;
    private final MatchRegistry matchRegistry;
    private final RequestRegistry requestRegistry;
    private final RequestHandlerRegistry requestHandlerRegistry;

    private final EngineService engineService;
    private final GameService gameService;

    private volatile DaemonStatus status = DaemonStatus.STARTING;
    private final Instant startedAt = Instant.now();


    public DaemonContext(Path appDir) throws Exception {
        this.engineRegistry = new EngineRegistry();
        this.enginePoolRegistry = new EnginePoolRegistry();
        this.matchRegistry = new MatchRegistry();

        this.requestRegistry = new RequestRegistry();
        requestRegistry.register("pool.create", CreatePoolRequest.class);
        requestRegistry.register("pool.list", ListPoolsRequest.class);
        requestRegistry.register("daemon.shutdown", ShutdownDaemonRequest.class);

        this.requestHandlerRegistry = new RequestHandlerRegistry();
        requestHandlerRegistry.register(CreatePoolRequest.class, new CreatePoolHandler());
        requestHandlerRegistry.register(ListPoolsRequest.class, new ListPoolsHandler());

        EngineConfigLoader loader = new EngineConfigLoader(appDir);
        List<EngineConfig> configs = loader.loadDefaults(appDir);

        engineRegistry.registerAll(configs);

        this.engineService = new EngineService(
            engineRegistry,
            enginePoolRegistry
        );

        this.gameService = new GameService(matchRegistry);
    }

    public EngineService engines() {
        return engineService;
    }

    public GameService games() {
        return gameService;
    }

    public RequestRegistry requests() {
        return requestRegistry;
    }

    public RequestHandlerRegistry requestHandlers() {
        return requestHandlerRegistry;
    }

    public void setStatus(DaemonStatus status) {
        this.status = Objects.requireNonNull(status);
    }

    public DaemonStatus status() {
        return status;
    }

    public Duration uptime() {
        return Duration.between(startedAt, Instant.now());
    }

    public void onShutdown(Runnable callback) {
        requestHandlerRegistry.register(
            ShutdownDaemonRequest.class,
            new ShutdownDaemonHandler(callback)
        );
    }

    @Override
    public void close() throws Exception {
        gameService.close();
        engineService.close();
    }
}
