package chessbox.cli.parse;

public final class MissingValueException extends ParseException {
    public MissingValueException(String option) {
        super(String.format("Missing value for '%s'", option));
    }
}
