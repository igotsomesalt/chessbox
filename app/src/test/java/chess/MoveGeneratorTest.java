package chess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveGeneratorTest {

    @ParameterizedTest
    @CsvSource({
        // Starting position
        "'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1', 20",

        // After 1. e4
        "'rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1', 20",

        // Kiwipete
        "'r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1', 48"
    })
    void legalMoveCount(String fen, int expected) {
        Position position = new Position(fen);

        int[] moves = MoveGenerator.generateLegalMoves(position);

        assertEquals(expected, moves.length, () ->
            "FEN: " + fen + "\n" +
            "Moves: " + movesToString(moves)
        );
    }

    @Test
    void startingPositionContainsExpectedMoves() {
        Position position = new Position(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        );

        int[] moves = MoveGenerator.generateLegalMoves(position);

        Set<String> actual = toUciSet(moves);

        String[] expected = {
            "a2a3", "a2a4",
            "b2b3", "b2b4",
            "c2c3", "c2c4",
            "d2d3", "d2d4",
            "e2e3", "e2e4",
            "f2f3", "f2f4",
            "g2g3", "g2g4",
            "h2h3", "h2h4",

            "b1a3", "b1c3",
            "g1f3", "g1h3"
        };

        assertEquals(
            Set.of(expected),
            actual
        );
    }

    @Test
    void enPassantIsGenerated() {
        Position position = new Position("k7/8/8/3pP3/8/8/8/K7 w - d6 0 1");

        int[] moves = MoveGenerator.generateLegalMoves(position);

        Set<String> actual = toUciSet(moves);

        assertTrue(
            actual.contains("e5d6"),
            "Expected en passant move e5d6, got: " + actual
        );
    }

    @Test
    void enPassantIsNotGeneratedWithoutEpSquare() {
        Position position = new Position(
            "8/8/8/3pP3/8/8/8/8 w - - 0 1"
        );

        int[] moves = MoveGenerator.generateLegalMoves(position);

        Set<String> actual = toUciSet(moves);

        assertTrue(
            !actual.contains("e5d6"),
            "Unexpected en passant move: " + actual
        );
    }

    @Test
    void promotionsGenerateFourMoves() {
        Position position = new Position(
            "7k/P7/8/8/8/8/8/7K w - - 0 1"
        );

        int[] moves = MoveGenerator.generateLegalMoves(position);

        Set<String> actual = toUciSet(moves);

        assertTrue(actual.contains("a7a8n"));
        assertTrue(actual.contains("a7a8b"));
        assertTrue(actual.contains("a7a8r"));
        assertTrue(actual.contains("a7a8q"));
    }

    @Test
    void castlingGeneratesBothSides() {
        Position position = new Position(
            "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"
        );

        int[] moves = MoveGenerator.generateLegalMoves(position);

        Set<String> actual = toUciSet(moves);

        assertTrue(
            actual.contains("e1g1"),
            "Expected white kingside castling"
        );

        assertTrue(
            actual.contains("e1c1"),
            "Expected white queenside castling"
        );
    }

    @Test
    void cannotCastleThroughCheck() {
        Position position = new Position(
            "r3kr1r/8/8/8/8/8/8/R3K2R w KQ - 0 1"
        );

        int[] moves = MoveGenerator.generateLegalMoves(position);

        Set<String> actual = toUciSet(moves);

        assertTrue(
            !actual.contains("e1g1"),
            "King should not be able to castle through f1"
        );
    }

    @Test
    void kingCannotMoveIntoCheck() {
        Position position = new Position("4r1k1/8/8/8/8/8/8/4K3 w - - 0 1");

        int[] moves = MoveGenerator.generateLegalMoves(position);

        Set<String> actual = toUciSet(moves);

        assertTrue(
            !actual.contains("e1e2"),
            "King should not be able to move into check"
        );
    }

    @Test
    void pinnedPieceCannotMove() {
        Position position = new Position(
            "4r1k1/8/8/8/8/8/4R3/4K3 w - - 0 1"
        );

        int[] moves = MoveGenerator.generateLegalMoves(position);

        Set<String> actual = toUciSet(moves);

        assertTrue(
            !actual.contains("e2a2"),
            "Pinned rook should not be able to move off the e-file"
        );

        assertTrue(
            !actual.contains("e2h2"),
            "Pinned rook should not be able to move off the e-file"
        );
    }

    @Test
    void capturesAreGenerated() {
        Position position = new Position("4k3/8/8/3p4/4P3/8/8/4K3 w - - 0 1");

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(actual.contains("e4d5"));
    }

    @Test
    void kingCannotCaptureProtectedPiece() {
        Position position = new Position("4r1k1/8/8/8/8/8/4p3/4K3 w - - 0 1");

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(!actual.contains("e1e2"));
    }

    @Test
    void rookCheckRequiresEvasion() {
        Position position = new Position(
            "4r1k1/8/8/8/8/8/8/4K3 w - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(actual.contains("e1f1"));
        assertTrue(actual.contains("e1d1"));
    }

    @Test
    void pinnedPieceCanMoveAlongPin() {
        Position position = new Position(
            "4r1k1/8/8/8/8/8/4R3/4K3 w - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(actual.contains("e2e3"));
        assertTrue(actual.contains("e2e4"));
        assertTrue(actual.contains("e2e5"));
        assertTrue(actual.contains("e2e6"));
        assertTrue(actual.contains("e2e7"));
    }

    @Test
    void pinnedKnightCannotMove() {
        Position position = new Position(
            "4r1k1/8/8/8/8/8/4N3/4K3 w - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(actual.stream().noneMatch(m -> m.startsWith("e2")));
    }

    @Test
    void capturePromotionGeneratesFourMoves() {
        Position position = new Position(
            "1r5k/P7/8/8/8/8/8/7K w - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(actual.contains("a7b8n"));
        assertTrue(actual.contains("a7b8b"));
        assertTrue(actual.contains("a7b8r"));
        assertTrue(actual.contains("a7b8q"));
    }

    @Test
    void singleCheckHasOnlyLegalKingMoves() {
        Position position = new Position(
            "4r1k1/8/8/8/8/8/8/4K3 w - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertEquals(
            Set.of(
                "e1d1",
                "e1f1",
                "e1d2",
                "e1f2"
            ),
            actual
        );
    }

    @Test
    void pinnedBishopCannotMoveOffPin() {
        Position position = new Position(
            "4r1k1/8/8/8/8/8/4B3/4K3 w - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(actual.stream().noneMatch(m -> m.startsWith("d2")));
    }

    @Test
    void pinnedQueenCanOnlyMoveAlongPin() {
        Position position = new Position(
            "4r1k1/8/8/8/8/8/4Q3/4K3 w - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(actual.contains("e2e3"));
        assertTrue(actual.contains("e2e4"));
        assertTrue(actual.contains("e2e5"));
        assertTrue(actual.contains("e2e6"));
        assertTrue(actual.contains("e2e7"));

        assertTrue(!actual.contains("e2a2"));
        assertTrue(!actual.contains("e2h2"));
    }

    @Test
    void doubleCheckOnlyAllowsKingMoves() {
        Position position = new Position(
            "4r1k1/8/8/8/8/8/3bK3/8 w - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(
            actual.stream().allMatch(m -> m.startsWith("e2")),
            "Double check should only allow king moves: " + actual
        );
    }

    @Test
    void kingCanCaptureCheckingPieceIfSafe() {
        Position position = new Position(
            "4k3/8/8/8/8/8/4r3/4K3 w - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(actual.contains("e1e2"));
    }

    @Test
    void kingCannotCaptureCheckingPieceIfProtected() {
        Position position = new Position(
            "8/8/8/8/8/4k3/4r3/4K2B w - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(!actual.contains("e1e2"));
    }

    @Test
    void pieceCanBlockSlidingCheck() {
        Position position = new Position(
            "4r1k1/8/8/8/8/8/8/3BK3 w - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        // Bishop can move to block the rook's check.
        assertTrue(actual.contains("d1e2"));
    }

    @Test
    void pieceCanBlockRookCheck() {
        Position position = new Position(
            "4r1k1/8/8/8/8/3B4/8/4K3 w - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(actual.contains("d3e2"));
    }

    @Test
    void knightCheckCannotBeBlocked() {
        Position position = new Position(
            "4k3/8/8/8/5n2/8/8/4K3 w - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(
            actual.stream().allMatch(m -> m.startsWith("e1")),
            "Knight check can only be answered by king movement/capture"
        );
    }

    @Test
    void pawnCheckCannotBeBlocked() {
        Position position = new Position(
            "4k3/8/8/8/8/3p4/4K3/8 w - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(
            actual.stream().allMatch(m -> m.startsWith("e2"))
        );
    }

    @Test
    void blackKingCannotMoveIntoCheck() {
        Position position = new Position(
            "4k3/8/8/8/8/8/4R3/8 b - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(!actual.contains("e8e7"));
    }

    @Test
    void blackEnPassantIsGenerated() {
        Position position = new Position(
            "k7/8/8/8/3pP3/8/8/K7 b - e3 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(
            actual.contains("d4e3"),
            "Expected black en passant: " + actual
        );
    }

    @Test
    void blackPromotionGeneratesFourMoves() {
        Position position = new Position(
            "7k/8/8/8/8/8/p7/7K b - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(actual.contains("a2a1n"));
        assertTrue(actual.contains("a2a1b"));
        assertTrue(actual.contains("a2a1r"));
        assertTrue(actual.contains("a2a1q"));
    }

    @Test
    void blackCapturePromotionGeneratesFourMoves() {
        Position position = new Position(
            "7k/8/8/8/8/8/p7/1R5K b - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(actual.contains("a2b1n"));
        assertTrue(actual.contains("a2b1b"));
        assertTrue(actual.contains("a2b1r"));
        assertTrue(actual.contains("a2b1q"));
    }

    @Test
    void cannotCastleWhileInCheck() {
        Position position = new Position(
            "r3k2r/8/8/8/8/8/4r3/R3K2R w KQkq - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(!actual.contains("e1g1"));
        assertTrue(!actual.contains("e1c1"));
    }

    @Test
    void cannotCastleIntoCheck() {
        Position position = new Position(
            "r3k2r/8/8/8/8/8/6r1/R3K2R w KQkq - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(!actual.contains("e1g1"));
    }

    @Test
    void cannotCastleThroughOccupiedSquare() {
        Position position = new Position(
            "r3k2r/8/8/8/8/8/8/R3KN1R w KQkq - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(!actual.contains("e1g1"));
    }

    @Test
    void castlingRightsAreRequired() {
        Position position = new Position(
            "r3k2r/8/8/8/8/8/8/R3K2R w - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(!actual.contains("e1g1"));
        assertTrue(!actual.contains("e1c1"));
    }

    @Test
    void enPassantCannotExposeKingToCheck() {
        Position position = new Position(
            "4r1k1/8/8/3pP3/8/8/8/4K3 w - d6 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertTrue(
            !actual.contains("e5d6"),
            "EP must not expose the king to the rook"
        );
    }

    @Test
    void promotionMovesAreExactlyCorrect() {
        Position position = new Position(
            "7k/P7/8/8/8/8/8/7K w - - 0 1"
        );

        Set<String> actual = toUciSet(
            MoveGenerator.generateLegalMoves(position)
        );

        assertEquals(
            Set.of(
                "a7a8n",
                "a7a8b",
                "a7a8r",
                "a7a8q",
                "h1g1",
                "h1g2",
                "h1h2"
            ),
            actual
        );
    }

    @Test
    void generatedMovesContainNoDuplicates() {
        Position position = new Position(
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1"
        );

        int[] moves = MoveGenerator.generateLegalMoves(position);

        Set<Integer> unique = new HashSet<>();

        for (int move : moves) {
            assertTrue(
                unique.add(move),
                "Duplicate move generated: " +
                UciMoveConverter.toNotation(move)
            );
        }
    }

    private static Set<String> toUciSet(int[] moves) {
        Set<String> result = new HashSet<>();

        for (int move : moves) {
            result.add(UciMoveConverter.toNotation(move));
        }

        return result;
    }

    private static String movesToString(int[] moves) {
        return Arrays.stream(moves)
            .mapToObj(UciMoveConverter::toNotation)
            .sorted()
            .toList()
            .toString();
    }
}