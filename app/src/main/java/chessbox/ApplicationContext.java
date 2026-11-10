package chessbox;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

import chessbox.cli.command.CommandRegistry;
import chessbox.cli.command.HelpCommand;
import chessbox.cli.command.VersionCommand;
import chessbox.cli.command.pool.PoolCommand;
import chessbox.cli.listener.EngineEventPrinter;
import chessbox.cli.listener.GameEventPrinter;
import chessbox.engine.EngineConfig;
import chessbox.engine.EngineConfigLoader;
import chessbox.engine.EnginePoolRegistry;
import chessbox.engine.EngineRegistry;
import chessbox.engine.EngineService;
import chessbox.game.GameService;
import chessbox.game.MatchRegistry;

public class ApplicationContext implements AutoCloseable {

    private final EnginePoolRegistry enginePoolRegistry;
    private final CommandRegistry commandRegistry;
    private final EngineRegistry engineRegistry;
    private final MatchRegistry matchRegistry;
    private final EngineService engineService;
    private final GameService gameService;
    private final LogWriter logWriter;

    boolean setup = true;

    public ApplicationContext(Path appDir, OutputStream out, OutputStream err) {
            this.commandRegistry = new CommandRegistry(
            new VersionCommand(),
            new HelpCommand(),
            new PoolCommand()
        );
        
        this.engineRegistry = new EngineRegistry();

        try {
            EngineConfigLoader configLoader = new EngineConfigLoader();
            List<EngineConfig> configs = configLoader.loadDefaults(appDir);
            engineRegistry.registerAll(configs);
        }

        catch (Exception e) {
            out().println(e.getMessage());
            setup = false;
        }

        this.enginePoolRegistry = new EnginePoolRegistry();
        this.matchRegistry = new MatchRegistry();
        this.engineService = new EngineService(engineRegistry, enginePoolRegistry);
        this.gameService = new GameService(matchRegistry);
        this.logWriter = new LogWriter(out, err);

        engineService.addListener(
            new EngineEventPrinter(new PrintStream(out), new PrintStream(err))
        );

        gameService.addListener(
            new GameEventPrinter(new PrintStream(out), new PrintStream(err))
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
