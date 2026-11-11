package chessbox.cli.parse;

public final class UnexpectedTokenException extends ParseException {
    public UnexpectedTokenException(String lexeme) {
        super(String.format("Unexpected token '%s'", lexeme));
    }
}
