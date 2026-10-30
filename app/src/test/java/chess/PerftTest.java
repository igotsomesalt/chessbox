package chess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1"
        ));
    }

    private void perft(int depth, long expected, boolean verbose, Position position) {
        long nodes = Perft.perft(position, depth, verbose);

        System.out.println(String.format(
            "depth=%d expected=%d actual=%d", depth, expected, nodes
        ));

        assertEquals(expected, nodes);
    }
}