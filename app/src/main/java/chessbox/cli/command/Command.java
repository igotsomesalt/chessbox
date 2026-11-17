package chessbox.cli.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import chessbox.cli.parse.OptionSpec;
import chessbox.cli.parse.PositionalSpec;
import chessbox.cli.token.Token;
import chessbox.util.Indentifiable;

public abstract class Command implements Indentifiable<String> {

    protected final CommandRegistry leafCommands = new CommandRegistry();
    protected final List<PositionalSpec> positionals = new ArrayList<>();
    protected final List<OptionSpec> options = new ArrayList<>();
    
    @Override 
    public String id() {
        return name();
    }

    public abstract String name();
    public abstract String description();

    public String usage() {
        StringBuilder sb = new StringBuilder("Usage: chessbox ");
        sb.append(name());

        for (PositionalSpec spec : positionals) {
            String arg = "<" + spec.name() + ">";

            if (!spec.required()) {
                arg = "[" + arg + "]";
            }

            sb.append(" ").append(arg);
        }

        for (OptionSpec spec : options) {
            String arg = "--" + spec.name();

            if (spec.takesValue()) {
                arg += " <" + spec.expectedType().name().toLowerCase() + ">";
            }

            if (!spec.required()) {
                arg = "[" + arg + "]";
            }

            sb.append(" ").append(arg);
        }

        return sb.toString();
    }

    public Collection<Command> leafCommands() {
        return leafCommands.getAll();
    }

    public List<PositionalSpec> positionals() {
        return positionals;
    }

    public List<OptionSpec> options() {
        return options;
    }

    public abstract void execute(List<Token> args) throws Exception;
}
