package chessbox.cli.command;

public interface Command {
    
    String name();
    String description();
    void execute() throws Exception;
}
