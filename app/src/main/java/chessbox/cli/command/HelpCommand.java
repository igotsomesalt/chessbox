package chessbox.cli.command;

import chessbox.App;

public class HelpCommand implements Command {

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
	public void execute(String[] args) throws Exception {

        if (args.length == 0) {
            App.ctx.out().print("""
                Chessbox - An engine simulation and general chess tool.

                Usage:
                    [command] [options]

                Commands:
                """
            );

            int width = App.ctx.commands().getAll().stream()
                .mapToInt(c -> c.name().length())
                .max()
                .orElse(0) + 4;

            for (Command cmd : App.ctx.commands().getAll()) {
                App.ctx.out().printf("    %-" + width + "s%s%n", cmd.name(), cmd.description());
            }

            App.ctx.out().println("\nRun 'help <command> for more information.");
        }

        if (args.length == 1) {
            App.ctx.out().println(HelpFormatter.format(App.ctx.commands().get(args[0])));
        }
	}
}
