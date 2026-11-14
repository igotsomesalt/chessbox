package chessbox.protocol.res.pool;

import java.util.Collection;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import chessbox.protocol.res.global.DaemonResponse;

public record ListPoolsResponse(Collection<@NonNull Pool> pools) implements DaemonResponse {

    public static record Pool(UUID id, String engineName, int threads, int available) {
    }
}
