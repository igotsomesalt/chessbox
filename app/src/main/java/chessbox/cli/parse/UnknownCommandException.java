package chessbox.cli.parse;

public class UnknownCommandException extends RuntimeException {
    public UnknownCommandException(String command) {
        super(String.format("Unknown command '%s'", command));
    }
}
