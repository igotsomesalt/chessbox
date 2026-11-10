package chessbox.cli.parse;

public class MissingValueException extends RuntimeException {
    public MissingValueException(String option) {
        super("Missing value for '%s'");
    }
}
