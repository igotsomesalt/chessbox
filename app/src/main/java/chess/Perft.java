package chess;

public final class Perft {

    public static long perft(Position position, int depth, boolean verbose) {
        if (depth == 0) {
            return 1;
        }

        int[] moves = MoveGenerator.generateLegalMoves(position);
        long nodes = 0;
        
        if (verbose) {
            System.out.println(
                String.format("position=%s depth=%d:\n",
                position.toFen(), depth)
            );
        }

        for (int move : moves) {
            MoveApplier.State state = MoveApplier.apply(position, move);
            if (verbose) System.out.println(UciMoveConverter.toNotation(move));
            nodes += perft(position, depth - 1, verbose);
            MoveApplier.undo(position, state);
        }

        System.out.println();
        return nodes;
    }

    public static void divide(Position position, int depth) {
        int[] moves = MoveGenerator.generateLegalMoves(position);

        long total = 0;

        try {
            for (int move : moves) {
                Position child = copyPosition(position);

                MoveApplier.apply(child, move);

                long nodes = perft(child, depth - 1, false);
                System.out.println(UciMoveConverter.toNotation(move) + ": " + nodes);

                total += nodes;
            }
        }

        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Total: " + total);
    }

    private static Position copyPosition(Position position) {
        return new Position(
            position.bitboards.clone(),
            position.epSq,
            position.castlingRights,
            position.whiteToMove,
            position.halfMoveClock,
            position.fullMoveClock
        );
    }
}