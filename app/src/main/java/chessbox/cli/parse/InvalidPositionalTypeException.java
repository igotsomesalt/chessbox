package chessbox.cli.parse;

public final class InvalidPositionalTypeException extends ParseException {
    public InvalidPositionalTypeException(String positionalType, String expectedType, String option) {
        super(String.format(
            "Invalid positional type '%s' for %s, when %s was expected", positionalType, option, expectedType
        ));
    }
}