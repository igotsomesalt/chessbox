package chessbox.cli.command;

import java.util.List;

import chessbox.App;
import chessbox.cli.token.Token;

public class HelpCommand extends Command {

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

        if (args.size() == 0) {
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

        if (args.size() == 1) {
            App.ctx.out().println(HelpFormatter.format(App.ctx.commands().get(args.get(0).lexeme())));
        }

        else throw new IllegalArgumentException("Arguments out of bounds");
	}  
}
