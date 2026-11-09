package chessbox.cli.command;

import chessbox.App;
import chessbox.cli.Cli;

public class VersionCommand implements Command {

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
	public void execute(String[] args) throws Exception {
        String version = Cli.class
            .getPackage()
            .getImplementationVersion();

        App.ctx.out().println(version != null ? version : "dev");
	}

}
