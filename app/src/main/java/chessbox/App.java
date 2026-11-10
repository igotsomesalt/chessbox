package chessbox;

import chessbox.cli.Cli;
import chessbox.util.AppPaths;

public class App {
    public static final ApplicationContext ctx = new ApplicationContext(
        AppPaths.installationDir(), System.out, System.err
    );

    public static void main(String[] args) {
        if (!ctx.setup) return;
        Cli cli = new Cli(System.in, ctx.commands());
        cli.run();
    }
}
