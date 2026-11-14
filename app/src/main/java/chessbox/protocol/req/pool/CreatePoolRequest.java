package chessbox.protocol.req.pool;

import chessbox.protocol.req.DaemonRequest;

public record CreatePoolRequest(String engineId, int threads) implements DaemonRequest {

    @Override
    public String id() {
        return "pool.create";
    }

}
