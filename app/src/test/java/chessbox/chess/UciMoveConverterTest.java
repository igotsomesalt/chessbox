package chessbox.chess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class UciMoveConverterTest {

    @ParameterizedTest
    @CsvSource({
        "8, 16, a2a3",
        "12, 28, e2e4",
        "6, 21, g1f3",
        "52, 36, e7e5",
        "0, 56, a1a8",
        "63, 7, h8h1"
    })
    void toNotation_convertsSquares(int from, int to, String expected) {
        int move = Move.encode(
            from,
            to,
            Position.WHITE_PAWN,
            Position.NO_PIECE,
            Position.NO_PIECE,
            0,
            false
        );

        assertEquals(expected, UciMoveConverter.toNotation(move));
    }

    @Test
    void toNotation_handlesPromotion() {
        int move = Move.encode(
            Position.A7,
            Position.A8,
            Position.WHITE_PAWN,
            Position.NO_PIECE,
            Position.WHITE_QUEEN,
            0,
            false
        );

        assertEquals("a7a8q", UciMoveConverter.toNotation(move));
    }

    @Test
    void toNotation_handlesKnightPromotion() {
        int move = Move.encode(
            Position.A7,
            Position.A8,
            Position.WHITE_PAWN,
            Position.NO_PIECE,
            Position.WHITE_KNIGHT,
            0,
            false
        );

        assertEquals("a7a8n", UciMoveConverter.toNotation(move));
    }

    @Test
    void toNotation_handlesBishopPromotion() {
        int move = Move.encode(
            Position.A7,
            Position.B8,
            Position.WHITE_PAWN,
            Position.NO_PIECE,
            Position.WHITE_BISHOP,
            0,
            false
        );

        assertEquals("a7b8b", UciMoveConverter.toNotation(move));
    }

    @Test
    void toNotation_handlesRookPromotion() {
        int move = Move.encode(
            Position.A7,
            Position.A8,
            Position.WHITE_PAWN,
            Position.NO_PIECE,
            Position.WHITE_ROOK,
            0,
            false
        );

        assertEquals("a7a8r", UciMoveConverter.toNotation(move));
    }

    @Test
    void toNotation_handlesBlackPromotion() {
        int move = Move.encode(
            Position.A2,
            Position.A1,
            Position.BLACK_PAWN,
            Position.NO_PIECE,
            Position.BLACK_QUEEN,
            0,
            false
        );

        assertEquals("a2a1q", UciMoveConverter.toNotation(move));
    }

    @Test
    void toNotation_moveOverload() {
        int encoded = Move.encode(
            Position.E2,
            Position.E4,
            Position.WHITE_PAWN,
            Position.NO_PIECE,
            Position.NO_PIECE,
            0,
            false
        );

        Move move = new Move(encoded);

        assertEquals("e2e4", UciMoveConverter.toNotation(move));
    }

    @Test
    void toMoveInt_convertsNormalMove() {
        Position position = new Position();

        int move = UciMoveConverter.toMoveInt("e2e4", position);

        assertEquals(Position.E2, Move.getSq1(move));
        assertEquals(Position.E4, Move.getSq2(move));
        assertEquals(Position.WHITE_PAWN, Move.getPiece(move));
        assertEquals(Position.NO_PIECE, Move.getCapPiece(move));
        assertEquals(Position.NO_PIECE, Move.getPromPiece(move));
    }

    @Test
    void toMoveInt_convertsKnightMove() {
        Position position = new Position();

        int move = UciMoveConverter.toMoveInt("g1f3", position);

        assertEquals(Position.G1, Move.getSq1(move));
        assertEquals(Position.F3, Move.getSq2(move));
        assertEquals(Position.WHITE_KNIGHT, Move.getPiece(move));
        assertEquals(Position.NO_PIECE, Move.getCapPiece(move));
        assertEquals(Position.NO_PIECE, Move.getPromPiece(move));
    }

    @Test
    void toMoveInt_detectsCapture() {
        Position position = new Position();

        // e2-e4, then d7-d5, then e4xd5
        position.makeMove(
            UciMoveConverter.toMoveInt("e2e4", position)
        );

        position.makeMove(
            UciMoveConverter.toMoveInt("d7d5", position)
        );

        int move = UciMoveConverter.toMoveInt("e4d5", position);

        assertEquals(Position.E4, Move.getSq1(move));
        assertEquals(Position.D5, Move.getSq2(move));
        assertEquals(Position.WHITE_PAWN, Move.getPiece(move));
        assertEquals(Position.BLACK_PAWN, Move.getCapPiece(move));
    }

    @ParameterizedTest
    @CsvSource({
        "a7a8q, 4",
        "a7a8r, 3",
        "a7a8b, 2",
        "a7a8n, 1"
    })
    void toMoveInt_handlesWhitePromotion(String uci, int expectedPiece) {
        Position position = new Position();

        // Build a position where a white pawn is on a7.
        // Replace this with your preferred FEN constructor if available.
        position = new Position(
            "8/P7/8/8/8/8/8/4K2k w - - 0 1"
        );

        int move = UciMoveConverter.toMoveInt(uci, position);

        assertEquals(Position.A7, Move.getSq1(move));
        assertEquals(Position.A8, Move.getSq2(move));
        assertEquals(Position.WHITE_PAWN, Move.getPiece(move));
        assertEquals(expectedPiece, Move.getPromPiece(move));
    }

    @ParameterizedTest
    @CsvSource({
        "a2a1q, 10",
        "a2a1r, 9",
        "a2a1b, 8",
        "a2a1n, 7"
    })
    void toMoveInt_handlesBlackPromotion(String uci, int expectedPiece) {
        Position position = new Position(
            "4k2K/8/8/8/8/8/p7/8 b - - 0 1"
        );

        int move = UciMoveConverter.toMoveInt(uci, position);

        assertEquals(Position.A2, Move.getSq1(move));
        assertEquals(Position.A1, Move.getSq2(move));
        assertEquals(Position.BLACK_PAWN, Move.getPiece(move));
        assertEquals(expectedPiece, Move.getPromPiece(move));
    }

    @Test
    void toMove_handlesPromotion() {
        Position position = new Position(
            "8/P7/8/8/8/8/8/4K2k w - - 0 1"
        );

        Move move = UciMoveConverter.toMove("a7a8q", position);

        assertEquals(Position.A7, Move.getSq1(move.encoded));
        assertEquals(Position.A8, Move.getSq2(move.encoded));
        assertEquals(
            Position.WHITE_QUEEN,
            Move.getPromPiece(move.encoded)
        );
    }

    // -------------------------------------------------------------------------
    // Castling
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
        "e1g1, 1",
        "e1c1, 2"
    })
    void toMoveInt_detectsWhiteCastling(String uci, int expectedCastle) {
        Position position = new Position(
            "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"
        );

        int move = UciMoveConverter.toMoveInt(uci, position);

        assertEquals(Position.WHITE_KING, Move.getPiece(move));
        assertEquals(expectedCastle, Move.getCastle(move));
    }

    @ParameterizedTest
    @CsvSource({
        "e8g8, 1",
        "e8c8, 2"
    })
    void toMoveInt_detectsBlackCastling(String uci, int expectedCastle) {
        Position position = new Position(
            "r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1"
        );

        int move = UciMoveConverter.toMoveInt(uci, position);

        assertEquals(Position.BLACK_KING, Move.getPiece(move));
        assertEquals(expectedCastle, Move.getCastle(move));
    }

    @Test
    void toMoveInt_detectsWhiteEnPassant() {
        Position position = new Position(
            "4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1"
        );

        int move = UciMoveConverter.toMoveInt("e5d6", position);

        assertEquals(Position.E5, Move.getSq1(move));
        assertEquals(Position.D6, Move.getSq2(move));
        assertEquals(Position.WHITE_PAWN, Move.getPiece(move));
        assertEquals(Position.NO_PIECE, Move.getCapPiece(move));
        assertTrue(Move.getEP(move) == 1);
    }

    @Test
    void toMoveInt_detectsBlackEnPassant() {
        Position position = new Position(
            "4k3/8/8/8/3Pp3/8/8/4K3 b - d3 0 1"
        );

        int move = UciMoveConverter.toMoveInt("e4d3", position);

        assertEquals(Position.E4, Move.getSq1(move));
        assertEquals(Position.D3, Move.getSq2(move));
        assertEquals(Position.BLACK_PAWN, Move.getPiece(move));
        assertEquals(Position.NO_PIECE, Move.getCapPiece(move));
        assertTrue(Move.getEP(move) == 1);
    }

    @Test
    void toMoveInt_doesNotMarkNormalPawnMoveAsEnPassant() {
        Position position = new Position();

        int move = UciMoveConverter.toMoveInt("e2e4", position);

        assertFalse(Move.getEP(move) == 1);
    }

    @ParameterizedTest
    @CsvSource({
        "''",
        "e2",
        "e2e",
        "e2e4q1",
        "i2e4",
        "e9e4",
        "e2i4",
        "e2e9",
        "E2e4",
        "e2E4"
    })
    void toMoveInt_rejectsInvalidUci(String uci) {
        Position position = new Position();

        assertThrows(
            IllegalArgumentException.class,
            () -> UciMoveConverter.toMoveInt(uci, position)
        );
    }

    @Test
    void toMoveInt_rejectsNull() {
        Position position = new Position();

        assertThrows(
            IllegalArgumentException.class,
            () -> UciMoveConverter.toMoveInt(null, position)
        );
    }

    @Test
    void toMoveInt_rejectsEmptySourceSquare() {
        Position position = new Position(
            "4k3/8/8/8/8/8/8/4K3 w - - 0 1"
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> UciMoveConverter.toMoveInt("a2a3", position)
        );

        assertTrue(
            exception.getMessage().contains("No piece on source square")
        );
    }

    @ParameterizedTest
    @CsvSource({
        "a7a8k",
        "a7a8p",
        "a7a8x",
        "a7a8q1"
    })
    void toMoveInt_rejectsInvalidPromotion(String uci) {
        Position position = new Position(
            "8/P7/8/8/8/8/8/4K2k w - - 0 1"
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> UciMoveConverter.toMoveInt(uci, position)
        );
    }

    // -------------------------------------------------------------------------
    // Round-trip tests
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
        "e2e4",
        "g1f3",
        "b1c3",
        "a2a4"
    })
    void notationRoundTrip(String uci) {
        Position position = new Position();

        int encoded = UciMoveConverter.toMoveInt(uci, position);

        assertEquals(
            uci,
            UciMoveConverter.toNotation(encoded)
        );
    }

    @Test
    void promotionNotationRoundTrip() {
        Position position = new Position(
            "8/P7/8/8/8/8/8/4K2k w - - 0 1"
        );

        String uci = "a7a8q";

        int encoded = UciMoveConverter.toMoveInt(uci, position);

        assertEquals(
            uci,
            UciMoveConverter.toNotation(encoded)
        );
    }

    @Test
    void castlingNotationRoundTrip() {
        Position position = new Position(
            "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"
        );

        assertEquals(
            "e1g1",
            UciMoveConverter.toNotation(
                UciMoveConverter.toMoveInt("e1g1", position)
            )
        );

        assertEquals(
            "e1c1",
            UciMoveConverter.toNotation(
                UciMoveConverter.toMoveInt("e1c1", position)
            )
        );
    }

    @ParameterizedTest
    @CsvSource({
        "d5e6, 5k2/2R2p2/3p1bpp/3Pp3/1N1n2P1/r6P/P4PK1/3B4 w - e6 0 36",
        "d5e6, rnbq1rk1/ppp2pbp/3p1np1/3Pp3/2P1P3/2N2NPP/PP3PB1/R1BQ1RK1 w - e6 0 11",
        "a5b6, r4rk1/p1qbbpp1/2pp1nnp/Pp2p3/2PPP3/2N1BN1P/1PQ1BPP1/R2R2K1 w - b6 0 14"
    })
    void encounteredIssues(String move, String fen) {
        Position position = new Position(fen);
        int moveInt = UciMoveConverter.toMoveInt(move, position);
        assert(position.isMoveLegal(moveInt));
    }
}