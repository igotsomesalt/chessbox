package engine;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class UciEngine implements AutoCloseable {

    private final Process process;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    public UciEngine(String executable) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(executable);

        builder.redirectError(ProcessBuilder.Redirect.INHERIT);

        process = builder.start();

        reader = new BufferedReader(
            new InputStreamReader(
                process.getInputStream(),
                StandardCharsets.UTF_8
            )
        );

        writer = new BufferedWriter(
            new OutputStreamWriter(
                process.getOutputStream(),
                StandardCharsets.UTF_8
            )
        );
    }

    public void send(String command) throws IOException {
        writer.write(command);
        writer.newLine();
        writer.flush();
    }

    public String readUntil(String prefix) throws IOException {
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith(prefix)) {
                return line;
            }
        }

        throw new EOFException("Engine exited unexpectedly");
    }

    public void initialize() throws IOException {
        send("uci");
        readUntil("uciok");

        send("isready");
        readUntil("readyok");
    }

    public String getBestMove(String fen, int depth)
            throws IOException {

        send("position fen " + fen);
        send("go depth " + depth);

        String response = readUntil("bestmove ");

        return response.split("\\s+")[1];
    }

    @Override
    public void close() throws IOException {
        try {
            if (process.isAlive()) {
                send("quit");
            }
        } finally {
            try {
                writer.close();
                reader.close();
            } finally {
                if (process.isAlive()) {
                    process.destroy();
                }
            }
        }
    }
}
