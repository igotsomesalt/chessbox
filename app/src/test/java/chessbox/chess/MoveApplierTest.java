package chessbox.chess;

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
        Position position = new Position("4k3/8/8/8/8/3p4/4P3/4K3 w - - 0 1");

        String originalFen = position.toFen();

        int move = findMove(position, "e2d3");

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(Position.WHITE_PAWN, position.getPieceAt(Position.D3));
        assertEquals(Position.NO_PIECE, position.getPieceAt(Position.D4));

        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakeEnPassant() {
        Position position = new Position("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1");

        String originalFen = position.toFen();

        int move = findMove(position, "e5d6");

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(Position.WHITE_PAWN, position.getPieceAt(Position.D6));
        assertEquals(Position.NO_PIECE, position.getPieceAt(Position.D5));

        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakePromotion() {
        Position position = new Position("4k3/P7/8/8/8/8/8/4K3 w - - 0 1");

        String originalFen = position.toFen();

        int move = findMove(position, "a7a8q");

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(Position.WHITE_QUEEN, position.getPieceAt(Position.A8));
        assertEquals(Position.NO_PIECE, position.getPieceAt(Position.A7));

        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakePromotionCapture() {
        Position position = new Position("1r2k3/P7/8/8/8/8/8/4K3 w - - 0 1");

        String originalFen = position.toFen();

        int move = findMove(position, "a7b8q");

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(Position.WHITE_QUEEN, position.getPieceAt(Position.B8));
        assertEquals(Position.NO_PIECE, position.getPieceAt(Position.A7));

        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakeKingSideCastle() {
        Position position = new Position("4k2r/8/8/8/8/8/8/4K2R w K - 0 1");

        String originalFen = position.toFen();

        int move = findMove(position, "e1g1");

        

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(Position.WHITE_KING, position.getPieceAt(Position.G1));
        assertEquals(Position.WHITE_ROOK, position.getPieceAt(Position.F1));

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

        assertEquals(Position.WHITE_KING, position.getPieceAt(Position.C1));
        assertEquals(Position.WHITE_ROOK, position.getPieceAt(Position.D1));

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
            "4k3/8/8/8/3p4/8/4P3/4K3 w - - 0 1"
        );

        int move = findMove(position, "e2e4");

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(Position.E3, position.epSq);

        MoveApplier.undo(position, state);

        assertEquals(-1, position.epSq);
    }

    @Test
    void makeUnmakePreservesMoveCounters() {
        Position position = new Position("4k3/8/8/8/8/8/4N3/4K3 w - - 17 23");

        String originalFen = position.toFen();

        int move = findMove(position, "e2f4");

        MoveApplier.State state = MoveApplier.apply(position, move);
        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakeBlackMove() {
        Position position = new Position(
            "4k3/8/8/8/8/4p3/8/4K3 b - - 0 1"
        );

        String originalFen = position.toFen();

        int move = findMove(position, "e3e2");

        MoveApplier.State state = MoveApplier.apply(position, move);
        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakeBlackEnPassant() {
        Position position = new Position("4k3/8/8/3pP3/8/8/8/4K3 b - e4 0 1");

        String originalFen = position.toFen();

        int move = findMove(position, "d5e4");

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(Position.BLACK_PAWN, position.getPieceAt(Position.E4));
        assertEquals(Position.NO_PIECE, position.getPieceAt(Position.D5));

        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakeRookCaptureRestoresCastlingRights() {
        Position position = new Position(
            "r3k2r/8/8/8/8/8/7Q/R3K2R w KQkq - 0 1"
        );

        String originalFen = position.toFen();

        int move = findMove(position, "h2h8");

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(Position.WHITE_QUEEN, position.getPieceAt(Position.H8));
        assertEquals(Position.NO_PIECE, position.getPieceAt(Position.H7));

        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakeRookCaptureRestoresBlackKingSideCastling() {
        Position position = new Position(
            "4k2r/8/8/8/8/8/7Q/4K3 w k - 0 1"
        );

        String originalFen = position.toFen();

        int move = findMove(position, "h2h8");

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(Position.WHITE_QUEEN, position.getPieceAt(Position.H8));

        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakeCaptureA8RookRestoresCastlingRights() {
        Position position = new Position(
            "r3k3/8/8/8/8/8/Q7/4K3 w q - 0 1"
        );

        String originalFen = position.toFen();

        int move = findMove(position, "a2a8");

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(Position.WHITE_QUEEN, position.getPieceAt(Position.A8));

        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void makeUnmakeDoublePawnPushRestoresEverything() {
        Position position = new Position();

        String originalFen = position.toFen();

        int move = findMove(position, "e2e4");

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
            position.toFen()
        );

        MoveApplier.undo(position, state);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void normalMoveClearsEnPassantSquare() {
        Position position = new Position(
            "4k3/8/8/8/8/8/4P3/4K3 w - e3 0 1"
        );

        int move = findMove(position, "e2e3");

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(-1, position.epSq);

        MoveApplier.undo(position, state);

        assertEquals(Position.E3, position.epSq);
    }

    @Test
    void enPassantCaptureClearsEnPassantSquare() {
        Position position = new Position(
            "4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1"
        );

        int move = findMove(position, "e5d6");

        MoveApplier.State state = MoveApplier.apply(position, move);

        assertEquals(-1, position.epSq);

        MoveApplier.undo(position, state);

        assertEquals(Position.D6, position.epSq);
    }

    @Test
    void nestedMakeUnmake() {
        Position position = new Position();

        String originalFen = position.toFen();

        int move1 = findMove(position, "e2e4");
        MoveApplier.State state1 = MoveApplier.apply(position, move1);

        int move2 = findMove(position, "e7e5");
        MoveApplier.State state2 = MoveApplier.apply(position, move2);

        int move3 = findMove(position, "g1f3");
        MoveApplier.State state3 = MoveApplier.apply(position, move3);

        MoveApplier.undo(position, state3);

        assertEquals(
            "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2",
            position.toFen()
        );

        MoveApplier.undo(position, state2);

        assertEquals(
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
            position.toFen()
        );

        MoveApplier.undo(position, state1);

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void nestedCaptureMakeUnmake() {
        Position position = new Position(
            "4k3/8/8/8/3p4/8/4P3/4K3 w - - 0 1"
        );

        String originalFen = position.toFen();

        int move1 = findMove(position, "e2e4");
        MoveApplier.State state1 = MoveApplier.apply(position, move1);

        int move2 = findMove(position, "d4e3");
        MoveApplier.State state2 = MoveApplier.apply(position, move2);

        MoveApplier.undo(position, state2);
        MoveApplier.undo(position, state1);

        assertEquals(originalFen, position.toFen());
    }

    private void assertEveryMoveUnmakes(String fen) {
        Position position = new Position(fen);
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
                    + "\nFEN: "
                    + fen
            );
        }
    }

    @Test
    void makeUnmakeVariousPositions() {
        assertEveryMoveUnmakes(
            "r3k2r/p1ppqpb1/bn2pnp1/2pP4/1p2P3/2N2N2/PPQ1BPPP/R3K2R w KQkq - 0 1"
        );

        assertEveryMoveUnmakes(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        );

        assertEveryMoveUnmakes(
            "4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1"
        );

        assertEveryMoveUnmakes(
            "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"
        );

        assertEveryMoveUnmakes(
            "4k3/P7/8/8/8/8/8/4K3 w - - 0 1"
        );
    }

    @Test
    void makeUnmakeThreePly() {
        Position position = new Position();

        String originalFen = position.toFen();

        int[] moves1 = MoveGenerator.generateLegalMoves(position);

        for (int move1 : moves1) {
            MoveApplier.State state1 = MoveApplier.apply(position, move1);

            int[] moves2 = MoveGenerator.generateLegalMoves(position);

            for (int move2 : moves2) {
                MoveApplier.State state2 = MoveApplier.apply(position, move2);

                int[] moves3 = MoveGenerator.generateLegalMoves(position);

                for (int move3 : moves3) {
                    MoveApplier.State state3 = MoveApplier.apply(position, move3);
                    MoveApplier.undo(position, state3);
                }

                MoveApplier.undo(position, state2);
            }

            MoveApplier.undo(position, state1);
        }

        assertEquals(originalFen, position.toFen());
    }

    @Test
    void testDoublePawnPushEp() {
        Position position = new Position();

        int move = UciMoveConverter.toMoveInt( "d2d3", position);

        MoveApplier.State state = MoveApplier.apply(position, move);

        System.out.println(position.toFen());

        MoveApplier.undo(position, state);
    }

    @Test
    void testEpAfterB5() {
        Position position = new Position();

        int d3 = UciMoveConverter.toMoveInt("d2d3", position);
        MoveApplier.State s1 = MoveApplier.apply(position, d3);

        int b5 = UciMoveConverter.toMoveInt("b7b5", position);
        MoveApplier.State s2 = MoveApplier.apply(position, b5);

        System.out.println(position.toFen());

        MoveApplier.undo(position, s2);
        MoveApplier.undo(position, s1);
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