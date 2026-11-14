package chessbox.daemon;

import chessbox.protocol.res.global.DaemonResponse;

public record ResponseEnvelope<T extends DaemonResponse>(boolean success, T data, DaemonError error) {
    public static <T extends DaemonResponse> ResponseEnvelope<T> ok(T data) {
        return new ResponseEnvelope<>(true, data, null);
    }

    public static ResponseEnvelope<DaemonResponse> fail(String code, String message) {
        return new ResponseEnvelope<>(false, null, new DaemonError(code, message));
    }
}
