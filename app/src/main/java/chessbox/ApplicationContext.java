package chessbox;

import chessbox.cli.command.CommandRegistry;
import chessbox.cli.command.VersionCommand;
import chessbox.engine.EngineRegistry;
import chessbox.engine.EngineService;
import chessbox.game.GameService;

public class ApplicationContext implements AutoCloseable {

    private final CommandRegistry commandRegistry;
    private final EngineRegistry engineRegistry;
    private final EngineService engineService;
    private final GameService gameService;

    public ApplicationContext() {
        this.commandRegistry = new CommandRegistry(
            new VersionCommand()
        );

        this.engineRegistry = new EngineRegistry();
        this.engineService = new EngineService(engineRegistry);
        this.gameService = new GameService();
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

    @Override
    public void close() throws Exception {
        engineService.close();
        gameService.close();
    }
}
