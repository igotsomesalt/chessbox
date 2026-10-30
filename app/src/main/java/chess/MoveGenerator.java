package chess;

public final class MoveGenerator {

    private MoveGenerator() {}

    public static int[] generateLegalMoves(Position position) {
        int[] pseudoMoves = generatePseudoLegalMoves(position);

        MoveList legalMoves = new MoveList(pseudoMoves.length);
        boolean white = position.whiteToMove;

        for (int move : pseudoMoves) {
            Position copy = new Position(position);
            MoveApplier.apply(copy, move);

            if (!isKingAttacked(copy, white)) legalMoves.add(move);
        }

        return legalMoves.toArray();
    }

    public static int[] generatePseudoLegalMoves(Position position) {
        MoveList moves = new MoveList();
        boolean white = position.whiteToMove;

        // Pawns
        generatePawnMoves(position, moves, white);

        // Knights
        generatePieceMoves(position, moves, white, white ? Position.WHITE_KNIGHT : Position.BLACK_KNIGHT, 1);

        // Bishops
        generatePieceMoves(position, moves, white, white ? Position.WHITE_BISHOP : Position.BLACK_BISHOP, 2);

        // Rooks
        generatePieceMoves(position, moves, white, white ? Position.WHITE_ROOK : Position.BLACK_ROOK, 3);

        // Queens
        generatePieceMoves(position, moves, white, white ? Position.WHITE_QUEEN : Position.BLACK_QUEEN, 4);

        // King
        generatePieceMoves(position, moves, white, white ? Position.WHITE_KING : Position.BLACK_KING, 5);

        // Castling
        generateCastlingMoves(position, moves, white);

        return moves.toArray();
    }

    // Pawns
    private static void generatePawnMoves(Position position, MoveList moves, boolean white) {
        int pawn = white ? Position.WHITE_PAWN : Position.BLACK_PAWN;

        long pawns = position.bitboards[pawn];
        long occupied = position.allPieces;
        long enemy = getSideOccupied(position, !white);

        int direction = white ? 8 : -8;
        int startRank = white ? 1 : 6;
        int promotionRank = white ? 7 : 0;

        while (pawns != 0) {
            int from = Long.numberOfTrailingZeros(pawns);
            pawns &= pawns - 1;

            // Single push
            int to = from + direction;

            if ((occupied & (1L << to)) == 0) {

                if ((to >>> 3) == promotionRank) addPromotions(moves, from, to, pawn, Position.NO_PIECE, white);
                else {
                    addMove(moves, from, to, pawn, Position.NO_PIECE);

                    // Double push
                    if ((from >>> 3) == startRank) {
                        int doubleTo = from + direction * 2;

                        if ((occupied & (1L << doubleTo)) == 0) {
                            addMove(moves, from, doubleTo, pawn, Position.NO_PIECE);
                        }
                    }
                }
            }

            // Captures
            long pawnAttacks = MoveLookup.lookupPawnMask(from, true, white);

            long captures = pawnAttacks & enemy;

            while (captures != 0) {
                int captureTo = Long.numberOfTrailingZeros(captures);
                captures &= captures - 1;

                int captured = position.getPieceAt(captureTo);

                if ((captureTo >>> 3) == promotionRank) addPromotions(moves, from, captureTo, pawn, captured, white);
                else addMove(moves, from, captureTo, pawn, captured);
            }

            // En passant
            if (position.epSq >= 0 && (pawnAttacks & (1L << position.epSq)) != 0) {
                moves.add(Move.encode(
                    from, position.epSq, pawn, 
                    white ? Position.BLACK_PAWN : Position.WHITE_PAWN,
                    Position.NO_PIECE, 0, true
                ));
            }
        }
    }

    private static void addPromotions(MoveList moves, int from, int to, int pawn, int captured, boolean white) {
        int knight = white  ? Position.WHITE_KNIGHT  : Position.BLACK_KNIGHT;
        int bishop = white  ? Position.WHITE_BISHOP  : Position.BLACK_BISHOP;
        int rook = white    ? Position.WHITE_ROOK    : Position.BLACK_ROOK;
        int queen = white   ? Position.WHITE_QUEEN   : Position.BLACK_QUEEN;

        moves.add(Move.encode(from, to, pawn, captured, knight, 0, false));
        moves.add(Move.encode(from, to, pawn, captured, bishop, 0, false));
        moves.add(Move.encode(from, to, pawn, captured, rook, 0, false));
        moves.add(Move.encode(from, to, pawn, captured, queen, 0, false));
    }

    private static void generatePieceMoves(
        Position position, MoveList moves, boolean white, int piece, int lookupPiece
    ) {
        long pieces = position.bitboards[piece];

        long friendly = getSideOccupied(position, white);
        long occupied = position.getOccupied();

        while (pieces != 0) {
            int from = Long.numberOfTrailingZeros(pieces);
            pieces &= pieces - 1;

            long attacks = MoveLookup.lookupMask(lookupPiece, from, occupied);
            attacks &= ~friendly;

            addMoves(position, moves, attacks, from, piece);
        }
    }

