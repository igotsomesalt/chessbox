package chess;

import java.util.Random;

public class Zobrist {
    private static final long[][] PIECES = new long[12][64];
    private static final long[] CASTLING = new long[16];
    private static final long[] EN_PASSANT = new long[8];
    private static final long SIDE_TO_MOVE;

    static {
        Random random = new Random(12345);

        for (int piece = 0; piece < 12; piece++) {
            for (int sq = 0; sq < 64; sq++) {
                PIECES[piece][sq] = random.nextLong();
            }
        }

        for (int i = 0; i < 16; i++) {
            CASTLING[i] = random.nextLong();
        }

        for (int i = 0; i < 8; i++) {
            EN_PASSANT[i] = random.nextLong();
        }

        SIDE_TO_MOVE = random.nextLong();
    }

    public static long compute(Position position) {
        long hash = 0;

        for (int piece = 0; piece < 12; piece++) {
            long bb = position.bitboards[piece];

            while (bb != 0) {
                int sq = Long.numberOfTrailingZeros(bb);

                hash ^= PIECES[piece][sq];

                bb &= bb - 1;
            }
        }

        if (!position.whiteToMove) {
            hash ^= SIDE_TO_MOVE;
        }

        hash ^= CASTLING[position.castlingRights];

        int epSq = position.epSq;

        if (epSq != -1) {
            hash ^= EN_PASSANT[epSq % 8];
        }

        return hash;
    }

    public static long updateHash( 
        long hash, int move, int capturedPiece, int capturedSquare, 
        int oldCastlingRights, int newCastlingRights, int oldEpSq, int newEpSq
    ) {
        int from = Move.getSq1(move);
        int to = Move.getSq2(move);
        int piece = Move.getPiece(move);

        // Remove moving piece from original square
        hash ^= Zobrist.PIECES[piece][from];

        // Add piece at destination (promotion handled separately)
        int promotion = Move.getPromPiece(move);
        int destinationPiece = promotion != 0 ? promotion : piece;

        hash ^= Zobrist.PIECES[destinationPiece][to];

        // Remove captured piece
        if (capturedPiece != -1) {
            hash ^= Zobrist.PIECES[capturedPiece][capturedSquare];
        }

        // Castling: update rook's position separately
        if (Move.getCastle(move) != 0) {
            int castle = Move.getCastle(move);
            int rook = Move.getPiece(move) < 6 ? 4 : 10;
            int rookFrom = rook < 6 ? 
                (castle == 1 ? Position.A1 : Position.H2) : (castle == 1 ? Position.A8 : Position.H8);
            int rookTo = rook < 6 ? 
                (castle == 1 ? Position.D1 : Position.F1) : (castle == 1 ? Position.D1 : Position.F8);

            hash ^= Zobrist.PIECES[rook][rookFrom];
            hash ^= Zobrist.PIECES[rook][rookTo];
        }

        // Remove old castling state, add new one
        hash ^= Zobrist.CASTLING[oldCastlingRights];
        hash ^= Zobrist.CASTLING[newCastlingRights];

        // Remove old en passant state
        if (oldEpSq != -1) {
            hash ^= Zobrist.EN_PASSANT[oldEpSq % 8];
        }

        // Add new en passant state
        if (newEpSq != -1) {
            hash ^= Zobrist.EN_PASSANT[newEpSq % 8];
        }

        // Toggle side to move
        hash ^= Zobrist.SIDE_TO_MOVE;

        return hash;
    }
}
