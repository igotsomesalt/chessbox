package chessbox.cli.command.daemon;

import java.util.List;

import chessbox.cli.CliMain;
import chessbox.cli.DaemonLauncher;
import chessbox.cli.command.Command;
import chessbox.cli.token.Token;

public class DaemonStartCommand extends Command {

    @Override
    public String name() {
        return "start";
    }

    @Override
    public String description() {
        return "Start daemon";
    }

    @Override
    public String usage() {
        return "daemon start";
    }

    @Override
    public void execute(List<Token> args) throws Exception {
        DaemonLauncher.start();
        CliMain.ctx.out().println("Daemon started");
    }
    
}
