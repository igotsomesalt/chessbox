package chessbox.cli.command;

public class HelpFormatter {

    private HelpFormatter() {}

    public static String format(Command command) {
        return String.format(
            """
            %s

            Usage:
              %s

            """,
            command.description(),
            command.usage()
        );
    }
}
