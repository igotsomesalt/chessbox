package chessbox.cli.parse;

public final class DuplicateOptionException extends ParseException {
    public DuplicateOptionException(String option) {
        super(String.format("Duplicate option '%s' was found", option));
    }
}
