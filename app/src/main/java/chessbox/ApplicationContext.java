package chessbox;

import java.io.OutputStream;
import java.io.PrintStream;

import chessbox.cli.command.CommandRegistry;
import chessbox.cli.command.VersionCommand;
import chessbox.cli.listener.EngineEventPrinter;
import chessbox.engine.EngineRegistry;
import chessbox.engine.EngineService;
import chessbox.game.GameService;

public class ApplicationContext implements AutoCloseable {

    private final CommandRegistry commandRegistry;
    private final EngineRegistry engineRegistry;
    private final EngineService engineService;
    private final GameService gameService;
    private final LogWriter logWriter;

    public ApplicationContext(OutputStream out, OutputStream err) {
        this.commandRegistry = new CommandRegistry(
            new VersionCommand()
        );

        this.engineRegistry = new EngineRegistry();
        this.engineService = new EngineService(engineRegistry);
        this.gameService = new GameService();
        this.logWriter = new LogWriter(out, err);

        engineService.addListener(
            new EngineEventPrinter(new PrintStream(out), new PrintStream(err))
        );
    }

    public CommandRegistry commands() {
        return commandRegistry;
    }

    public EngineService engines() {
        return engineService;
    }

    public GameService games() {
        return gameService;
    }

    public LogWriter out() {
        return logWriter;
    }

    @Override
    public void close() throws Exception {
        engineService.close();
        gameService.close();
    }
}
