package chessbox.chess;

public class Fen {

    private Fen() {}

    public static String toFen(Position position) {
        StringBuilder fen = new StringBuilder();

        for (int rank = 7; rank >= 0; rank--) {
            int empty = 0;

            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                int piece = getPieceAt(position.bitboards, square);

                if (piece == -1) {
                    empty++;
                } else {
                    if (empty > 0) {
                        fen.append(empty);
                        empty = 0;
                    }

                    fen.append(pieceToFenChar(piece));
                }
            }

            if (empty > 0) {
                fen.append(empty);
            }

            if (rank > 0) {
                fen.append('/');
            }
        }

        fen.append(' ');
        fen.append(position.whiteToMove ? 'w' : 'b');

        fen.append(' ');
        appendCastlingRights(fen, position);

        fen.append(' ');

        if (position.epSq == -1) {
            fen.append('-');
        } else {
            fen.append(squareToString(position.epSq));
        }

        fen.append(String.format(" %d %d", position.halfMoveClock, position.fullMoveClock));

        return fen.toString();
    }

    public static Position fromFen(String fen) throws IllegalArgumentException {
        String[] fields = fen.trim().split("\\s+");

        if (fields.length != 6) {
            throw new IllegalArgumentException(
                "Invalid FEN: expected 6 fields, got " + fields.length
            );
        }

        long[] bitboards = new long[12];

        String[] ranks = fields[0].split("/");

        if (ranks.length != 8) {
            throw new IllegalArgumentException(
                "Invalid FEN: expected 8 ranks"
            );
        }

        for (int fenRank = 0; fenRank < 8; fenRank++) {
            int rank = 7 - fenRank;
            int file = 0;

            for (int i = 0; i < ranks[fenRank].length(); i++) {
                char c = ranks[fenRank].charAt(i);

                if (Character.isDigit(c)) {
                    int emptySquares = c - '0';

                    if (emptySquares < 1 || emptySquares > 8) {
                        throw new IllegalArgumentException(
                            "Invalid FEN: invalid empty-square count"
                        );
                    }

                    file += emptySquares;
                } else {
                    int piece = fenCharToPiece(c);

                    if (piece == -1) {
                        throw new IllegalArgumentException(
                            "Invalid FEN: invalid piece '" + c + "'"
                        );
                    }

                    if (file >= 8) {
                        throw new IllegalArgumentException(
                            "Invalid FEN: too many squares in rank"
                        );
                    }

                    int square = rank * 8 + file;

                    bitboards[piece] |= 1L << square;

                    file++;
                }
            }

            if (file != 8) {
                throw new IllegalArgumentException(
                    "Invalid FEN: rank does not contain 8 squares"
                );
            }
        }

        boolean whiteToMove;

        if (fields[1].equals("w")) {
            whiteToMove = true;
        } else if (fields[1].equals("b")) {
            whiteToMove = false;
        } else {
            throw new IllegalArgumentException(
                "Invalid FEN: invalid side to move"
            );
        }

        int castlingRights = 0;

        if (!fields[2].equals("-")) {
            for (int i = 0; i < fields[2].length(); i++) {
                switch (fields[2].charAt(i)) {
                    case 'K':
                        castlingRights |= 1;
                        break;

                    case 'Q':
                        castlingRights |= 1 << 1;
                        break;

                    case 'k':
                        castlingRights |= 1 << 2;
                        break;

                    case 'q':
                        castlingRights |= 1 << 3;
                        break;

                    default:
                        throw new IllegalArgumentException(
                            "Invalid FEN: invalid castling right"
                        );
                }
            }
        }

        int epSq;

        if (fields[3].equals("-")) {
            epSq = -1;
        } else {
            epSq = stringToSquare(fields[3]);
        }

        int halfMoveClock = 0;
        int fullMoveClock = 0;

        try {
            halfMoveClock = Integer.parseInt(fields[4]);
            fullMoveClock = Integer.parseInt(fields[5]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Invalid FEN: invalid move counters"
            );
        }

        return new Position(
            bitboards,
            castlingRights,
            epSq,
            whiteToMove,
            halfMoveClock,
            fullMoveClock
        );
    }


    private static int getPieceAt(long[] bitboards, int square) {
        long mask = 1L << square;

        for (int piece = 0; piece < 12; piece++) {
            if ((bitboards[piece] & mask) != 0) {
                return piece;
            }
        }

        return -1;
    }


    private static char pieceToFenChar(int piece) {
        return switch (piece) {
            case Position.WHITE_PAWN   -> 'P';
            case Position.WHITE_KNIGHT -> 'N';
            case Position.WHITE_BISHOP -> 'B';
            case Position.WHITE_ROOK   -> 'R';
            case Position.WHITE_QUEEN  -> 'Q';
            case Position.WHITE_KING   -> 'K';

            case Position.BLACK_PAWN   -> 'p';
            case Position.BLACK_KNIGHT -> 'n';
            case Position.BLACK_BISHOP -> 'b';
            case Position.BLACK_ROOK   -> 'r';
            case Position.BLACK_QUEEN  -> 'q';
            case Position.BLACK_KING   -> 'k';

            default -> throw new IllegalArgumentException(
                "Invalid piece index: " + piece
            );
        };
    }


    private static int fenCharToPiece(char c) {
        return switch (c) {
            case 'P' -> Position.WHITE_PAWN;
            case 'N' -> Position.WHITE_KNIGHT;
            case 'B' -> Position.WHITE_BISHOP;
            case 'R' -> Position.WHITE_ROOK;
            case 'Q' -> Position.WHITE_QUEEN;
            case 'K' -> Position.WHITE_KING;

            case 'p' -> Position.BLACK_PAWN;
            case 'n' -> Position.BLACK_KNIGHT;
            case 'b' -> Position.BLACK_BISHOP;
            case 'r' -> Position.BLACK_ROOK;
            case 'q' -> Position.BLACK_QUEEN;
            case 'k' -> Position.BLACK_KING;

            default -> -1;
        };
    }

    /**
     * Appends KQkq castling rights.
     */
    private static void appendCastlingRights(
            StringBuilder fen,
            Position position
    ) {
        boolean hasRights = false;

        if (position.getCastlingRights(true, true)) {
            fen.append('K');
            hasRights = true;
        }

        if (position.getCastlingRights(false, true)) {
            fen.append('Q');
            hasRights = true;
        }

        if (position.getCastlingRights(true, false)) {
            fen.append('k');
            hasRights = true;
        }

        if (position.getCastlingRights(false, false)) {
            fen.append('q');
            hasRights = true;
        }

        if (!hasRights) {
            fen.append('-');
        }
    }


    private static String squareToString(int square) {
        if (square < 0 || square >= 64) {
            throw new IllegalArgumentException(
                "Invalid square: " + square
            );
        }

        char file = (char) ('a' + (square & 7));
        char rank = (char) ('1' + (square >>> 3));

        return "" + file + rank;
    }


    private static int stringToSquare(String square) {
        if (square.length() != 2) {
            throw new IllegalArgumentException(
                "Invalid en passant square: " + square
            );
        }

        char file = square.charAt(0);
        char rank = square.charAt(1);

        if (file < 'a' || file > 'h' ||
            rank < '1' || rank > '8') {
            throw new IllegalArgumentException(
                "Invalid square: " + square
            );
        }

        return (rank - '1') * 8 + (file - 'a');
    }
}
