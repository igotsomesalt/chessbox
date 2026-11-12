package chessbox.cli;

import chessbox.daemon.protocol.DaemonError;

public class DaemonException extends RuntimeException {
    private final String code;

    public DaemonException(String code, String message) {
        super(message);
        this.code = code;
    }

    public DaemonException(DaemonError error) {
        this(error.code(), error.message());
    }

    public String code() {
        return code;
    }
}