package chessbox.cli.parse;

import chessbox.cli.token.Token;

public record PositionalSpec(String name, Token.Type type, boolean required) {}
