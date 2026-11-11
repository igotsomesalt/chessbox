package chessbox.cli.command.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import chessbox.App;
import chessbox.cli.command.Command;
import chessbox.cli.parse.CommandParser;
import chessbox.cli.parse.OptionSpec;
import chessbox.cli.parse.ParsedArgs;
import chessbox.cli.token.Token;
import chessbox.engine.EnginePool;
import chessbox.util.UUID;

public class LsCommand extends Command {

	public LsCommand() {
		options.addAll(List.of(
			new OptionSpec("quiet", "q", null, false, null),
			new OptionSpec("engine", null, Token.Type.WORD, false, null),
			new OptionSpec("no-trunc", null, null, false, null)
		));
	}

	@Override
	public String name() {
		return "ls";
	}

	@Override
	public String description() {
		return "List active pools";
	}

	@Override
	public String usage() {
		return "pool ls [options]";
	}

	@Override
	public void execute(List<Token> args) throws Exception {
		ParsedArgs parsed = CommandParser.parse(args, positionals, options);

		boolean quiet = parsed.hasFlag("quiet");
		boolean noTrunc = parsed.hasFlag("no-trunc");

		Collection<EnginePool> pools = App.ctx.engines().listEnginePools();

		if (parsed.option("engine") != null) {
			String engine = parsed.option("engine").lexeme();

			pools = pools.stream()
				.filter(pool -> pool.config().name().equalsIgnoreCase(engine))
				.toList();
		}

		if (quiet) {
			for (EnginePool pool : pools) {
				App.ctx.out().println(noTrunc ? pool.id().toString() : UUID.shortId(pool.id()));
			}
			return;
		}

		List<List<String>> grid = new ArrayList<>();

		grid.add(List.of(
			"POOL ID",
			"ENGINE",
			"SIZE",
			"AVAILABLE"
		));

		for (EnginePool pool : pools) {
			grid.add(List.of(
				formatId(pool.id(), noTrunc),
				pool.config().name(),
				String.valueOf(pool.size()),
				String.valueOf(pool.available())
			));
		}

		printTable(grid);
	}

	private String formatId(java.util.UUID id, boolean noTrunc) {
		return noTrunc ? id.toString() : UUID.shortId(id);
	}

	private void printTable(List<List<String>> grid) {
		int columns = grid.get(0).size();
		int[] widths = new int[columns];

		for (List<String> row : grid) {
			for (int i = 0; i < columns; i++) {
				widths[i] = Math.max(widths[i], row.get(i).length());
			}
		}

		for (List<String> row : grid) {
			for (int i = 0; i < columns; i++) {
				App.ctx.out().printf(
					"%-" + (widths[i] + 2) + "s",
					row.get(i)
				);
			}

			App.ctx.out().println("");
		}
	}

}
