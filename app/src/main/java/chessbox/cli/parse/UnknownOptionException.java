package chessbox.cli.parse;

public class UnknownOptionException extends RuntimeException {
    public UnknownOptionException(String option) {
        super(String.format("Unknown option '%s'", option));
    }
}
