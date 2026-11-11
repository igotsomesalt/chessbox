package chessbox.cli.parse;

public final class MissingOptionException extends ParseException {
    public MissingOptionException(String option) {
        super(String.format("Required option '%s' is missing", option));
    }
}
