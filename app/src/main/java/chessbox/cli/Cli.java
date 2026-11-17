package chessbox.cli;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import chessbox.cli.command.Command;
import chessbox.cli.command.CommandRegistry;
import chessbox.cli.parse.ParseException;
import chessbox.cli.parse.UnknownCommandException;
import chessbox.cli.token.Token;
import chessbox.cli.token.Tokenizer;

public class Cli {
    private CommandRegistry registry;
    private final Scanner scanner;

    public Cli(InputStream in, CommandRegistry registry) {
        this.registry = registry;
        this.scanner = new Scanner(in);
    }

    private void printWelcome() {
        System.out.printf("""
            Chessbox v%s
            A command-line platform for chess engine testing and tournaments.

            Type 'help' for commands, or 'exit' to quit
            """, getVersion()
        );
    }

    public static String getVersion() {
        String version = Cli.class
            .getPackage()
            .getImplementationVersion();

        return version != null ? version : "dev";
    }

    public void run() {
        printWelcome();

        while (true) {
            CliMain.ctx.out().print("chessbox # ");
            CliMain.ctx.out().flush();

            if (!scanner.hasNextLine()) {
                break;
            }

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            execute(input.split(" "));
        }
    }

    public void execute(String[] input) {
        Command command = null;

        try {
            List<Token> tokens = Tokenizer.tokenize(input);

            if (tokens.isEmpty()) return;

            Token dispatch = tokens.get(0);
            command = registry.get(dispatch.lexeme());

            if (command == null) throw new UnknownCommandException(dispatch.lexeme());

            command.execute(tokens.subList(1, tokens.size()));

        } catch (ParseException e) {
            CliMain.ctx.out().println(e.getMessage());

            if (command != null) {
                CliMain.ctx.out().println(command.usage());
            }

        } catch (Exception e) {
            CliMain.ctx.out().println(e.getMessage());
        }
    }

}
