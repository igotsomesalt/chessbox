package chessbox.cli.parse;

public class DuplicateOptionException extends RuntimeException {
    public DuplicateOptionException(String option) {
        super(String.format("Duplicate option '%s' was found", option));
    }
}
