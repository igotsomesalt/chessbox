package chessbox.protocol.res.global;

import chessbox.protocol.DaemonStatus;
import chessbox.protocol.res.DaemonResponse;

public record DaemonStatusResponse( 
    DaemonStatus status, long pid, String startedAt, long uptimeSeconds, int pools, int activeMatches
) implements DaemonResponse {

}
