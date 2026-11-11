package chessbox.cli.parse;

public final class UnknownLeafCommandException extends ParseException {
    public UnknownLeafCommandException(String leaf) {
        super(String.format("Unknown leaf command '%s'", leaf));
    }
}
