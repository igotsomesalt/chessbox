package chessbox.cli.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import chessbox.util.Indentifiable;

public abstract class Command implements Indentifiable<String> {

    protected final CommandRegistry subCommands = new CommandRegistry();
    protected final List<String> options = new ArrayList<>();
    
    @Override 
    public String id() {
        return name();
    }

    public abstract String name();
    public abstract String description();
    public abstract String usage();

    public Collection<Command> subCommands() {
        return subCommands.getAll();
    }

    public List<String> options() {
        return options;
    }

    public abstract void execute(String[] args) throws Exception;
}
