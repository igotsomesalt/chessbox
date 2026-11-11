
package chessbox.cli.parse;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import chessbox.cli.token.Token;

class CommandParserTest {

    private Token word(String value) {
        return new Token(Token.Type.WORD, value);
    }

    private Token number(String value) {
        return new Token(Token.Type.NUMBER, value);
    }

    private Token option(String value) {
        return new Token(Token.Type.OPTION, value);
    }

    private PositionalSpec engineSpec() {
        return new PositionalSpec("engine", Token.Type.WORD, true);
    }

    private PositionalSpec opponentSpec() {
        return new PositionalSpec("opponent", Token.Type.WORD, true);
    }

    private OptionSpec sizeSpec() {
        return new OptionSpec("size", "s", Token.Type.NUMBER, false, number("1"));
    }

    private OptionSpec verboseSpec() {
        return new OptionSpec("verbose", "v", null, false, null);
    }

    @Test
    void emptyInput() {
        ParsedArgs args = CommandParser.parse(
            List.of(),
            List.of(),
            List.of()
        );

        assertTrue(args.positionals().isEmpty());
        assertTrue(args.options().isEmpty());
        assertTrue(args.flags().isEmpty());
    }

    @Test
    void parsesSinglePositional() {
        ParsedArgs args = CommandParser.parse(
            List.of(word("stockfish")),
            List.of(engineSpec()),
            List.of()
        );

        assertEquals(1, args.positionals().size());
        assertEquals("stockfish", args.positionals().get("engine").lexeme());
    }

    @Test
    void parsesMultiplePositionals() {
        ParsedArgs args = CommandParser.parse(
            List.of(word("stockfish"), word("leela")),
            List.of(engineSpec(), opponentSpec()),
            List.of()
        );

        assertEquals(2, args.positionals().size());
        assertEquals("stockfish", args.positionals().get("engine").lexeme());
        assertEquals("leela", args.positionals().get("opponent").lexeme());
    }

    @Test
    void parsesLongOption() {
        ParsedArgs args = CommandParser.parse(
            List.of(option("--size"), number("4")),
            List.of(),
            List.of(sizeSpec())
        );

        assertEquals("4", args.options().get("size").lexeme());
        assertTrue(args.flags().isEmpty());
    }

    @Test
    void parsesShortOption() {
        ParsedArgs args = CommandParser.parse(
            List.of(option("-s"), number("4")),
            List.of(),
            List.of(sizeSpec())
        );

        assertEquals("4", args.options().get("size").lexeme());
    }

    @Test
    void parsesFlag() {
        ParsedArgs args = CommandParser.parse(
            List.of(option("--verbose")),
            List.of(),
            List.of(verboseSpec())
        );

        assertTrue(args.flags().contains("verbose"));
        assertTrue(args.options().isEmpty());
    }

    @Test
    void parsesShortFlag() {
        ParsedArgs args = CommandParser.parse(
            List.of(option("-v")),
            List.of(),
            List.of(verboseSpec())
        );

        assertTrue(args.flags().contains("verbose"));
    }

    @Test
    void parsesMixedArguments() {
        ParsedArgs args = CommandParser.parse(
            List.of(
                word("stockfish"),
                option("--size"),
                number("4"),
                option("--verbose")
            ),
            List.of(engineSpec()),
            List.of(sizeSpec(), verboseSpec())
        );

        assertEquals("stockfish", args.positionals().get("engine").lexeme());
        assertEquals("4", args.options().get("size").lexeme());
        assertTrue(args.flags().contains("verbose"));
    }

    @Test
    void parsesOptionsBeforePositionals() {
        assertThrows(UnexpectedTokenException.class, () -> 
            CommandParser.parse(
                List.of(
                    option("--size"),
                    number("4"),
                    word("stockfish")
                ),
                List.of(engineSpec()),
                List.of(sizeSpec())
            )
        );
    }

    @Test
    void appliesDefaultOptionValue() {
        ParsedArgs args = CommandParser.parse(
            List.of(),
            List.of(),
            List.of(sizeSpec())
        );

        assertEquals("1", args.options().get("size").lexeme());
    }

