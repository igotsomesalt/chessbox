package chess;

public class Bitboard {

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
