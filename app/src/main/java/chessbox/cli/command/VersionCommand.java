package chessbox.cli.command;

import chessbox.cli.Cli;

public class VersionCommand implements Command {

	@Override
	public String name() {
		return "version";
	}

	@Override
	public String description() {
        return "Displays version";
	}

	@Override
	public void execute(String[] args) throws Exception {
        String version = Cli.class
            .getPackage()
            .getImplementationVersion();

        System.out.println(version != null ? version : "dev");
	}

}
