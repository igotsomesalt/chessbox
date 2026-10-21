package chess;

public class Bitboard {

    public static final int WHITE_KING = 5;
    public static final int WHITE_ROOK = 3;

    public static final int BLACK_KING = 11;
    public static final int BLACK_ROOK = 9;

    public static final int G1 = 6;
    public static final int F1 = 5;
    public static final int E1 = 4;
    public static final int H1 = 7;
    public static final int C1 = 2;
    public static final int D1 = 3;
    public static final int A1 = 0;

    public static final int G8 = 62;
    public static final int F8 = 61;
    public static final int E8 = 60;
    public static final int H8 = 63;
    public static final int C8 = 58;
    public static final int D8 = 59;
    public static final int A8 = 56;
    
    private Bitboard() {}

    public static long addBit(long bitboard, int index) {
        return bitboard | (0x1 << index);
    }

    public static long removeBit(long bitboard, int index) {
        return bitboard & ~(0x1 << index);
    }

    public static long moveBit(long bitboard, int i1, int i2) {
        return (bitboard | (0x1 << i2)) & ~(0x1 << i1);
    }
}
