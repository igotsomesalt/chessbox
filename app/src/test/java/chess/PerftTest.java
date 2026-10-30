package chess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class PerftTest {

    @Test
    void perft() {
        String fen = System.getenv("PERFT_FEN");
        int depth;
        long expected;
        boolean verbose;

        try {
            depth = Integer.parseInt(System.getenv("PERFT_DEPTH"));
        }

        catch (NumberFormatException e) {
            System.out.println("PERFT_DEPTH is not a number");
            return;
        }

        try {
            expected = Long.parseLong(System.getenv("PERFT_EXPECTED"));
        }

        catch (NumberFormatException e) {
            System.out.println("PERFT_PRINT is not a number");
            return;
        }

        try {
            verbose = Integer.parseInt(System.getenv("PERFT_VERBOSE")) != 0;
        }

        catch (NumberFormatException e) {
            System.out.println("PERFT_PRINT is not a number");
            return;
        }

        assertNotNull(fen, "PERFT_FEN environment variable is not set");

        Position position = new Position(fen);
        perft(depth, expected, verbose, position);
    }

    @ParameterizedTest
    @CsvSource({
        "1, 20",
        "2, 400",
        "3, 8902",
        "4, 197281",
        "5, 4865609"
    })
    void startingPositionPerft(int depth, long expected) {
        perft(depth, expected, false, new Position());
    }

    @ParameterizedTest
    @CsvSource({
        "1, 48",
        "2, 2039",
        "3, 97862",
        "4, 4085603"
    })
    void kiwipetePerft(int depth, long expected) {
        perft(depth, expected, false, new Position(
            "r3k2r/p1ppqpb1/bn2pnp1/2pP4/1p2P3/2N2N2/PPQ1BPPP/R3K2R w KQkq - 0 1"
        ));
    }

    @ParameterizedTest(name = "FEN: {0}, depth: {1}, expected nodes: {2}")
    @CsvSource({
        "'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1', 1, 20",
        "'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1', 2, 400",
        "'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1', 3, 8902"
    })
    void divideTest(String fen, int depth, long expectedNodes) {
        Position position = Fen.fromFen(fen);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));
            Perft.divide(position, depth);

        } finally {
            System.setOut(originalOut);
        }

        String result = output.toString();

        String totalLine = result.lines()
            .filter(line -> line.startsWith("Total:"))
            .findFirst()
            .orElseThrow();

        long actualNodes = Long.parseLong(
            totalLine.substring("Total:".length()).trim()
        );

        System.out.println(
            "depth=" + depth +
            " expected=" + expectedNodes +
            " actual=" + actualNodes + '\n'
        );

        assertEquals(expectedNodes, actualNodes);
    }

    private void perft(int depth, long expected, boolean verbose, Position position) {
        long nodes = Perft.perft(position, depth, verbose);

        System.out.println(String.format(
            "depth=%d expected=%d actual=%d", depth, expected, nodes
        ));

        assertEquals(expected, nodes);
    }
}