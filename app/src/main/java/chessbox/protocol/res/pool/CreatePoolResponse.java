package chessbox.protocol.res.pool;

import java.util.UUID;

import chessbox.protocol.res.DaemonResponse;

public record CreatePoolResponse(
    UUID id, String engine, int threads, int available
) implements DaemonResponse {}