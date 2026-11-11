package chessbox.util;

import java.io.OutputStream;
import java.io.PrintStream;

public class LogWriter {

    private final PrintStream out;
    private final PrintStream err;

    public LogWriter(OutputStream out, OutputStream err) {
        this.out = new PrintStream(out, true);
        this.err = new PrintStream(err, true);
    }

    public void println(String message) {
        out.println(message);
    }

    public void print(String message) {
        out.print(message);
    }

    public void printf(String format, Object... args) {
        out.printf(format, args);
        out.flush();
    }

    public void error(String message) {
        err.println(message);
    }
}
