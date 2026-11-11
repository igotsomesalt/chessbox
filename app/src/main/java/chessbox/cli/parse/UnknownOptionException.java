package chessbox.cli.parse;

public final class UnknownOptionException extends ParseException {
    public UnknownOptionException(String option) {
        super(String.format("Unknown option '%s'", option));
    }
}
