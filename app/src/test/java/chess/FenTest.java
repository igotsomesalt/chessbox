package chess;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        String fen = "r3k2r/ppp2ppp/2n1b3/8/8/2N1B3/PPP2PPP/R3K2R b KQkq e3 12 25";

        Position position = Fen.fromFen(fen);

        assertEquals(false, position.whiteToMove);
        assertEquals(20, position.epSq);
        assertEquals(0xf, position.castlingRights);

        assertEquals(fen, Fen.toFen(position));
    }

    @Test
    void castlingRightsAreParsedCorrectly() {
        Position position = Fen.fromFen("8/8/8/8/8/8/8/4K2k w Kq - 0 1");

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

    @Test
    void testEmptyBoard() {
        Position position = new Position("8/8/8/8/8/8/8/8 w - - 0 1");

        for (int square = 0; square < 64; square++) {
            assertEquals(Position.NO_PIECE, position.getPieceAt(square));
        }

        assertTrue(position.whiteToMove);
        assertEquals(-1, position.epSq);
        assertEquals(0, position.halfMoveClock);
        assertEquals(1, position.fullMoveClock);
    }

    @Test
    void testOnlyKings() {
        Position position = new Position("4k3/8/8/8/8/8/8/4K3 w - - 0 1");

        assertEquals(Position.BLACK_KING, position.getPieceAt(60)); // e8
        assertEquals(Position.WHITE_KING, position.getPieceAt(4));  // e1

        assertTrue(position.whiteToMove);
    }

    @Test
    void testSideToMoveBlack() {
        Position position = new Position("4k3/8/8/8/8/8/8/4K3 b - - 12 37");

        assertFalse(position.whiteToMove);
        assertEquals(12, position.halfMoveClock);
        assertEquals(37, position.fullMoveClock);
    }

    @Test
    void testAllPieceTypes() {
        Position position = new Position(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1"
        );

        // White
        assertEquals(Position.WHITE_ROOK, position.getPieceAt(0));   // a1
        assertEquals(Position.WHITE_KNIGHT, position.getPieceAt(1)); // b1
        assertEquals(Position.WHITE_BISHOP, position.getPieceAt(2)); // c1
        assertEquals(Position.WHITE_QUEEN, position.getPieceAt(3));  // d1
        assertEquals(Position.WHITE_KING, position.getPieceAt(4));   // e1
        assertEquals(Position.WHITE_BISHOP, position.getPieceAt(5)); // f1
        assertEquals(Position.WHITE_KNIGHT, position.getPieceAt(6)); // g1
        assertEquals(Position.WHITE_ROOK, position.getPieceAt(7));   // h1

        // Black
        assertEquals(Position.BLACK_ROOK, position.getPieceAt(56));   // a8
        assertEquals(Position.BLACK_KNIGHT, position.getPieceAt(57)); // b8
        assertEquals(Position.BLACK_BISHOP, position.getPieceAt(58)); // c8
        assertEquals(Position.BLACK_QUEEN, position.getPieceAt(59));  // d8
        assertEquals(Position.BLACK_KING, position.getPieceAt(60));   // e8
        assertEquals(Position.BLACK_BISHOP, position.getPieceAt(61)); // f8
        assertEquals(Position.BLACK_KNIGHT, position.getPieceAt(62)); // g8
        assertEquals(Position.BLACK_ROOK, position.getPieceAt(63));   // h8
    }

    @Test
    void testPawnRanks() {
        Position position = new Position(
                "8/pppppppp/8/8/8/8/PPPPPPPP/8 w - - 0 1"
        );

        for (int square = 8; square < 16; square++) {
            assertEquals(Position.WHITE_PAWN, position.getPieceAt(square));
        }

        for (int square = 48; square < 56; square++) {
            assertEquals(Position.BLACK_PAWN, position.getPieceAt(square));
        }
    }

    @Test
    void testCastlingRightsAll() {
        Position position = new Position(
                "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"
        );

        assertTrue(position.getCastlingRights(true, true));
        assertTrue(position.getCastlingRights(false, true));
        assertTrue(position.getCastlingRights(true, false));
        assertTrue(position.getCastlingRights(false, false));
    }

    @Test
    void testNoCastlingRights() {
        Position position = new Position(
                "r3k2r/8/8/8/8/8/8/R3K2R w - - 0 1"
        );

        assertFalse(position.getCastlingRights(true, true));
        assertFalse(position.getCastlingRights(false, true));
        assertFalse(position.getCastlingRights(true, false));
        assertFalse(position.getCastlingRights(false, false));
    }

    @Test
    void testWhiteCastlingRightsOnly() {
        Position position = new Position(
                "r3k2r/8/8/8/8/8/8/R3K2R w KQ - 0 1"
        );

        assertTrue(position.getCastlingRights(true, true));
        assertTrue(position.getCastlingRights(false, true));
        assertFalse(position.getCastlingRights(true, false));
        assertFalse(position.getCastlingRights(false, false));
    }

    @Test
    void testBlackCastlingRightsOnly() {
        Position position = new Position(
                "r3k2r/8/8/8/8/8/8/R3K2R b kq - 0 1"
        );

        assertFalse(position.getCastlingRights(true, true));
        assertFalse(position.getCastlingRights(false, true));
        assertTrue(position.getCastlingRights(true, false));
        assertTrue(position.getCastlingRights(false, false));
    }

    @Test
    void testEnPassantSquare() {
        Position position = new Position(
                "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2"
        );

        // e6 = rank 6, file e = square 44 with A1 = 0
        assertEquals(44, position.epSq);
    }

    @Test
    void testNoEnPassantSquare() {
        Position position = new Position(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        );

        assertEquals(-1, position.epSq);
    }

    @Test
    void testHalfmoveAndFullmoveCounters() {
        Position position = new Position(
                "8/8/8/8/8/8/8/4K2k b - - 87 143"
        );

        assertEquals(87, position.halfMoveClock);
        assertEquals(143, position.fullMoveClock);
    }

    @Test
    void testComplexPosition() {
        Position position = new Position(
            "r3k2r/pppq1ppp/2np1n2/8/2B1P3/2N2Q2/PPP2PPP/R3K2R w KQkq - 5 12"
        );

        assertTrue(position.whiteToMove);

        assertTrue(position.getCastlingRights(true, true));
        assertTrue(position.getCastlingRights(false, true));
        assertTrue(position.getCastlingRights(true, false));
        assertTrue(position.getCastlingRights(false, false));

        assertEquals(5, position.halfMoveClock);
        assertEquals(12, position.fullMoveClock);

        // White pieces
        assertEquals(Position.WHITE_KING, position.getPieceAt(4));  // e1
        assertEquals(Position.WHITE_QUEEN, position.getPieceAt(21)); // f3
        assertEquals(Position.WHITE_BISHOP, position.getPieceAt(26)); // c4
        assertEquals(Position.WHITE_KNIGHT, position.getPieceAt(18)); // c3

        // Black king/queen
        assertEquals(Position.BLACK_KING, position.getPieceAt(60)); // e8
        assertEquals(Position.BLACK_QUEEN, position.getPieceAt(51)); // d8
    }

    @Test
    void testSparsePosition() {
        Position position = new Position(
            "8/8/8/3k4/8/4Q3/8/4K3 w - - 0 1"
        );

        assertEquals(Position.WHITE_KING, position.getPieceAt(4));   // e1
        assertEquals(Position.WHITE_QUEEN, position.getPieceAt(20)); // e3
        assertEquals(Position.BLACK_KING, position.getPieceAt(35));  // d5

        // Everything else should be empty.
        int[] occupied = {4, 20, 35};

        for (int square = 0; square < 64; square++) {
            boolean shouldBeOccupied = false;

            for (int occupiedSquare : occupied) {
                if (square == occupiedSquare) {
                    shouldBeOccupied = true;
                    break;
                }
            }

            if (!shouldBeOccupied) {
                assertEquals(Position.NO_PIECE, position.getPieceAt(square));
            }
        }
    }

    @Test
    void toFenEmptyBoard() {
        Position position = new Position(
                new long[12],
                0,
                -1,
                true,
                0,
                1
        );

        assertEquals(
                "8/8/8/8/8/8/8/8 w - - 0 1",
                Fen.toFen(position)
        );
    }

    @Test
    void toFenOnlyKings() {
        long[] bitboards = new long[12];

        bitboards[Position.WHITE_KING] = 1L << 4;   // e1
        bitboards[Position.BLACK_KING] = 1L << 60;  // e8

        Position position = new Position(
                bitboards,
                0,
                -1,
                true,
                0,
                1
        );

        assertEquals(
                "4k3/8/8/8/8/8/8/4K3 w - - 0 1",
                Fen.toFen(position)
        );
    }

    @Test
    void toFenAllPieceTypes() {
        long[] bitboards = new long[12];

        // White pieces
        bitboards[Position.WHITE_PAWN]   = 1L << 8;   // a2
        bitboards[Position.WHITE_KNIGHT] = 1L << 9;   // b2
        bitboards[Position.WHITE_BISHOP] = 1L << 10;  // c2
        bitboards[Position.WHITE_ROOK]   = 1L << 11;  // d2
        bitboards[Position.WHITE_QUEEN]  = 1L << 12;  // e2
        bitboards[Position.WHITE_KING]   = 1L << 13;  // f2

        // Black pieces
        bitboards[Position.BLACK_PAWN]   = 1L << 48;  // a7
        bitboards[Position.BLACK_KNIGHT] = 1L << 49;  // b7
        bitboards[Position.BLACK_BISHOP] = 1L << 50;  // c7
        bitboards[Position.BLACK_ROOK]   = 1L << 51;  // d7
        bitboards[Position.BLACK_QUEEN]  = 1L << 52;  // e7
        bitboards[Position.BLACK_KING]   = 1L << 53;  // f7

        Position position = new Position(
                bitboards,
                0,
                -1,
                true,
                0,
                1
        );

        assertEquals(
                "8/pnbrqk2/8/8/8/8/PNBRQK2/8 w - - 0 1",
                Fen.toFen(position)
        );
    }

    @Test
    void toFenCompressesEmptySquares() {
        long[] bitboards = new long[12];

        bitboards[Position.WHITE_KING] = 1L << 4;  // e1
        bitboards[Position.BLACK_KING] = 1L << 60; // e8

        Position position = new Position(
                bitboards,
                0,
                -1,
                true,
                0,
                1
        );

        String fen = Fen.toFen(position);

        assertEquals(
                "4k3/8/8/8/8/8/8/4K3 w - - 0 1",
                fen
        );
    }

    @Test
    void toFenCompressesMultipleEmptyRuns() {
        long[] bitboards = new long[12];

        // a8 and h8
        bitboards[Position.BLACK_ROOK] =
                (1L << 56) | (1L << 63);

        // a1 and h1
        bitboards[Position.WHITE_ROOK] =
                (1L << 0) | (1L << 7);

        Position position = new Position(
                bitboards,
                0,
                -1,
                true,
                0,
                1
        );

        assertEquals(
                "r6r/8/8/8/8/8/8/R6R w - - 0 1",
                Fen.toFen(position)
        );
    }

    @Test
    void toFenBlackToMove() {
        Position position = new Position(
                Position.DEFAULT_BITBOARDS,
                Position.DEFAULT_CASTLING,
                -1,
                false,
                0,
                1
        );

        assertEquals(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1",
                Fen.toFen(position)
        );
    }

    @Test
    void toFenAllCastlingRights() {
        Position position = new Position(
                Position.DEFAULT_BITBOARDS,
                0xf,
                -1,
                true,
                0,
                1
        );

        assertEquals(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                Fen.toFen(position)
        );
    }

    @Test
    void toFenNoCastlingRights() {
        Position position = new Position(
                Position.DEFAULT_BITBOARDS,
                0,
                -1,
                true,
                0,
                1
        );

        assertEquals(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1",
                Fen.toFen(position)
        );
    }

    @Test
    void toFenEachIndividualCastlingRight() {
        Position base = new Position(
                Position.DEFAULT_BITBOARDS,
                0,
                -1,
                true,
                0,
                1
        );

        Position whiteKingSide = new Position(base);
        whiteKingSide.castlingRights = 0b0001;

        Position whiteQueenSide = new Position(base);
        whiteQueenSide.castlingRights = 0b0010;

        Position blackKingSide = new Position(base);
        blackKingSide.castlingRights = 0b0100;

        Position blackQueenSide = new Position(base);
        blackQueenSide.castlingRights = 0b1000;

        assertEquals(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w K - 0 1",
                Fen.toFen(whiteKingSide)
        );

        assertEquals(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w Q - 0 1",
                Fen.toFen(whiteQueenSide)
        );

        assertEquals(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w k - 0 1",
                Fen.toFen(blackKingSide)
        );

        assertEquals(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w q - 0 1",
                Fen.toFen(blackQueenSide)
        );
    }

    @Test
    void toFenEnPassantSquare() {
        Position position = new Position(
                Position.DEFAULT_BITBOARDS,
                Position.DEFAULT_CASTLING,
                44, // e6
                true,
                0,
                2
        );

        assertEquals(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq e6 0 2",
                Fen.toFen(position)
        );
    }

    @Test
    void toFenDifferentEnPassantSquares() {
        Position position = new Position(
                Position.DEFAULT_BITBOARDS,
                0,
                20, // e3
                true,
                0,
                1
        );

        assertEquals(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - e3 0 1",
                Fen.toFen(position)
        );
    }

    @Test
    void toFenHalfmoveAndFullmoveCounters() {
        Position position = new Position(
                Position.DEFAULT_BITBOARDS,
                Position.DEFAULT_CASTLING,
                -1,
                false,
                87,
                143
        );

        assertEquals(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 87 143",
                Fen.toFen(position)
        );
    }

    @Test
    void toFenLargeCounters() {
        Position position = new Position(
                Position.DEFAULT_BITBOARDS,
                0,
                -1,
                true,
                999,
                5000
        );

        assertEquals(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 999 5000",
                Fen.toFen(position)
        );
    }

    @Test
    void toFenPiecePlacementAcrossRanks() {
        long[] bitboards = new long[12];

        // One piece on every rank, different file.
        bitboards[Position.WHITE_ROOK]   |= 1L << 0;   // a1
        bitboards[Position.WHITE_KNIGHT] |= 1L << 9;   // b2
        bitboards[Position.WHITE_BISHOP] |= 1L << 18;  // c3
        bitboards[Position.WHITE_QUEEN]  |= 1L << 27;  // d4
        bitboards[Position.WHITE_KING]   |= 1L << 36;  // e5
        bitboards[Position.BLACK_BISHOP] |= 1L << 45;  // f6
        bitboards[Position.BLACK_KNIGHT] |= 1L << 54;  // g7
        bitboards[Position.BLACK_ROOK]   |= 1L << 63;  // h8

        Position position = new Position(
                bitboards,
                0,
                -1,
                true,
                0,
                1
        );

        assertEquals(
                "7r/6n1/5b2/4K3/3Q4/2B5/1N6/R7 w - - 0 1",
                Fen.toFen(position)
        );
    }

    @Test
    void toFenMixedPiecesAndEmptyRuns() {
        long[] bitboards = new long[12];

        bitboards[Position.WHITE_KING] = 1L << 4;      // e1
        bitboards[Position.WHITE_QUEEN] = 1L << 27;    // d4
        bitboards[Position.WHITE_ROOK] = 1L << 0;      // a1
        bitboards[Position.WHITE_PAWN] = 1L << 8;      // a2

        bitboards[Position.BLACK_KING] = 1L << 60;     // e8
        bitboards[Position.BLACK_QUEEN] = 1L << 35;    // d5
        bitboards[Position.BLACK_ROOK] = 1L << 63;     // h8
        bitboards[Position.BLACK_PAWN] = 1L << 55;     // h7

        Position position = new Position(
                bitboards,
                0xf,
                44,
                false,
                17,
                29
        );

        assertEquals(
                "4k2r/7p/8/3q4/3Q4/8/P7/R3K3 b KQkq e6 17 29",
                Fen.toFen(position)
        );
    }

}
