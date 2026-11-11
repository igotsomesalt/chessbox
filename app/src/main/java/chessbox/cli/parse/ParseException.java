package chessbox.cli.parse;

public sealed abstract class ParseException extends RuntimeException
    permits DuplicateOptionException, InvalidOptionValueException, InvalidPositionalTypeException,
    MissingLeafCommandException, MissingOptionException, MissingPositionalException, MissingValueException,
    UnexpectedPositionalArgumentException, UnexpectedTokenException, UnknownCommandException, UnknownLeafCommandException,
    UnknownOptionException {

    protected ParseException(String message) {
        super(message);
    }
}
