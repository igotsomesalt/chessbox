package chessbox.cli.parse;

public final class InvalidOptionValueException extends ParseException {
    public InvalidOptionValueException(String optionType, String expectedType, String option) {
        super(String.format(
            "Invalid option type '%s' for %s, when %s was expected", optionType, option, expectedType
        ));
    }
}
