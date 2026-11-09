package chessbox.cli.command;

public interface Command {
    
    String name();
    String description();
    String usage();
    void execute(String[] args) throws Exception;
}
