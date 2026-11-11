package chessbox.cli.parse;

public final class UnknownCommandException extends ParseException {
    public UnknownCommandException(String command) {
        super(String.format("Unknown command '%s'", command));
    }
}
