package chessbox.cli.command.pool;

import java.util.List;

import chessbox.cli.CliMain;
import chessbox.cli.command.Command;
import chessbox.cli.parse.CommandParser;
import chessbox.cli.parse.OptionSpec;
import chessbox.cli.parse.ParsedArgs;
import chessbox.cli.parse.PositionalSpec;
import chessbox.cli.token.Token;

public class CreatePoolCommand extends Command {
    
	public CreatePoolCommand() {
		positionals.addAll(List.of(
			new PositionalSpec("engine-id", Token.Type.WORD,true)
		));

		options.addAll(List.of(
			new OptionSpec("thread", "t", Token.Type.NUMBER, false, new Token(Token.Type.NUMBER, "1"))
		));
	}

	@Override
	public String name() {
		return "create";
	}

	@Override
	public String description() {
		return "Create and configure a pool";
	}

	@Override
    public String usage() {
        return "pool create <engine-id> [--size <count>]";
    }

	@Override
	public void execute(List<Token> args) throws Exception {
        ParsedArgs parsedArgs = CommandParser.parse(args, positionals, options);
		String engineId = parsedArgs.positional("engine-id").lexeme();
		int threads = Integer.parseInt(parsedArgs.option("thread").lexeme());
		CliMain.ctx.engines().createPool(engineId, threads);
	}
}
