package chessbox.daemon.handler.global;

import org.jspecify.annotations.NonNull;

import chessbox.daemon.handler.RequestHandler;
import chessbox.protocol.DaemonStatus;
import chessbox.protocol.req.global.ShutdownDaemonRequest;
import chessbox.protocol.res.DaemonStatusResponse;

public class ShutdownDaemonHandler implements RequestHandler<@NonNull ShutdownDaemonRequest, DaemonStatusResponse> {

    private final Runnable shutdown;

    @Override
    public Class<@NonNull ShutdownDaemonRequest> id() {
        return ShutdownDaemonRequest.class;
    }

    public ShutdownDaemonHandler(Runnable shutdown) {
        this.shutdown = shutdown;
    }

    @Override
    public DaemonStatusResponse handle(@NonNull ShutdownDaemonRequest request) throws Exception {
        DaemonStatusResponse response = null;

        try {
            shutdown.run();
            response = new DaemonStatusResponse(DaemonStatus.STOPPING, "Daemon shutdown");
        }
        catch(Exception e) {
            response = new DaemonStatusResponse(DaemonStatus.RUNNING, "Daemon did not shutdown");
        }

        return response;
    }

}
