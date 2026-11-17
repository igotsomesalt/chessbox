package chessbox.cli.command.global;

import java.util.List;

import chessbox.cli.command.Command;
import chessbox.cli.parse.CommandParser;
import chessbox.cli.token.Token;

public class ShellCommand extends Command {

	@Override
	public String name() {
		return "shell";
	}

	@Override
	public String description() {
		return "Run in chessbox shell";
	}

	@Override
	public void execute(List<Token> args) throws Exception {
		CommandParser.parse(args, positionals, options);
        
	}

}
