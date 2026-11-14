package chessbox.protocol.req.pool;

import chessbox.protocol.req.DaemonRequest;

public record ListPoolsRequest() implements DaemonRequest {

    @Override
    public String id() {
        return "pool.list";
    }
    
}
