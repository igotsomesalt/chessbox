package chessbox.cli.command;

import chessbox.util.Indentifiable;

public abstract class Command implements Indentifiable<String> {
    
    @Override 
    public String id() {
        return name();
    }

    public abstract String name();
    public abstract String description();
    public abstract String usage();
    public abstract void execute(String[] args) throws Exception;
}