    @Test
    void explicitValueOverridesDefault() {
        ParsedArgs args = CommandParser.parse(
            List.of(option("--size"), number("8")),
            List.of(),
            List.of(sizeSpec())
        );

        assertEquals("8", args.options().get("size").lexeme());
    }

    @Test
    void rejectsUnknownOption() {
        assertThrows(UnknownOptionException.class, () ->
            CommandParser.parse(
                List.of(option("--unknown")),
                List.of(),
                List.of(sizeSpec())
            )
        );
    }

    @Test
    void rejectsMissingOptionValue() {
        assertThrows(MissingValueException.class, () ->
            CommandParser.parse(
                List.of(option("--size")),
                List.of(),
                List.of(sizeSpec())
            )
        );
    }

    @Test
    void rejectsInvalidOptionType() {
        assertThrows(InvalidOptionValueException.class, () ->
            CommandParser.parse(
                List.of(option("--size"), word("abc")),
                List.of(),
                List.of(sizeSpec())
            )
        );
    }

    @Test
    void rejectsDuplicateOption() {
        assertThrows(DuplicateOptionException.class, () ->
            CommandParser.parse(
                List.of(
                    option("--size"),
                    number("4"),
                    option("--size"),
                    number("8")
                ),
                List.of(),
                List.of(sizeSpec())
            )
        );
    }

    @Test
    void rejectsDuplicateOptionThroughAlias() {
        assertThrows(DuplicateOptionException.class, () ->
            CommandParser.parse(
                List.of(
                    option("--size"),
                    number("4"),
                    option("-s"),
                    number("8")
                ),
                List.of(),
                List.of(sizeSpec())
            )
        );
    }

    @Test
    void rejectsMissingRequiredOption() {
        OptionSpec required = new OptionSpec(
            "engine", "e", Token.Type.WORD, true, null
        );

        assertThrows(MissingOptionException.class, () ->
            CommandParser.parse(
                List.of(),
                List.of(),
                List.of(required)
            )
        );
    }

    @Test
    void acceptsRequiredOption() {
        OptionSpec required = new OptionSpec(
            "engine", "e", Token.Type.WORD, true, null
        );

        ParsedArgs args = CommandParser.parse(
            List.of(option("--engine"), word("stockfish")),
            List.of(),
            List.of(required)
        );

        assertEquals("stockfish", args.options().get("engine").lexeme());
    }

    @Test
    void rejectsMissingRequiredPositional() {
        assertThrows(MissingPositionalException.class, () ->
            CommandParser.parse(
                List.of(),
                List.of(engineSpec()),
                List.of()
            )
        );
    }

    @Test
    void acceptsOptionalPositionalOmitted() {
        PositionalSpec optional = new PositionalSpec(
            "engine", Token.Type.WORD, false
        );

        ParsedArgs args = CommandParser.parse(
            List.of(),
            List.of(optional),
            List.of()
        );

        assertTrue(args.positionals().isEmpty());
    }

    @Test
    void rejectsInvalidPositionalType() {
        assertThrows(InvalidPositionalTypeException.class, () ->
            CommandParser.parse(
                List.of(number("123")),
                List.of(engineSpec()),
                List.of()
            )
        );
    }

    @Test
    void rejectsUnexpectedPositional() {
        assertThrows(UnexpectedPositionalArgumentException.class, () ->
            CommandParser.parse(
                List.of(word("stockfish"), word("leela")),
                List.of(engineSpec()),
                List.of()
            )
        );
    }

    @Test
    void rejectsPositionalWhenNoneExpected() {
        assertThrows(UnexpectedPositionalArgumentException.class, () ->
            CommandParser.parse(
                List.of(word("stockfish")),
                List.of(),
                List.of()
            )
        );
    }

    @Test
    void rejectsDuplicateFlag() {
        assertThrows(DuplicateOptionException.class, () ->
            CommandParser.parse(
                List.of(option("--verbose"), option("-v")),
                List.of(),
                List.of(verboseSpec())
            )
        );
    }
}
