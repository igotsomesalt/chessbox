package chessbox.cli.parse;

public final class MissingPositionalException extends ParseException {
    public MissingPositionalException(String positional) {
        super(String.format("Missing required argument <%s>", positional));
    }
}
