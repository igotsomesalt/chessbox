package chessbox.cli.parse;

public class InvalidOptionValueException extends RuntimeException {
    public InvalidOptionValueException(String optionType, String expectedType, String option) {
        super(String.format(
            "Invalid option type '%s' for %s, when %s was expected", optionType, option, expectedType
        ));
    }
}
