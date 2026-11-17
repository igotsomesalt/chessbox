package chessbox.cli;


public class CliMain {
    public static final CliContext ctx;

    static {
        try {
            ctx = new CliContext(System.out, System.err);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void main(String[] args) {
        Cli cli = new Cli(System.in, ctx.commands());

        if (args.length == 0 ||
            (args.length == 1 && args[0].equals("shell"))) {
            cli.run();
        } else {
            cli.execute(String.join(" ", args));
        }
    }
}
