package chessbox.cli.parse;

public class UnknownLeafCommandException extends RuntimeException {
    public UnknownLeafCommandException(String leaf) {
        super(String.format("Unknown leaf command '%s'", leaf));
    }
}
