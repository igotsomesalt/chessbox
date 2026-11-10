
package chessbox.cli.parse;

import java.util.*;

import chessbox.cli.token.Token;

public class CommandParser {

    public static ParsedArgs parse(List<Token> tokens, List<PositionalSpec> positionalSpecs, List<OptionSpec> optionSpecs) {
        Map<String, Token> positionals = new HashMap<>();
        Map<String, Token> options = new HashMap<>();
        Set<String> flags = new HashSet<>();

        Map<String, OptionSpec> lookup = new HashMap<>();

        for (OptionSpec spec : optionSpecs) {
            lookup.put("--" + spec.name(), spec);

            if (spec.alias() != null) {
                lookup.put("-" + spec.alias(), spec);
            }
        }

        boolean processingPositionals = true;
        int positionalIndex = 0;

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            String value = token.lexeme();

            if (processingPositionals && (token.type() == Token.Type.WORD || token.type() == Token.Type.NUMBER)) {

                if (positionalIndex >= positionalSpecs.size()) {
                    throw new UnexpectedPositionalArgumentException(value);
                }

                PositionalSpec spec = positionalSpecs.get(positionalIndex);

                if (token.type() != spec.type()) {
                    throw new InvalidPositionalTypeException(
                        value, spec.name(), spec.type().toString().toLowerCase()
                    );
                }

                positionals.put(spec.name(), token);
                positionalIndex++;
                continue;
            }

            processingPositionals = false;


            // Option
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
                        next.type().toString(),
                        spec.expectedType().toString(),
                        value
                    );
                }

                options.put(spec.name(), next);
                i++;
            } else {
                flags.add(spec.name());
            }
        }

        // Apply defaults and check required options
        for (OptionSpec spec : optionSpecs) {
            if (options.containsKey(spec.name()) ||
                flags.contains(spec.name())) {
                continue;
            }

            if (spec.defaultValue() != null) {
                options.put(spec.name(), spec.defaultValue());
            } else if (spec.required()) {
                throw new MissingOptionException(spec.name());
            }
        }

        // Check required positionals
        for (PositionalSpec spec : positionalSpecs) {
            if (spec.required() && !positionals.containsKey(spec.name())) {
                throw new MissingPositionalException(spec.name());
            }
        }

        return new ParsedArgs(
            Map.copyOf(positionals),
            Map.copyOf(options),
            Set.copyOf(flags)
        );
    }
}
