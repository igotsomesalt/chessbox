package chessbox.cli.parse;

import chessbox.cli.token.Token;

public record OptionSpec(String name, String alias, Token.Type expectedType, boolean required) {
    public boolean takesValue() {
        return expectedType != null;
    }
}
