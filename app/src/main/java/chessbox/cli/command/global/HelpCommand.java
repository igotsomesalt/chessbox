package chessbox.cli.command.global;

import java.util.List;

import chessbox.App;
import chessbox.cli.command.Command;
import chessbox.cli.command.HelpFormatter;
import chessbox.cli.parse.CommandParser;
import chessbox.cli.parse.ParsedArgs;
import chessbox.cli.parse.PositionalSpec;
import chessbox.cli.token.Token;

public class HelpCommand extends Command {

    public HelpCommand() {
        positionals.add(
            new PositionalSpec("command", Token.Type.WORD, false)
        );
    }

	@Override
	public String name() {
		return "help";
	}

	@Override
	public String description() {
		return "Show help information";
	}

    @Override 
    public String usage() {
        return "help [command]";
    }

	@Override
	public void execute(List<Token> args) throws Exception {
        ParsedArgs parsedArgs = CommandParser.parse(args, positionals, options);

        Token name = parsedArgs.positional("command");

        if (name == null) {
           App.ctx.out().println(HelpFormatter.formatCommands(App.ctx.commands().getAll())); 
        }

        else {
            Command cmd = App.ctx.commands().get(name.lexeme());
            if (cmd == null) throw new IllegalArgumentException("Unknown command");
            App.ctx.out().println(HelpFormatter.formatCommand(cmd));
        }
	}  
}
