package chessbox.cli.parse;

public class MissingOptionException extends RuntimeException {
    public MissingOptionException(String option) {
        super(String.format("Required option '%s' is missing"));
    }
}
