package chessbox.cli.command;

public interface Command {
    
    String name();
    String description();
    void execute(String[] args) throws Exception;
}
