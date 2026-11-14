package chessbox.cli.command.daemon;

import java.util.List;

import chessbox.cli.command.Command;
import chessbox.cli.command.Dispatcher;
import chessbox.cli.token.Token;

public class DaemonCommand extends Command implements Dispatcher {
    
	public DaemonCommand() {
		super();
		leafCommands.registerAll(
			new DaemonStartCommand(),
            new DaemonStopCommand()
		);
	}

	@Override
	public String name() {
		return "daemon";
	}

	@Override
	public String description() {
		return "Manage engine daemon execution lifetime";
	}

	@Override
	public String usage() {
		return "daemon <command> [options]";
	}

	@Override
	public void execute(List<Token> args) throws Exception {
		dispatch(name(), args, leafCommands);
	}
}
