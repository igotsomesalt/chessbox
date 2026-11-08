package chessbox.cli.command;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class CommandRegistry {

    private final Map<String, Command> commands = new HashMap<>();

    public CommandRegistry() {}

    public void register(Command command) {
        commands.put(command.name(), command);
    }

    public Command get(String name) {
        Command cmd = commands.get(name);
        if (cmd == null) throw new NoSuchElementException("Unknown command: " + name);
        return cmd;
    }
}
