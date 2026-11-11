package chessbox.cli.command;

import java.util.Collection;

public class HelpFormatter {

    private HelpFormatter() {}

    public static String formatCommand(Command command) {
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

    public static String formatCommands(Collection<Command> commands) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
            Chessbox - An engine simulation and general chess tool.

            Usage:
                [command] [options]

            Commands:
            """
        );

        int width = commands.stream()
            .mapToInt(c -> c.name().length())
            .max()
            .orElse(0) + 4;

        for (Command cmd : commands) {
            sb.append(String.format("\t%-" + width + "s%s%n", cmd.name(), cmd.description()));
        }

        sb.append("\nRun 'help <command> for more information.");
        return sb.toString();
    }
}
