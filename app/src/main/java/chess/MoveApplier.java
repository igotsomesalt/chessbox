package chess;

public class MoveApplier {

    public static final class State {
        int move;
        int capturedPiece;
        int capturedSquare;
        int promotedPiece;

        int oldCastlingRights;
        int oldEpSq;
        int oldHalfMoveClock;
        int oldFullMoveClock;
        boolean oldWhiteToMove;

        long oldWhitePieces;
        long oldBlackPieces;
        long oldAllPieces;

        public State() {

        }

        public State(
            int move, int capturedPiece, int capturedSquare, int promotedPiece,
            int oldCastlingRights, int oldEpSq, int oldHalfMoveClock, int oldFullMoveClock, boolean oldWhiteToMove,
            long oldWhitePieces, long oldBlackPieces, long oldAllPieces
        ) {
            this.move = move; this.capturedPiece = capturedPiece; this.capturedSquare = capturedSquare; 
            this.promotedPiece = promotedPiece; this.oldCastlingRights = oldCastlingRights; this.oldEpSq = oldEpSq;
            this.oldHalfMoveClock = oldHalfMoveClock; this.oldFullMoveClock = oldFullMoveClock;
            this.oldWhiteToMove = oldWhiteToMove; this.oldWhitePieces = oldWhitePieces; 
            this.oldBlackPieces = oldBlackPieces; this.oldAllPieces = oldAllPieces;
        }

        public State(int move, Position position) {
            this(
                move, Move.getCapPiece(move), Move.getSq2(move), Move.getPromPiece(move),
                position.castlingRights, position.epSq, position.halfMoveClock, position.fullMoveClock, 
                position.whiteToMove, position.whitePieces, position.blackPieces, position.allPieces
            );
        }
    }

    public static State apply(Position position, int move) {
        State state = new State(move, position);
        apply(position, move, state);
        return state;
    }
    
    public static void apply(Position position, int move, State state) {

        state.move = move;
        state.capturedPiece = Move.getCapPiece(move);
        state.capturedSquare = Move.getSq2(move);
        state.promotedPiece = Move.getPromPiece(move);

        state.oldCastlingRights = position.castlingRights;
        state.oldEpSq = position.epSq;
        state.oldHalfMoveClock = position.halfMoveClock;
        state.oldFullMoveClock = position.fullMoveClock;
        state.oldWhiteToMove = position.whiteToMove;

        state.oldWhitePieces = position.whitePieces;
        state.oldBlackPieces = position.blackPieces;
        state.oldAllPieces = position.allPieces;

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
                updateCastlingRightsAfterCapture(position, capturedPiece, to);
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
            position.setCastlingRights(true, true, false);
            position.setCastlingRights(false, true, false);
        } else if (piece == Position.BLACK_KING) {
            position.setCastlingRights(true, false, false);
            position.setCastlingRights(false, false, false);
        }

        // Update en passant square
        position.epSq = -1;

        if (piece == Position.WHITE_PAWN && to - from == 16) {
            int epSquare = from + 8;

            // Black pawn must be adjacent to the EP target.
            int file = epSquare & 7;

            boolean capturable =
                    (file > 0
                            && position.getPieceAt(epSquare + 7)
                            == Position.BLACK_PAWN)
                    ||
                    (file < 7
                            && position.getPieceAt(epSquare + 9)
                            == Position.BLACK_PAWN);

            if (capturable) {
                position.epSq = epSquare;
            }

            } else if (piece == Position.BLACK_PAWN && from - to == 16) {
            int epSquare = from - 8;

            // White pawn must be adjacent to the EP target.
            int file = epSquare & 7;

            boolean capturable =
                    (file > 0
                            && position.getPieceAt(epSquare - 9)
                            == Position.WHITE_PAWN)
                    ||
                    (file < 7
                            && position.getPieceAt(epSquare - 7)
                            == Position.WHITE_PAWN);

            if (capturable) {
                position.epSq = epSquare;
            }
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

    private static void updateCastlingRightsAfterCapture(Position position, int capturedPiece, int square) {
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

    public static void undo(Position position, State state) {
        int move = state.move;

        int piece = Move.getPiece(move);
        boolean white = piece < 6;
        int castle = Move.getCastle(move);
        boolean ep = Move.getEP(move) == 1;

        if (castle == 1) undoKingCastle(position, white);
        else if (castle == 2) undoQueenCastle(position, white);

        // Normal move
        else {
            int from = Move.getSq1(move);
            int to = Move.getSq2(move);

            long fromMask = 1L << from;
            long toMask = 1L << to;

            if (state.promotedPiece != Position.NO_PIECE) {
                position.bitboards[state.promotedPiece] &= ~toMask;
                position.bitboards[piece] |= fromMask;
            }

            else {
                position.bitboards[piece] &= ~toMask;
                position.bitboards[piece] |= fromMask;
            }

            // Restore captured piece.
            if (state.capturedPiece != Position.NO_PIECE) {
                int corrector = (ep ? 8 : 0) * (white ? -1 : 1);
                position.bitboards[state.capturedPiece] |= 1L << (state.capturedSquare + corrector);
            }
        }

        // Restore all state that can change during a move.
        position.castlingRights = state.oldCastlingRights;
        position.epSq = state.oldEpSq;
        position.halfMoveClock = state.oldHalfMoveClock;
        position.fullMoveClock = state.oldFullMoveClock;
        position.whiteToMove = state.oldWhiteToMove;

        position.whitePieces = state.oldWhitePieces;
        position.blackPieces = state.oldBlackPieces;
        position.allPieces = state.oldAllPieces;

        position.rebuildPieceAt();
    }

    private static void undoKingCastle(Position position, boolean white) {
        int king = white ? Position.WHITE_KING : Position.BLACK_KING;
        int rook = white ? Position.WHITE_ROOK : Position.BLACK_ROOK;

        int kingFrom = white ? Bitboard.E1 : Bitboard.E8;
        int kingTo   = white ? Bitboard.G1 : Bitboard.G8;

        int rookFrom = white ? Bitboard.H1 : Bitboard.H8;
        int rookTo   = white ? Bitboard.F1 : Bitboard.F8;

        position.bitboards[king] &= ~(1L << kingTo);
        position.bitboards[king] |= 1L << kingFrom;

        position.bitboards[rook] &= ~(1L << rookTo);
        position.bitboards[rook] |= 1L << rookFrom;
    }

    private static void undoQueenCastle(Position position, boolean white) {
        int king = white ? Position.WHITE_KING : Position.BLACK_KING;
        int rook = white ? Position.WHITE_ROOK : Position.BLACK_ROOK;

        int kingFrom = white ? Bitboard.E1 : Bitboard.E8;
        int kingTo   = white ? Bitboard.C1 : Bitboard.C8;

        int rookFrom = white ? Bitboard.A1 : Bitboard.A8;
        int rookTo   = white ? Bitboard.D1 : Bitboard.D8;

        position.bitboards[king] &= ~(1L << kingTo);
        position.bitboards[king] |= 1L << kingFrom;

        position.bitboards[rook] &= ~(1L << rookTo);
        position.bitboards[rook] |= 1L << rookFrom;
    }
}
