package chessbox.cli;


public class CliMain {
    public static final CliContext ctx;

    static {
        try {
            ctx = new CliContext(System.out, System.err);
        }

        catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void main(String[] args) {
        Cli cli = new Cli(System.in, ctx.commands());
        cli.run();
    }
}
