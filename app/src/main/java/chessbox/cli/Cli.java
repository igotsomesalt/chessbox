package chessbox.cli;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

import chessbox.cli.command.Command;
import chessbox.cli.command.CommandRegistry;

public class Cli {
    private CommandRegistry registry;
    private final Scanner scanner;
    private boolean running = false;

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
        running = true;

        printWelcome();

        while (running) {
            System.out.print("chessbox> ");

            if (!scanner.hasNextLine()) {
                break;
            }

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            try {
                String[] tokens = input.split(" ");
                Command command = registry.get(tokens[0]);
                command.execute(Arrays.copyOfRange(tokens, 1, tokens.length));
            } 
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
