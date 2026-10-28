package chess;

public class MoveApplier {
    
    static void apply(Position position, int move) {
        int piece = Move.getPiece(move);
        boolean white = piece < 6;

        int castle = Move.getCastle(move);

        // Castling
        if (castle == 1) {
            doKingCastle(position, white);
            position.epSq = -1;

            position.halfMoveClock++;
            if (!position.whiteToMove) position.fullMoveClock++;

            position.whiteToMove = !position.whiteToMove;
            position.rebuildDerivedState();
            return;
        }

        if (castle == 2) {
            doQueenCastle(position, white);
            position.epSq = -1;

            position.halfMoveClock++;
            if (!position.whiteToMove) position.fullMoveClock++;

            position.whiteToMove = !position.whiteToMove;
            position.rebuildDerivedState();
            return;
        }

        int from = Move.getSq1(move);
        int to = Move.getSq2(move);

        long fromMask = 1L << from;
        long toMask = 1L << to;
        
        position.bitboards[piece] &= ~fromMask;

        // En passant
        if (Move.getEP(move) == 1) {
            int capturedPawnSq = white ? to - 8 : to + 8;
            int capturedPawn = white ? 6 : 0;

            position.bitboards[capturedPawn] &= ~(1L << capturedPawnSq);

            position.bitboards[piece] |= toMask;
        }

        // Promotion
        else if (Move.getPromPiece(move) != Position.NO_PIECE) {
            int promotedPiece = Move.getPromPiece(move);

            // Remove captured piece, if any
            int capturedPiece = Move.getCapPiece(move);
            if (capturedPiece != Position.NO_PIECE) {
                position.bitboards[capturedPiece] &= ~toMask;
            }

            // Put promoted piece on destination
            position.bitboards[promotedPiece] |= toMask;
        }

        // Normal move / capture
        else {
            // Remove captured piece, if any
            int capturedPiece = Move.getCapPiece(move);

            if (capturedPiece != Position.NO_PIECE) {
                position.bitboards[capturedPiece] &= ~toMask;
                updateCastlingRightsAfterCapture(position, capturedPiece, to);
            }

            // Move piece
            position.bitboards[piece] |= toMask;
        }

        // Update castling rights

        if (piece == Position.WHITE_KING) {
            position.setCastlingRights(false, true, false);
            position.setCastlingRights(false, true, true);
        } else if (piece == Position.BLACK_KING) {
            position.setCastlingRights(false, false, false);
            position.setCastlingRights(false, false, true);
        }

        // Update en passant square
        position.epSq = -1;

        if (piece == 0 && to - from == 16) {
            position.epSq = from + 8;
        } else if (piece == 6 && from - to == 16) {
            position.epSq = from - 8;
        }

        if (piece == Position.WHITE_PAWN ||
            piece == Position.BLACK_PAWN ||
            Move.getCapPiece(move) != Position.NO_PIECE ||
            Move.getEP(move) == 1) {

            position.halfMoveClock = 0;

        } else {
            position.halfMoveClock++;
        }

        if (!position.whiteToMove) {
            position.fullMoveClock++;
        }
        
        position.whiteToMove = !position.whiteToMove;
        position.rebuildDerivedState();
    }

    static void doKingCastle(Position position, boolean w) {
        int king = w ? Position.WHITE_KING : Position.BLACK_KING;
        int rook = w ? Position.WHITE_ROOK : Position.BLACK_ROOK;

        int kingFrom = w ? Bitboard.E1 : Bitboard.E8;
        int kingTo   = w ? Bitboard.G1 : Bitboard.G8;

        int rookFrom = w ? Bitboard.H1 : Bitboard.H8;
        int rookTo   = w ? Bitboard.F1 : Bitboard.F8;

        position.bitboards[king] &= ~(1L << kingFrom);
        position.bitboards[king] |=  (1L << kingTo);

        position.bitboards[rook] &= ~(1L << rookFrom);
        position.bitboards[rook] |=  (1L << rookTo);

        position.setCastlingRights(false, w, false);
    }

    public static void doQueenCastle(Position position, boolean w) {
        int king = w ? Position.WHITE_KING : Position.BLACK_KING;
        int rook = w ? Position.WHITE_ROOK : Position.BLACK_ROOK;

        int kingFrom = w ? Bitboard.E1 : Bitboard.E8;
        int kingTo   = w ? Bitboard.C1 : Bitboard.C8;

        int rookFrom = w ? Bitboard.A1 : Bitboard.A8;
        int rookTo   = w ? Bitboard.D1 : Bitboard.D8;

        position.bitboards[king] &= ~(1L << kingFrom);
        position.bitboards[king] |=  (1L << kingTo);

        position.bitboards[rook] &= ~(1L << rookFrom);
        position.bitboards[rook] |=  (1L << rookTo);

        position.setCastlingRights(false, w, false);
    }

        private static void updateCastlingRightsAfterCapture(
            Position position,
            int capturedPiece,
            int square
    ) {
        if (capturedPiece == Position.WHITE_ROOK) {
            if (square == Bitboard.H1) {
                position.setCastlingRights(true, true, false);
            } else if (square == Bitboard.A1) {
                position.setCastlingRights(false, true, false);
            }
        } else if (capturedPiece == Position.BLACK_ROOK) {
            if (square == Bitboard.H8) {
                position.setCastlingRights(true, false, false);
            } else if (square == Bitboard.A8) {
                position.setCastlingRights(false, false, false);
            }
        }
    }
}
