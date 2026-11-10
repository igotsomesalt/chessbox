package chessbox.cli.parse;

public class MissingLeafCommandException extends RuntimeException {
    public MissingLeafCommandException(String parent) {
        super(String.format("Missing leaf command for '%s'", parent));
    }
}
