package chessbox.cli.command;

public class CommandException extends Exception {
    private final Command command;

    public CommandException(Command command, Throwable cause) {
        super(cause.getMessage(), cause);
        this.command = command;
    }

    public Command command() {
        return command;
    }
}