    private static void addMoves(Position position, MoveList moves, long attacks, int from, int piece) {
        while (attacks != 0) {
            int to = Long.numberOfTrailingZeros(attacks);
            attacks &= attacks - 1;

            int captured = position.getPieceAt(to);
            addMove(moves, from, to, piece, captured);
        }
    }

    private static void generateCastlingMoves(Position position, MoveList moves, boolean white) {
        int king = white ? Position.WHITE_KING : Position.BLACK_KING;

        int kingSquare = white ? Bitboard.E1 : Bitboard.E8;

        // King must be on its original square.
        if ((position.bitboards[king] & (1L << kingSquare)) == 0) return;

        // King cannot castle out of check.
        if (isSquareAttacked(position, kingSquare, !white)) return;

        // kingside
        if (position.getCastlingRights(true, white)) {
            int f = white ? Bitboard.F1 : Bitboard.F8;
            int g = white ? Bitboard.G1 : Bitboard.G8;

            if (position.isEmpty(f)
                    && position.isEmpty(g)
                    && !isSquareAttacked(position, f, !white)
                    && !isSquareAttacked(position, g, !white)) {

                moves.add(Move.encode(kingSquare, g, king, Position.NO_PIECE, Position.NO_PIECE, 1, false));
            }
        }

        // queenside
        if (position.getCastlingRights(false, white)) {
            int b = white ? Bitboard.B1 : Bitboard.B8;
            int c = white ? Bitboard.C1 : Bitboard.C8;
            int d = white ? Bitboard.D1 : Bitboard.D8;

            if (position.isEmpty(b)
                    && position.isEmpty(c)
                    && position.isEmpty(d)
                    && !isSquareAttacked(position, c, !white)
                    && !isSquareAttacked(position, d, !white)) {

                moves.add(Move.encode(kingSquare, c, king, Position.NO_PIECE, Position.NO_PIECE, 2, false));
            }
        }
    }

    private static void addMove(MoveList moves, int from, int to, int piece, int captured) {
        moves.add(Move.encode(from, to, piece, captured, Position.NO_PIECE, 0, false));
    }

    private static long getSideOccupied(Position position, boolean white) {
        long occupied = 0L;
        int start = white ? 0 : 6;

        for (int i = start; i < start + 6; i++) {
            occupied |= position.bitboards[i];
        }

        return occupied;
    }

    public static boolean isKingAttacked(Position position, boolean whiteToMove) {
        int king = whiteToMove ? Position.WHITE_KING : Position.BLACK_KING;
        long kingBoard = position.bitboards[king];

        if (kingBoard == 0) return true;

        int square = Long.numberOfTrailingZeros(kingBoard);
        return isSquareAttacked(position, square, !whiteToMove);
    }

    public static boolean isSquareAttacked(Position position, int square, boolean byWhite) {
        int pawn     = byWhite ? Position.WHITE_PAWN     : Position.BLACK_PAWN;
        int knight   = byWhite ? Position.WHITE_KNIGHT   : Position.BLACK_KNIGHT;
        int bishop   = byWhite ? Position.WHITE_BISHOP   : Position.BLACK_BISHOP;
        int rook     = byWhite ? Position.WHITE_ROOK     : Position.BLACK_ROOK;
        int queen    = byWhite ? Position.WHITE_QUEEN    : Position.BLACK_QUEEN;
        int king     = byWhite ? Position.WHITE_KING     : Position.BLACK_KING;

        long pawnAttacks = MoveLookup.lookupPawnMask(square, true, byWhite);
        if ((pawnAttacks & position.bitboards[pawn]) != 0) return true;

        long knightAttacks = MoveLookup.lookupMask(1, square, position.getOccupied());
        if ((knightAttacks & position.bitboards[knight]) != 0) return true;

        long kingAttacks = MoveLookup.lookupMask(5, square, position.getOccupied());
        if ((kingAttacks & position.bitboards[king]) != 0) return true;

        long bishopAttacks = MoveLookup.lookupMask(2, square, position.getOccupied());
        if ((bishopAttacks & (position.bitboards[bishop] | position.bitboards[queen])) != 0) return true;

        long rookAttacks = MoveLookup.lookupMask(3, square, position.getOccupied());
        return (rookAttacks & (position.bitboards[rook] | position.bitboards[queen])) != 0;
    }

    private static final class MoveList {

        private int[] moves;
        private int size;

        MoveList() {
            this(64);
        }

        MoveList(int capacity) {
            moves = new int[Math.max(1, capacity)];
        }

        void add(int move) {
            if (size == moves.length) {
                int[] expanded = new int[moves.length * 2];
                System.arraycopy(moves, 0, expanded, 0, moves.length);
                moves = expanded;
            }

            moves[size++] = move;
        }

        int[] toArray() {
            int[] result = new int[size];
            System.arraycopy(moves, 0, result, 0, size);
            return result;
        }
    }
}