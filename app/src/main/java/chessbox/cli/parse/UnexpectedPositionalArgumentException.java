package chessbox.cli.parse;

public final class UnexpectedPositionalArgumentException extends ParseException {
    public UnexpectedPositionalArgumentException(String positional) {
        super(String.format("Unexpected positional argument '%s'", positional));
    }
}
