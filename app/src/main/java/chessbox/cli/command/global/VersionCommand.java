package chessbox.cli.command.global;

import java.util.List;

import chessbox.App;
import chessbox.cli.Cli;
import chessbox.cli.command.Command;
import chessbox.cli.token.Token;

public class VersionCommand extends Command {

	@Override
	public String name() {
		return "version";
	}

	@Override
	public String description() {
        return "Display application version";
	}	
    
    @Override
	public String usage() {
		return "version";
	}

	@Override
	public void execute(List<Token> args) throws Exception {
        if (args.size() == 0) throw new IllegalArgumentException("Arguments out of bounds");

        String version = Cli.class
            .getPackage()
            .getImplementationVersion();

        App.ctx.out().println(version != null ? version : "dev");
	}

}
