package chessbox;

import chessbox.cli.Cli;

public class App {
    public static final ApplicationContext context = new ApplicationContext();

    public static void main(String[] args) {
        Cli cli = new Cli(System.in, context.commands());
        cli.run();
    }
}
