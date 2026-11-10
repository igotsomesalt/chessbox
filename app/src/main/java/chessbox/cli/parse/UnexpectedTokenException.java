package chessbox.cli.parse;

public class UnexpectedTokenException extends RuntimeException {
    public UnexpectedTokenException(String lexeme) {
        super(String.format("Unexpected token '%s'", lexeme));
    }
}
