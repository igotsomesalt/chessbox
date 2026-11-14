package chessbox.protocol.req.global;

import chessbox.protocol.req.DaemonRequest;

public record ShutdownDaemonRequest() implements DaemonRequest {

    @Override
    public String id() {
        return "daemon.shutdown";
    }

}
