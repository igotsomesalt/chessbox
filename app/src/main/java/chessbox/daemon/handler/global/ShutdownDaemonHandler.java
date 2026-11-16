package chessbox.daemon.handler.global;

import chessbox.daemon.DaemonMain;
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
            ProcessHandle.current().pid(),
            DaemonMain.ctx.startedAt().toString(),
            DaemonMain.ctx.uptime().toSeconds(),
            DaemonMain.ctx.engines().listEnginePools().size(),
            DaemonMain.ctx.games().activeMatches()
        );
    }
}
