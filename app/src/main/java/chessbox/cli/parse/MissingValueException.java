package chessbox.cli.parse;

public final class MissingValueException extends ParseException {
    public MissingValueException(String option) {
        super("Missing value for '%s'");
    }
}
