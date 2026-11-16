package chessbox.daemon.handler.global;

import chessbox.daemon.DaemonMain;
import chessbox.daemon.handler.RequestHandler;
import chessbox.protocol.DaemonStatus;
import chessbox.protocol.req.global.DaemonStatusRequest;
import chessbox.protocol.res.global.DaemonStatusResponse;

public record DaemonStatusHandler() implements RequestHandler<DaemonStatusRequest, DaemonStatusResponse> {

	@Override
	public Class<DaemonStatusRequest> id() {
		return DaemonStatusRequest.class;
	}

	@Override
	public DaemonStatusResponse handle(DaemonStatusRequest request) throws Exception {
        return new DaemonStatusResponse(
            DaemonMain.ctx.status(),
            ProcessHandle.current().pid(),
            DaemonMain.ctx.startedAt().toString(),
            DaemonMain.ctx.uptime().toSeconds(),
            DaemonMain.ctx.engines().listEnginePools().size(),
            DaemonMain.ctx.games().activeMatches()
        );
	}

}
