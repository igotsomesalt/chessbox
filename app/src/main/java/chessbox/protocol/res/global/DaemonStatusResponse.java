package chessbox.protocol.res.global;

import chessbox.protocol.DaemonStatus;
import chessbox.protocol.res.DaemonResponse;

public record DaemonStatusResponse(DaemonStatus status, String message) implements DaemonResponse {

} 
