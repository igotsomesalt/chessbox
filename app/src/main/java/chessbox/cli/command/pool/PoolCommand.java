package chessbox.cli.command.pool;

import java.util.List;

import chessbox.cli.command.Command;
import chessbox.cli.command.Dispatcher;
import chessbox.cli.token.Token;

public class PoolCommand extends Command implements Dispatcher {

	public PoolCommand() {
		super();
		leafCommands.registerAll(
			new CreatePoolCommand(),
			new LsCommand()
		);
	}

	@Override
	public String name() {
		return "pool";
	}

	@Override
	public String description() {
		return "Configure and manage engine pool execution";
	}

	@Override
	public String usage() {
		return "pool <command> [options]";
	}

	@Override
	public void execute(List<Token> args) throws Exception {
		dispatch(name(), args, leafCommands);
	}
}
