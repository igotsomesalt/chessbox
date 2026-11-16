package chessbox.protocol.req.global;

import chessbox.protocol.req.DaemonRequest;

public record DaemonStatusRequest() implements DaemonRequest {

	@Override
	public String id() {
		return "daemon.status";
	}

}
