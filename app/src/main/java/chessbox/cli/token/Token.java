package chessbox.cli.token;

public record Token(Type type, String lexeme) {

    public static enum Type {
        WORD, NUMBER, OPTION, ENDLINE, UNKNOWN
    }
}
