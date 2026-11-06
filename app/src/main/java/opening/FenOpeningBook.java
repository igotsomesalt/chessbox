package opening;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import chess.Position;

public class FenOpeningBook implements OpeningBook {
    private final List<String> fens;
    private final Random random = new Random();
    private final Set<String> seen = new HashSet<>();
    private String source;

    public FenOpeningBook(Path path) throws IOException {
        this(path.toString(), Files.newInputStream(path));
    }

    public FenOpeningBook(String source, InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            fens = reader.lines()
                .filter(s -> !s.isBlank())
                .toList();
        }

        if (fens.isEmpty()) {
            throw new IllegalArgumentException("Empty opening book");
        }

        this.source = source;
    }

    @Override
    public Position nextPosition() throws IllegalArgumentException {
        String fen;

        do {
            fen = fens.get(random.nextInt(fens.size()));
        }
        while (seen.contains(fen));

        seen.add(fen);
        return new Position(fen);
    }

    @Override 
    public String toString() {
        return source;
    }
}
