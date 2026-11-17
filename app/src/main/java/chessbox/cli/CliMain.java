package chessbox.cli;


public class CliMain {
    public static final CliContext ctx;

    static {
        try {
            ctx = new CliContext(System.out, System.err);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

     public static void main(String[] args) {

        try {
            long start = System.nanoTime();

            CliContext ctx = new CliContext(System.out, System.err);

            Cli cli = new Cli(System.in, ctx.commands());

            if (args.length == 0 || (args.length == 1 && args[0].equals("shell"))) {
                cli.run();
            } else {
                cli.execute(String.join(" ", args));
            }

            System.out.println(String.format(
                "Process completed in %d ms", (System.nanoTime() - start) / 1000000
            ));

            System.exit(0);

        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
