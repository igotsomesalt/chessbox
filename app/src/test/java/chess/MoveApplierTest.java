package chess;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveApplierTest {

    @Test
    void makeUnmakeStartingPosition() {
        Position position = new Position();
        String originalFen = position.toFen();

        int[] moves = MoveGenerator.generateLegalMoves(position);

        for (int move : moves) {
            MoveApplier.State state = MoveApplier.apply(position, move);
            MoveApplier.undo(position, state);

            assertEquals(
                originalFen,
                position.toFen(),
                "Position changed after make/unmake: " + UciMoveConverter.toNotation(move)
            );
        }
    }

    @Test
    void makeUnmakeKiwipete() {
        Position position = new Position(
            "r3k2r/p1ppqpb1/bn2pnp1/2pP4/1p2P3/2N2N2/PPQ1BPPP/R3K2R w KQkq - 0 1"
        );

        String originalFen = position.toFen();

        int[] moves = MoveGenerator.generateLegalMoves(position);

        for (int move : moves) {
            MoveApplier.State state = MoveApplier.apply(position, move);
            MoveApplier.undo(position, state);

            assertEquals(
                originalFen,
                position.toFen(),
                "Position changed after make/unmake: "
                    + UciMoveConverter.toNotation(move)
            );
        }
    }

    @Test
    void makeUnmakeQuietMove() {
        Position position = new Position();

        int move = findMove(position, "e2e4");

        String originalFen = position.toFen();

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            originalFen
        );

        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakeCapture() {
        Position position = new Position("4k3/8/8/8/3p4/8/4P3/4K3 w - - 0 1");

        String originalFen = position.toFen();

        int move = findMove(position, "e2d3");

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(Position.WHITE_PAWN, position.getPieceAt(Bitboard.D3));
        assertEquals(Position.NO_PIECE, position.getPieceAt(Bitboard.D4));

        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakeEnPassant() {
        Position position = new Position("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1");

        String originalFen = position.toFen();

        int move = findMove(position, "e5d6");

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(Position.WHITE_PAWN, position.getPieceAt(Bitboard.D6));
        assertEquals(Position.NO_PIECE, position.getPieceAt(Bitboard.D5));

        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakePromotion() {
        Position position = new Position("4k3/P7/8/8/8/8/8/4K3 w - - 0 1");

        String originalFen = position.toFen();

        int move = findMove(position, "a7a8q");

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(Position.WHITE_QUEEN, position.getPieceAt(Bitboard.A8));
        assertEquals(Position.NO_PIECE, position.getPieceAt(Bitboard.A7));

        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakePromotionCapture() {
        Position position = new Position("1r2k3/P7/8/8/8/8/8/4K3 w - - 0 1");

        String originalFen = position.toFen();

        int move = findMove(position, "a7b8q");

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(Position.WHITE_QUEEN, position.getPieceAt(Bitboard.B8));
        assertEquals(Position.NO_PIECE, position.getPieceAt(Bitboard.A7));

        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakeKingSideCastle() {
        Position position = new Position("4k2r/8/8/8/8/8/8/4K2R w K - 0 1");

        String originalFen = position.toFen();

        int move = findMove(position, "e1g1");

        

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(Position.WHITE_KING, position.getPieceAt(Bitboard.G1));
        assertEquals(Position.WHITE_ROOK, position.getPieceAt(Bitboard.F1));

        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakeQueenSideCastle() {
        Position position = new Position(
            "r3k3/8/8/8/8/8/8/R3K3 w Q - 0 1"
        );

        String originalFen = position.toFen();

        int move = findMove(position, "e1c1");

        

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(Position.WHITE_KING, position.getPieceAt(Bitboard.C1));
        assertEquals(Position.WHITE_ROOK, position.getPieceAt(Bitboard.D1));

        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakePreservesCastlingRights() {
        Position position = new Position(
            "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"
        );

        String originalFen = position.toFen();

        int move = findMove(position, "e1f1");

        MoveApplier.State state = MoveApplier.apply(position, move);
        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakePreservesEnPassantSquare() {
        Position position = new Position(
            "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1"
        );

        int move = findMove(position, "e2e4");

        MoveApplier.State state = MoveApplier.apply(position, move);
        assertEquals(Bitboard.E3, position.epSq);
        MoveApplier.undo(position, state);
        assertEquals(-1, position.epSq);
    }

    @Test
    void makeUnmakePreservesMoveCounters() {
        Position position = new Position("4k3/8/8/8/8/8/4N3/4K3 w - - 17 23");

        String originalFen = position.toFen();

        int move = findMove(position, "g1f3");

        MoveApplier.State state = MoveApplier.apply(position, move);
        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    private int findMove(Position position, String notation) {
        int[] moves = MoveGenerator.generateLegalMoves(position);

        for (int move : moves) {
            if (UciMoveConverter.toNotation(move).equals(notation)) {
                return move;
            }
        }

        throw new AssertionError(
            "Move was not generated: " + notation
        );
    }
}