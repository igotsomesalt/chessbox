package chessbox.cli.parse;

public final class MissingLeafCommandException extends ParseException {
    public MissingLeafCommandException(String parent) {
        super(String.format("Missing leaf command for '%s'", parent));
    }
}
