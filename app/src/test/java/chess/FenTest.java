package chess;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FenTest {

    @Test
    void startingPositionToFen() {
        Position position = new Position();

        String fen = Fen.toFen(position);

        assertEquals(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            fen
        );
    }

    @Test
    void startingFenToPosition() {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

        Position position = Fen.fromFen(fen);

        assertEquals(true, position.whiteToMove);
        assertEquals(-1, position.epSq);
        assertEquals(0xf, position.castlingRights);

        for (int i = 0; i < 12; i++) {
            assertEquals(
                Position.DEFAULT_BITBOARDS[i],
                position.bitboards[i]
            );
        }
    }

    @Test
    void roundTripStartingPosition() {
        Position position = new Position();

        String fen = Fen.toFen(position);
        Position parsed = Fen.fromFen(fen);
        String result = Fen.toFen(parsed);

        assertEquals(fen, result);
    }

    @Test
    void complexPositionRoundTrip() {
        String fen =
                "r3k2r/ppp2ppp/2n1b3/8/8/2N1B3/PPP2PPP/R3K2R b KQkq e3 12 25";

        Position position = Fen.fromFen(fen);

        assertEquals(false, position.whiteToMove);
        assertEquals(20, position.epSq);
        assertEquals(0xf, position.castlingRights);

        assertEquals(fen, Fen.toFen(position));
    }

    @Test
    void castlingRightsAreParsedCorrectly() {
        Position position = Fen.fromFen(
                "8/8/8/8/8/8/8/4K2k w Kq - 0 1"
        );

        assertEquals(true, position.getCastlingRights(true, true));   // K
        assertEquals(false, position.getCastlingRights(false, true)); // Q
        assertEquals(false, position.getCastlingRights(true, false)); // k
        assertEquals(true, position.getCastlingRights(false, false)); // q
    }

    @Test
    void enPassantSquareIsParsedCorrectly() {
        Position position = Fen.fromFen(
                "8/8/8/8/8/8/4P3/4K2k w - e3 0 1"
        );

        assertEquals(20, position.epSq);
        assertEquals("e3", Fen.toFen(position).split(" ")[3]);
    }

    @Test
    void blackToMoveIsParsedCorrectly() {
        Position position = Fen.fromFen(
                "8/8/8/8/8/8/4p3/4k2K b - - 0 1"
        );

        assertEquals(false, position.whiteToMove);
    }
}
