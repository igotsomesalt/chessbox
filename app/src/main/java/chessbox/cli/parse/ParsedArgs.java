package chessbox.cli.parse;

import java.util.List;
import java.util.Map;
import java.util.Set;

import chessbox.cli.token.Token;

public record ParsedArgs(List<Token> positionals, Map<String, Token> options, Set<String> flags) {
    public Token option(String name) {
        return options.get(name);
    }

    public boolean hasFlag(String name) {
        return flags.contains(name);
    }

    public Token positional(int index) {
        return positionals.get(index);
    }
}