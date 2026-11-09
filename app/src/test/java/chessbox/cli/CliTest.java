package chessbox.cli;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import chessbox.cli.command.CommandRegistry;

public class CliTest {

    @Test 
    void versionTest() {
        System.out.println(Cli.getVersion());
    }

    @Test 
    void printWelcome() {        
        InputStream inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        Cli cli = new Cli(inputStream, new CommandRegistry());
        cli.run();
    }
}
