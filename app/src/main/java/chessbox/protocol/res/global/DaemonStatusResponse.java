package chessbox.protocol.res;

import chessbox.protocol.DaemonStatus;
import chessbox.protocol.res.global.DaemonResponse;

public record DaemonStatusResponse(DaemonStatus status, String message) implements DaemonResponse {

} 
