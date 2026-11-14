
package chessbox.cli;

import java.io.OutputStream;

import chessbox.cli.command.CommandRegistry;
import chessbox.cli.command.daemon.DaemonCommand;
import chessbox.cli.command.global.*;
import chessbox.cli.command.pool.PoolCommand;
import chessbox.util.LogWriter;

public class CliContext {
    private final CommandRegistry commandRegistry;
    private final DaemonClient daemonClient;
    private final LogWriter logWriter;

    private boolean running = true;

    public CliContext(OutputStream out, OutputStream err) {
        this.logWriter = new LogWriter(out, err);

        this.daemonClient = new DaemonClient(
            "127.0.0.1",
            7878
        );

        this.commandRegistry = new CommandRegistry(
            new VersionCommand(),
            new HelpCommand(),
            new ExitCommand(),
            new PoolCommand(),
            new DaemonCommand()
        );
    }

    public CommandRegistry commands() {
        return commandRegistry;
    }

    public DaemonClient daemon() {
        return daemonClient;
    }

    public LogWriter out() {
        return logWriter;
    }

    public void exit() {
        running = false;
    }

    public boolean running() {
        return running;
    }
}
