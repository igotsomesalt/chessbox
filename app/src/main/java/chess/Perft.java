package chess;

import java.util.HashSet;
import java.util.Set;

public final class Perft {

    public static long perft(Position position, int depth, boolean verbose) {
        MoveApplier.State[] states = new MoveApplier.State[depth];

        for (int i = 0; i < depth; i++) {
            states[i] = new MoveApplier.State();
        }

        return perft(position, depth, states, 0, verbose);
    }

    private static long perft(Position position, int depth, MoveApplier.State[] states, int ply, boolean verbose) {
        if (depth == 0) {
            return 1;
        }

        int[] mvs = MoveGenerator.generateLegalMoves(position);
        Set<Integer> seen = new HashSet<>();
        long nodes = 0;

        MoveApplier.State state = states[ply];

        if (verbose) {
            System.out.println(
                String.format("position=%s depth=%d:\n",
                position.toFen(), depth)
            );
        }

        for (int move : mvs) {
            if (!seen.add(move)) {
                System.out.println(
                    "DUPLICATE: " +
                    UciMoveConverter.toNotation(move) +
                    " in " +
                    Fen.toFen(position)
                );
            }

            MoveApplier.apply(position, move, state);
            if (verbose) System.out.println(UciMoveConverter.toNotation(move));
            nodes += perft(position, depth - 1, states, ply + 1, verbose);
            MoveApplier.undo(position, state);
        }

        return nodes;
    }
}