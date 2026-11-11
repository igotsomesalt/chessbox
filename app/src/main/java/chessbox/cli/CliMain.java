package chessbox.cli;

import chessbox.util.AppPaths;

public class CliMain {
    public static final AppContext ctx = new AppContext(
        AppPaths.installationDir(), System.out, System.err
    );

    public static void main(String[] args) {
        if (!ctx.setup) return;
        Cli cli = new Cli(System.in, ctx.commands());
        cli.run();
    }
}
