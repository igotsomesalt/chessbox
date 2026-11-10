package chessbox.cli.parse;

public class InvalidPositionalTypeException extends RuntimeException {
    public InvalidPositionalTypeException(String positionalType, String expectedType, String option) {
        super(String.format(
            "Invalid positional type '%s' for %s, when %s was expected", positionalType, option, expectedType
        ));
    }
}