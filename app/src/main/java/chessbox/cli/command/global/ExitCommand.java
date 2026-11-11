package chessbox.cli.command.global;

import java.util.List;

import chessbox.cli.CliMain;
import chessbox.cli.command.Command;
import chessbox.cli.parse.CommandParser;
import chessbox.cli.token.Token;

public class ExitCommand extends Command {

	@Override
	public String name() {
		return "exit";
	}

	@Override
	public String description() {
		return "Exit application";
	}

	@Override
	public String usage() {
		return "exit";
	}

	@Override
	public void execute(List<Token> args) throws Exception {
        CommandParser.parse(args, positionals, options);
		CliMain.ctx.exit();
	}
    
}
