package chessbox.cli.parse;

import java.util.Map;
import java.util.Set;

import chessbox.cli.token.Token;

public record ParsedArgs(Map<String, Token> positionals, Map<String, Token> options, Set<String> flags) {
    public Token option(String name) {
        return options.get(name);
    }

    public boolean hasFlag(String name) {
        return flags.contains(name);
    }

    public Token positional(String name) {
        return positionals.get(name);
    }
}