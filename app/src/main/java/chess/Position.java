package chess;

class Position {
    // bitboard array indexes:
    // 0: w pawn; 1: w knight; 2: w bishop; 3: w rook; 4: w queen; 5: w king
    // 6: b pawn; 7: b knight; 8: b bishop; 9: b rook; 10: b queen; 11: b king
    public long[] bitboards;
    public boolean whiteToMove;
    public int epSq;
    
    // 1st bit = wk; 2nd = wq; 3rd = bk; 4th = bq
    public int castlingRights;

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
        this(DEFAULT_BITBOARDS, DEFAULT_CASTLING, -1, true);
    }

    public Position(long[] bitboards, int castlingRights, int epSq, boolean whiteToMove) {
        this.bitboards = bitboards;
        this.epSq = epSq;
        this.castlingRights = castlingRights;
        this.whiteToMove = whiteToMove;
    }

    public boolean getCastlingRights(boolean kingside, boolean white) {
        int shift = (kingside ? 0 : 1) + (white ? 0 : 2);
        return ((castlingRights >>> shift) & 1) == 1;
    }

    public void setCastlingRights(boolean kingside, boolean white, boolean value) {
        int shift = (kingside ? 0 : 1) + (white ? 0 : 2);
        int mask = 1 << shift;

        if (value) {
            castlingRights |= mask;
        } else {
            castlingRights &= ~mask;
        }
    }
}