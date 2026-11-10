package chessbox.cli.parse;

import java.util.*;

import chessbox.cli.token.Token;

public class CommandParser {

    public ParsedArgs parse(List<Token> tokens, List<OptionSpec> specs) {
        List<Token> positionals = new ArrayList<>();
        Map<String, Token> options = new HashMap<>();
        Set<String> flags = new HashSet<>();

        Map<String, OptionSpec> lookup = new HashMap<>();

        for (OptionSpec spec : specs) {
            lookup.put("--" + spec.name(), spec);

            if (spec.alias() != null) {
                lookup.put("-" + spec.alias(), spec);
            }
        }

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            String value = token.lexeme();

            if (token.type() == Token.Type.WORD || token.type() == Token.Type.NUMBER) {
                positionals.add(token);
                continue;
            }

            if (token.type() != Token.Type.OPTION) {
                throw new UnexpectedTokenException(value);
            }

            OptionSpec spec = lookup.get(value);

            if (spec == null) {
                throw new UnknownOptionException(value);
            }

            if (options.containsKey(spec.name()) ||
                flags.contains(spec.name())) {
                throw new DuplicateOptionException(value);
            }

            if (spec.takesValue()) {
                if (i + 1 >= tokens.size()) {
                    throw new MissingValueException(value);
                }

                Token next = tokens.get(i + 1);

                if (next.type() != spec.expectedType()) {
                    throw new InvalidOptionValueException(
                        next.type().toString(), spec.expectedType().toString(), value
                    );
                }

                options.put(spec.name(), next);
                i++;

            } else {
                flags.add(spec.name());
            }
        }

        // Check required options
        for (OptionSpec spec : specs) {
            if (spec.required() && !options.containsKey(spec.name()) & !flags.contains(spec.name())) {
                throw new MissingOptionException(spec.name());
            }
        }

        return new ParsedArgs(
            List.copyOf(positionals),
            Map.copyOf(options),
            Set.copyOf(flags)
        );
    }
}