package chessbox.cli.command.daemon;

import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import chessbox.cli.CliMain;
import chessbox.cli.command.Command;
import chessbox.cli.parse.CommandParser;
import chessbox.cli.parse.OptionSpec;
import chessbox.cli.parse.ParsedArgs;
import chessbox.cli.token.Token;
import chessbox.protocol.req.global.DaemonStatusRequest;
import chessbox.protocol.res.global.DaemonStatusResponse;

public class DaemonStatusCommand extends Command {

    private final ObjectMapper mapper = new ObjectMapper();

    public DaemonStatusCommand() {
        options.addAll(List.of(
            new OptionSpec("json", "j", null, false, null),
            new OptionSpec("quiet", "q", null, false, null),
            new OptionSpec("watch", "w", null, false, null),
            new OptionSpec("interval", "i", Token.Type.NUMBER, false, null)
        ));
    }

	@Override
	public String name() {
		return "status";
	}

	@Override
	public String description() {
		return "Show daemon status";
	}

	@Override
	public void execute(List<Token> args) throws Exception {
		ParsedArgs parsed = CommandParser.parse(args, positionals, options);

        boolean json = parsed.flags().contains("json");
        boolean quiet = parsed.flags().contains("quiet");
        boolean watch = parsed.flags().contains("watch");

        Token intervalToken = parsed.options().get("interval");
        int interval = intervalToken == null ? 1 : Integer.parseInt(intervalToken.lexeme());

        if (interval <= 0) {
            throw new IllegalArgumentException(
                "Interval must be greater than zero"
            );
        }

        if (intervalToken != null && !watch) {
            throw new IllegalArgumentException(
                "--interval requires --watch"
            );
        }

        if (quiet && (json || watch)) {
            throw new IllegalArgumentException(
                "--quiet cannot be combined with --json or --watch"
            );
        }

        if (watch) {
            watchStatus(json, interval);
        } else if (!quiet) {
            printStatus(fetchStatus(), json, 0);
        } else {
            fetchStatus();
        }
    }

    private void watchStatus(boolean json, int interval) throws Exception {
        int previousLines = 0;

        try {
            while (!Thread.currentThread().isInterrupted()) {
                DaemonStatusResponse status = fetchStatus();

                previousLines = printStatus(status, json, previousLines);

                try {
                    Thread.sleep(Duration.ofSeconds(interval));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            CliMain.ctx.out().flush();
        }
    }

    private int printStatus(DaemonStatusResponse status, boolean json, int previousLines) throws Exception {

        String output = json
            ? mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(status)
            : formatStatus(status);

        String[] lines = output.split("\\R", -1);

        if (previousLines > 0) {
            CliMain.ctx.out().printf("\033[%dA", previousLines);

            for (int i = 0; i < previousLines; i++) {
                CliMain.ctx.out().print("\r\033[2K\n");
            }

            CliMain.ctx.out().printf("\033[%dA", previousLines);
        }

        for (String line : lines) {
            CliMain.ctx.out().print("\r\033[2K");
            CliMain.ctx.out().println(line);
        }

        return lines.length;
    }

    private String formatDuration(long seconds) {
        Duration duration = Duration.ofSeconds(seconds);

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long secs = duration.toSecondsPart();

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    private DaemonStatusResponse fetchStatus() throws Exception {
        return CliMain.ctx.daemon().send(new DaemonStatusRequest(), DaemonStatusResponse.class);
    }

    private String formatStatus(DaemonStatusResponse status) {
        return """
            Status:  %s
            PID:     %d
            Uptime:  %s
            Matches: %d
            """.formatted(
                status.status(),
                status.pid(),
                formatDuration(status.uptimeSeconds()),
                status.activeMatches()
            ).stripTrailing();
    }
}
