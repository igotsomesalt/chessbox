package chessbox.cli.command;

import java.util.Collection;

import chessbox.util.Registry;

public class CommandRegistry extends Registry<String, Command> {

    public CommandRegistry(Collection<Command> commands) {
        super(commands);
    }

    public CommandRegistry(Command... commands) {
        super(commands);
    }
}
