package chess;

public class Position {

    // Rank 1
    public static final int A1 = 0;
    public static final int B1 = 1;
    public static final int C1 = 2;
    public static final int D1 = 3;
    public static final int E1 = 4;
    public static final int F1 = 5;
    public static final int G1 = 6;
    public static final int H1 = 7;

    // Rank 2
    public static final int A2 = 8;
    public static final int B2 = 9;
    public static final int C2 = 10;
    public static final int D2 = 11;
    public static final int E2 = 12;
    public static final int F2 = 13;
    public static final int G2 = 14;
    public static final int H2 = 15;

    // Rank 3
    public static final int A3 = 16;
    public static final int B3 = 17;
    public static final int C3 = 18;
    public static final int D3 = 19;
    public static final int E3 = 20;
    public static final int F3 = 21;
    public static final int G3 = 22;
    public static final int H3 = 23;

    // Rank 4
    public static final int A4 = 24;
    public static final int B4 = 25;
    public static final int C4 = 26;
    public static final int D4 = 27;
    public static final int E4 = 28;
    public static final int F4 = 29;
    public static final int G4 = 30;
    public static final int H4 = 31;

    // Rank 5
    public static final int A5 = 32;
    public static final int B5 = 33;
    public static final int C5 = 34;
    public static final int D5 = 35;
    public static final int E5 = 36;
    public static final int F5 = 37;
    public static final int G5 = 38;
    public static final int H5 = 39;

    // Rank 6
    public static final int A6 = 40;
    public static final int B6 = 41;
    public static final int C6 = 42;
    public static final int D6 = 43;
    public static final int E6 = 44;
    public static final int F6 = 45;
    public static final int G6 = 46;
    public static final int H6 = 47;

    // Rank 7
    public static final int A7 = 48;
    public static final int B7 = 49;
    public static final int C7 = 50;
    public static final int D7 = 51;
    public static final int E7 = 52;
    public static final int F7 = 53;
    public static final int G7 = 54;
    public static final int H7 = 55;

    // Rank 8
    public static final int A8 = 56;
    public static final int B8 = 57;
    public static final int C8 = 58;
    public static final int D8 = 59;
    public static final int E8 = 60;
    public static final int F8 = 61;
    public static final int G8 = 62;
    public static final int H8 = 63;


    // Files
    public static final long FILE_A = 0x0101010101010101L;
    public static final long FILE_B = 0x0202020202020202L;
    public static final long FILE_C = 0x0404040404040404L;
    public static final long FILE_D = 0x0808080808080808L;
    public static final long FILE_E = 0x1010101010101010L;
    public static final long FILE_F = 0x2020202020202020L;
    public static final long FILE_G = 0x4040404040404040L;
    public static final long FILE_H = 0x8080808080808080L;

    // Ranks
    public static final long RANK_1 = 0x00000000000000ffL;
    public static final long RANK_2 = 0x000000000000ff00L;
    public static final long RANK_3 = 0x0000000000ff0000L;
    public static final long RANK_4 = 0x00000000ff000000L;
    public static final long RANK_5 = 0x000000ff00000000L;
    public static final long RANK_6 = 0x0000ff0000000000L;
    public static final long RANK_7 = 0x00ff000000000000L;
    public static final long RANK_8 = 0xff00000000000000L;

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

    public Position(String fenString) throws IllegalArgumentException {
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

    public boolean isMoveLegal(Move move) {
        return isMoveLegal(move.encoded);
    }

    public boolean isMoveLegal(int move) {
        int[] moves = MoveGenerator.generateLegalMoves(this);
        
        for (int _move : moves) {
            if (move == _move) return true;
        }

        return false;
    }

    public boolean isInCheckmate() {
        return  MoveGenerator.isKingAttacked(this, whiteToMove) && 
                MoveGenerator.generateLegalMoves(this).length == 0;
    }

    public boolean isInStalemate() {
        return  !MoveGenerator.isKingAttacked(this, whiteToMove) &&
                 MoveGenerator.generateLegalMoves(this).length == 0;
    }
}