package chessbox.chess;

public class Move {
	// encoded move format:
	//             EP  Castle  PromPiece  CapPiece     Piece  Sq2     Sq1
	// 0b00000000_|0___00_____00|00______000|0________0000|___0000|00_00|0000
	//            26   24        20         16          12          6       0
    public int encoded;

    public Move(int encodedMove) {
        encoded = encodedMove;
    }

    public static int encode(int from, int to, int piece, int capPiece, int promPiece, int castle, boolean ep) {
        return 
            from |
            (to << 6) |
            (piece << 12) |
            (capPiece << 16) |
            (promPiece << 20) |
            (castle << 24) |
            ((ep ? 1 : 0) << 26);
    }

    public static int getSq1(int encoded) {
        return encoded & 0x3f;
    }

    public static int getSq2(int encoded) {
        return (encoded >>> 6) & 0x3f;
    }

    public static int getPiece(int encoded) {
        return (encoded >>> 12) & 0xf;
    }

    public static int getCapPiece(int encoded) {
        return (encoded >>> 16) & 0xf;
    }

    public static int getPromPiece(int encoded) {
        return (encoded >>> 20) & 0xf;
    }

    public static int getCastle(int encoded) {
        return (encoded >>> 24) & 0x3;
    }
    
    public static int getEP(int encoded) {
        return (encoded >>> 26) & 0x1;
    }

    public static String toUci(int encoded) {
        return UciMoveConverter.toNotation(encoded);
    }

    public int getSq1() {
        return encoded & 0x3f;
    }

    public int getSq2() {
        return (encoded >>> 6) & 0x3f;
    }

    public int getPiece() {
        return (encoded >>> 12) & 0xf;
    }

    public int getCapPiece() {
        return (encoded >>> 16) & 0xf;
    }

    public int getPromPiece() {
        return (encoded >>> 20) & 0xf;
    }

    public int getCastle() {
        return (encoded >>> 24) & 0x3;
    }

    public int getEP() {
        return (encoded >>> 26) & 0x1;
    }

    public String toUci() {
        return UciMoveConverter.toNotation(encoded);
    }
}