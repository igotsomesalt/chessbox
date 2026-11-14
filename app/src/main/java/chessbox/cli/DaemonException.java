package chessbox.cli;

public class DaemonException extends RuntimeException {
    private final String code;

    public DaemonException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}