package chessbox.cli.parse;

public class UnexpectedPositionalArgumentException extends  RuntimeException {
    public UnexpectedPositionalArgumentException(String positional) {
        super(String.format("Unexpected positional argument '%s'", positional));
    }
}
