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

    public Position(long[] bitboards, int epSq, int castlingRights, boolean whiteToMove) {
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