package chessbox.cli.parse;

import chessbox.cli.token.Token;

public record OptionSpec(String name, String alias, Token.Type expectedType, boolean required, Token defaultValue) {
    public boolean takesValue() {
        return expectedType != null;
    }
}
