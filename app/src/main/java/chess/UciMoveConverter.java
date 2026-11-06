package chess;

public class UciMoveConverter {

    public UciMoveConverter() {
        super();
    }

    public static String toNotation(Move move) {
        return toNotation(move.encoded);
    }

    public static String toNotation(int move) {
        int from = Move.getSq1(move);
        int to = Move.getSq2(move);
        int promotion = Move.getPromPiece(move);

        StringBuilder uci = new StringBuilder(5);

        uci.append(squareToString(from));
        uci.append(squareToString(to));

        if (promotion != Position.NO_PIECE) {
            uci.append(pieceToUci(promotion));
        }

        return uci.toString();
    }


    public static Move toMove(String move, Position position) throws IllegalArgumentException {
        return new Move(toMoveInt(move, position));
    }

    public static int toMoveInt(String move, Position position) throws IllegalArgumentException {
        if (move == null || (move.length() != 4 && move.length() != 5)) {
            throw new IllegalArgumentException("Invalid UCI move: " + move);
        }

        int from = stringToSquare(move.substring(0, 2));
        int to = stringToSquare(move.substring(2, 4));

        int piece = position.getPieceAt(from);

        if (piece == Position.NO_PIECE) {
            throw new IllegalArgumentException(
                "No piece on source square: " + move
            );
        }

        int capPiece = position.getPieceAt(to);

        int promPiece = Position.NO_PIECE;

        if (move.length() == 5) {
            promPiece = uciToPiece(move.charAt(4), piece);
        }

        int castle = getCastleType(piece, from, to);
        boolean ep = isEnPassant(position, piece, from, to, capPiece);

        if (ep) {
            capPiece = piece == Position.WHITE_PAWN
                ? Position.BLACK_PAWN
                : Position.WHITE_PAWN;
        }


        return Move.encode(
            from,
            to,
            piece,
            capPiece,
            promPiece,
            castle,
            ep
        );
    }

    private static String squareToString(int square) {
        if (square < 0 || square > 63) {
            throw new IllegalArgumentException("Invalid square: " + square);
        }

        char file = (char) ('a' + (square & 7));
        char rank = (char) ('1' + (square >>> 3));

        return "" + file + rank;
    }

    private static int stringToSquare(String square) {
        if (square == null || square.length() != 2) {
            throw new IllegalArgumentException("Invalid square: " + square);
        }

        char file = square.charAt(0);
        char rank = square.charAt(1);

        if (file < 'a' || file > 'h' ||
            rank < '1' || rank > '8') {
            throw new IllegalArgumentException("Invalid square: " + square);
        }

        return (rank - '1') * 8 + (file - 'a');
    }

    private static char pieceToUci(int piece) {
        return switch (piece) {
            case Position.WHITE_KNIGHT, Position.BLACK_KNIGHT -> 'n';
            case Position.WHITE_BISHOP, Position.BLACK_BISHOP -> 'b';
            case Position.WHITE_ROOK, Position.BLACK_ROOK -> 'r';
            case Position.WHITE_QUEEN, Position.BLACK_QUEEN -> 'q';
            default -> throw new IllegalArgumentException(
                "Invalid promotion piece: " + piece
            );
        };
    }

    private static int uciToPiece(char piece, int movingPiece) {
        boolean white = movingPiece < 6;

        return switch (Character.toLowerCase(piece)) {
            case 'n' -> white
                ? Position.WHITE_KNIGHT
                : Position.BLACK_KNIGHT;

            case 'b' -> white
                ? Position.WHITE_BISHOP
                : Position.BLACK_BISHOP;

            case 'r' -> white
                ? Position.WHITE_ROOK
                : Position.BLACK_ROOK;

            case 'q' -> white
                ? Position.WHITE_QUEEN
                : Position.BLACK_QUEEN;

            default -> throw new IllegalArgumentException(
                "Invalid promotion piece: " + piece
            );
        };
    }


    private static int getCastleType(int piece, int from, int to) {
        if (piece == Position.WHITE_KING) {
            if (from == Position.E1 && to == Position.G1) {
                return 1;
            }

            if (from == Position.E1 && to == Position.C1) {
                return 2;
            }
        }

        if (piece == Position.BLACK_KING) {
            if (from == Position.E8 && to == Position.G8) {
                return 1;
            }

            if (from == Position.E8 && to == Position.C8) {
                return 2;
            }
        }

        return 0;
    }

    private static boolean isEnPassant(
        Position position,
        int piece,
        int from,
        int to,
        int capPiece
    ) {
        // Only pawns can capture en passant.
        if (piece != Position.WHITE_PAWN &&
            piece != Position.BLACK_PAWN) {
            return false;
        }

        // Destination must be empty.
        if (capPiece != Position.NO_PIECE) {
            return false;
        }

        // The destination must be the current en-passant square.
        return to == position.epSq;
    }
}