package chessbox.cli.parse;

public class MissingPositionalException extends RuntimeException {
    public MissingPositionalException(String positional) {
        super(String.format("Missing required argument <%s>", positional));
    }
}
