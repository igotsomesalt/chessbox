package chessbox;

import chessbox.cli.Cli;

public class App {
    public static final ApplicationContext ctx = new ApplicationContext(System.out, System.err);

    public static void main(String[] args) {
        Cli cli = new Cli(System.in, ctx.commands());
        cli.run();
    }
}
