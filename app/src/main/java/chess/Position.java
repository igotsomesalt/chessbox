package chess;

public class Position {

    public static final int WHITE_KING = 5;
    public static final int WHITE_QUEEN = 4;
    public static final int WHITE_ROOK = 3;
    public static final int WHITE_BISHOP = 2;
    public static final int WHITE_KNIGHT = 1;
    public static final int WHITE_PAWN = 0;

    public static final int BLACK_KING = 11;
    public static final int BLACK_QUEEN = 10;
    public static final int BLACK_ROOK = 9;
    public static final int BLACK_BISHOP = 8;
    public static final int BLACK_KNIGHT = 7;
    public static final int BLACK_PAWN = 6;

    public static final int NO_PIECE = 15;

    // bitboard array indexes:
    // 0: w pawn; 1: w knight; 2: w bishop; 3: w rook; 4: w queen; 5: w king
    // 6: b pawn; 7: b knight; 8: b bishop; 9: b rook; 10: b queen; 11: b king
    public long[] bitboards;

    // Piece occupying each square.
    // Index = square (A1 = 0, H8 = 63)
    // Value = piece constant or NO_PIECE
    public int[] pieceAt;

    // All piece masks.
    public long allPieces;
    public long whitePieces;
    public long blackPieces;

    public boolean whiteToMove;
    public int epSq;

    // 1st bit = wk; 2nd = wq; 3rd = bk; 4th = bq
    public int castlingRights;

    public int halfMoveClock;
    public int fullMoveClock;

    public static final long[] DEFAULT_BITBOARDS = {
        0x000000000000ff00L, // w pawns
        0x0000000000000042L, // w knights
        0x0000000000000024L, // w bishops
        0x0000000000000081L, // w rooks
        0x0000000000000008L, // w queen
        0x0000000000000010L, // w king

        0x00ff000000000000L, // b pawns
        0x4200000000000000L, // b knights
        0x2400000000000000L, // b bishops
        0x8100000000000000L, // b rooks
        0x0800000000000000L, // b queen
        0x1000000000000000L  // b king
    };

    public static final int DEFAULT_CASTLING = 0xf;

    public Position() {
        this(DEFAULT_BITBOARDS, DEFAULT_CASTLING, -1, true, 0, 1);
    }

    public Position(Position position) {
        this(
            position.bitboards.clone(), position.castlingRights,
            position.epSq, position.whiteToMove,
            position.halfMoveClock, position.fullMoveClock
        );
    }

    public Position(String fenString) {
        this(Fen.fromFen(fenString));
    }

    public Position(
        long[] bitboards, int castlingRights, int epSq,
        boolean whiteToMove, int halfMoveClock, int fullMoveClock
    ) {
        this.bitboards = bitboards.clone();
        this.epSq = epSq;
        this.castlingRights = castlingRights;
        this.whiteToMove = whiteToMove;
        this.halfMoveClock = halfMoveClock;
        this.fullMoveClock = fullMoveClock;

        this.pieceAt = new int[64];
        initializePieceAt();
        rebuildDerivedState();
    }

    private void initializePieceAt() {
        // Default every square to NO_PIECE.
        for (int square = 0; square < 64; square++) {
            pieceAt[square] = NO_PIECE;
        }

        // Populate the array from the bitboards.
        for (int piece = 0; piece < 12; piece++) {
            long board = bitboards[piece];

            while (board != 0) {
                int square = Long.numberOfTrailingZeros(board);

                pieceAt[square] = piece;

                board &= board - 1;
            }
        }
    }

    public void rebuildDerivedState() {
        initializePieceAt();

        whitePieces = 0L;
        blackPieces = 0L;

        for (int piece = 0; piece < 6; piece++) {
            whitePieces |= bitboards[piece];
        }

        for (int piece = 6; piece < 12; piece++) {
            blackPieces |= bitboards[piece];
        }

        allPieces = whitePieces | blackPieces;
    }

    void rebuildPieceAt() {
        for (int square = 0; square < 64; square++) {
            pieceAt[square] = NO_PIECE;
        }

        for (int piece = 0; piece < 12; piece++) {
            long board = bitboards[piece];

            while (board != 0) {
                int square = Long.numberOfTrailingZeros(board);
                pieceAt[square] = piece;
                board &= board - 1;
            }
        }
    }

    public int getPieceAt(int square) {
        return pieceAt[square];
    }

    public boolean getCastlingRights(boolean kingside, boolean white) {
        int shift = (kingside ? 0 : 1) + (white ? 0 : 2);
        return ((castlingRights >>> shift) & 1) == 1;
    }

    public void setCastlingRights(
        boolean kingside,
        boolean white,
        boolean value
    ) {
        int shift = (kingside ? 0 : 1) + (white ? 0 : 2);
        int mask = 1 << shift;

        if (value) {
            castlingRights |= mask;
        } else {
            castlingRights &= ~mask;
        }
    }

    public String toFen() {
        return Fen.toFen(this);
    }

    public boolean isEmpty(int square) {
        return (allPieces & 1L << square) == 0;
    }

    public long getSideOccupied(boolean white) {
        return white ? whitePieces : blackPieces;
    }

    public long getOccupied() {
        return allPieces;
    }

    public boolean hasPiece(int square, int piece) {
        return (bitboards[piece] & 1L << square) != 0;
    }

    public MoveApplier.State makeMove(int move) {
        return MoveApplier.apply(this, move);
    }

    public void makeMove(int move, MoveApplier.State state) {
        MoveApplier.apply(this, move, state);
    }

    public MoveApplier.State makeMove(Move move) {
        return MoveApplier.apply(this, move.encoded);
    }

    public void makeMove(Move move, MoveApplier.State state) {
        MoveApplier.apply(this, move.encoded, state);
    }

    public void undoMove(MoveApplier.State state) {
        MoveApplier.undo(this, state);
    }
}