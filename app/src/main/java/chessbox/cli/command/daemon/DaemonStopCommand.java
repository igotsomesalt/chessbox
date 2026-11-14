package chessbox.cli.command.daemon;

import java.util.List;

import chessbox.cli.CliMain;
import chessbox.cli.command.Command;
import chessbox.cli.token.Token;
import chessbox.protocol.req.global.ShutdownDaemonRequest;
import chessbox.protocol.res.DaemonStatusResponse;

public class DaemonStopCommand extends Command {

    @Override
    public String name() {
        return "stop";
    }

    @Override
    public String description() {
        return "Stop daemon.";
    }

    @Override
    public String usage() {
        return "daemon stop";
    }

    @Override
    public void execute(List<Token> args) throws Exception {
        DaemonStatusResponse response = CliMain.ctx.daemon().send(new ShutdownDaemonRequest(), DaemonStatusResponse.class);
        CliMain.ctx.out().println("Daemon stopped");
    }

}
