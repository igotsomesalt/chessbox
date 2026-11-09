package chessbox.cli.command;

import chessbox.util.Registry;

public class CommandRegistry extends Registry<String, Command> {

    public CommandRegistry(Command... commands) {
        super();
        for (Command cmd : commands) {
            register(cmd);
        }
    }
}
