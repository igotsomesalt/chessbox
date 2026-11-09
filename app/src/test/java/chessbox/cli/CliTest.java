package chessbox.cli;

import org.junit.jupiter.api.Test;

import chessbox.cli.command.CommandRegistry;

public class CliTest {

    @Test 
    void versionTest() {
        System.out.println(Cli.getVersion());
    }

    @Test 
    void printWelcome() {
        Cli cli = new Cli(new CommandRegistry());
        cli.run();
    }
}
