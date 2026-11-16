package chessbox.daemon.handler.global;

import org.jspecify.annotations.NonNull;

import chessbox.daemon.handler.RequestHandler;
import chessbox.protocol.DaemonStatus;
import chessbox.protocol.req.global.ShutdownDaemonRequest;
import chessbox.protocol.res.global.DaemonStatusResponse;

public class ShutdownDaemonHandler implements RequestHandler<ShutdownDaemonRequest, DaemonStatusResponse> {

    @Override
    public Class<ShutdownDaemonRequest> id() {
        return ShutdownDaemonRequest.class;
    }

    @Override
    public DaemonStatusResponse handle(ShutdownDaemonRequest request) {
        return new DaemonStatusResponse(
            DaemonStatus.STOPPING,
            "Daemon shutting down"
        );
    }
}
