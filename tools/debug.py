#!/usr/bin/env python3

import sys
import argparse


# ============================================================
# Bitboard utilities
# ============================================================

def parse_int(value: str) -> int:
    """
    Parse a signed decimal or hexadecimal integer.

    Examples:
        123
        -123
        0x1234
        -0x1234
    """
    value = value.strip()

    negative = value.startswith("-")
    if negative:
        value = value[1:]

    if value.lower().startswith("0x"):
        number = int(value, 16)
    else:
        number = int(value, 10)

    return -number if negative else number


def to_unsigned_64(value: int) -> int:
    """
    Convert a signed integer to its 64-bit two's-complement
    representation.
    """
    return value & 0xFFFFFFFFFFFFFFFF


def square_int(alg: str) -> int:
    """Convert algebraic notation to a 0-63 square index."""
    return (int(alg[1]) - 1) * 8 + (ord(alg[0].lower()) - ord('a'))


def square_name(square: int) -> str:
    """Convert a 0-63 square index to algebraic notation."""
    file = square % 8
    rank = square // 8
    return f"{chr(ord('a') + file)}{rank + 1}"


def piece_name(piece: int) -> str:
    """Convert a piece index to piece name"""
    if piece == 0:
        return  "white pawn"
    if piece == 1:
        return  "white knight"
    if piece == 2:
        return  "white bishop"
    if piece == 3:
        return  "white rook"
    if piece == 4:
        return  "white queen"
    if piece == 5:
        return  "white king"
    if piece == 6:
        return  "black pawn"
    if piece == 7:
        return  "black knight"
    if piece == 8:
        return  "black bishop"
    if piece == 9:
        return  "black rook"
    if piece == 10:
        return "black queen"
    if piece == 11:
        return "black king"
    if piece == 15:
        return "none"
    return "unknown"


def castle_name(castle: int) -> int:
    if castle == 0:
        return "none"
    if castle == 1:
        return "king side"
    if castle == 2:
        return "queen side"
    return unknown


def to_signed_32(value: int) -> int:
    """Interpret a value as a signed Java 32-bit int."""
    value &= 0xFFFFFFFF

    if value & 0x80000000:
        return value - 0x100000000

    return value


def print_sq(value):
    if type(value) is str:
        print(f"Square index: {square_int(value)}")
    if type(value) is int:
        print(f"Algebraic notation: {square_name(value)}")


def print_fen(fen: str):
    """Print a chess position represented by a FEN string."""

    fields = fen.strip().split()

    if len(fields) != 6:
        raise ValueError(
            "FEN must contain 6 fields: "
            "board side castling en-passant halfmove fullmove"
        )

    board, side, castling, ep, halfmove, fullmove = fields

    ranks = board.split("/")

    if len(ranks) != 8:
        raise ValueError("FEN board must contain 8 ranks")

    print()
    print(f"FEN: {fen}")
    print()

    print("    a b c d e f g h")
    print("  +-----------------+")

    for rank_index, rank_data in enumerate(ranks):
        rank = 8 - rank_index
        row = []

        for char in rank_data:
            if char.isdigit():
                row.extend(["."] * int(char))
            elif char in "PNBRQKpnbrqk":
                row.append(char)
            else:
                raise ValueError(f"Invalid FEN piece: {char}")

        if len(row) != 8:
            raise ValueError(
                f"Rank {rank} contains {len(row)} squares instead of 8"
            )

        print(f"{rank} | {' '.join(row)} |")

    print("  +-----------------+")
    print("    a b c d e f g h")
    print()

    print("Position information:")
    print(f"  Side to move:    {'White' if side == 'w' else 'Black'}")
    print(f"  Castling rights: {castling}")
    print(f"  En passant:      {ep}")
    print(f"  Halfmove clock:  {halfmove}")
    print(f"  Fullmove number:  {fullmove}")
    print()


def print_move(value: int):
    """Decode and visualize an encoded Move."""

    # Java int is exactly 32 bits.
    encoded = value & 0xFFFFFFFF
    signed = to_signed_32(encoded)

    sq1 = encoded & 0x3F
    sq2 = (encoded >> 6) & 0x3F
    piece = (encoded >> 12) & 0xF
    cap_piece = (encoded >> 16) & 0xF
    prom_piece = (encoded >> 20) & 0xF
    castle = (encoded >> 24) & 0x3
    ep = (encoded >> 26) & 0x1

    print()
    print(f"Decimal: {signed}")
    print(f"Hex:     0x{encoded:08X}")
    print(f"Binary:  {encoded:032b}")
    print()

    print("Move fields:")
    print(f"  Sq1:      {sq1:2}  ({square_name(sq1)})")
    print(f"  Sq2:      {sq2:2}  ({square_name(sq2)})")
    print(f"  Piece:     {piece}  ({piece_name(piece)})")
    print(f"  CapPiece:  {cap_piece}  ({piece_name(cap_piece)})")
    print(f"  PromPiece: {prom_piece}  ({piece_name(prom_piece)})")
    print(f"  Castle:    {castle}  ({castle_name(castle)})")
    print(f"  EP:        {ep}  ({ep == 1})")

    print()
    print("Bit layout:")
    print("  EP  Castle  PromPiece  CapPiece  Piece  Sq2  Sq1")
    print("  26    24       20         16       12    6    0")
    print()


def print_bitboard(value: int, coordinates: bool = True):
    """
    Print a 64-bit bitboard.

    Bit 0  = A1
    Bit 7  = H1
    Bit 56 = A8
    Bit 63 = H8
    """

    bb = to_unsigned_64(value)

    print()
    print(f"Decimal: {value}")
    print(f"Hex:     0x{bb:016X}")
    print()

    if coordinates:
        print("    a b c d e f g h")

    print("  +-----------------+")

    # Rank 8 -> rank 1
    for rank in range(7, -1, -1):
        row = []

        for file in range(8):
            square = rank * 8 + file
            bit = (bb >> square) & 1
            row.append("1" if bit else ".")

        print(f"{rank + 1} | {' '.join(row)} |")

    print("  +-----------------+")

    if coordinates:
        print("    a b c d e f g h")

    print()


def print_bitboard_bits(value: int):
    """Print the raw 64-bit binary representation."""

    bb = to_unsigned_64(value)

    print()
    print(f"0x{bb:016X}")
    print()

    for rank in range(7, -1, -1):
        start = rank * 8
        bits = ""

        for file in range(8):
            square = start + file
            bits += str((bb >> square) & 1)

        print(bits)

    print()


def list_set_squares(value: int):
    """Print all squares whose bits are set."""

    bb = to_unsigned_64(value)

    squares = []

    for square in range(64):
        if bb & (1 << square):
            file = square % 8
            rank = square // 8

            squares.append(
                f"{chr(ord('a') + file)}{rank + 1}"
            )

    print("Set bits:")

    if squares:
        print(" ".join(squares))
    else:
        print("(none)")


def popcount(value: int):
    """Count set bits."""

    bb = to_unsigned_64(value)
    print(f"Popcount: {bb.bit_count()}")


def lsb(value: int):
    """Print the least-significant set bit."""

    bb = to_unsigned_64(value)

    if bb == 0:
        print("LSB: none")
        return

    index = (bb & -bb).bit_length() - 1

    file = index % 8
    rank = index // 8

    print(f"LSB index: {index}")
    print(f"LSB square: {chr(ord('a') + file)}{rank + 1}")


def msb(value: int):
    """Print the most-significant set bit."""

    bb = to_unsigned_64(value)

    if bb == 0:
        print("MSB: none")
        return

    index = bb.bit_length() - 1

    file = index % 8
    rank = index // 8

    print(f"MSB index: {index}")
    print(f"MSB square: {chr(ord('a') + file)}{rank + 1}")


# ============================================================
# Commands
# ============================================================
def command_sq(args):
    try:
        value = parse_int(args.value)
    except ValueError:
        value = args.value

    print_sq(value)

def command_fen(args):
    print_fen(args.value)

def command_move(args):
    value = parse_int(args.value)
    print_move(value)


def command_bb(args):
    value = parse_int(args.value)
    print_bitboard(value)


def command_bits(args):
    value = parse_int(args.value)
    print_bitboard_bits(value)


def command_squares(args):
    value = parse_int(args.value)
    list_set_squares(value)


def command_popcount(args):
    value = parse_int(args.value)
    popcount(value)


def command_lsb(args):
    value = parse_int(args.value)
    lsb(value)


def command_msb(args):
    value = parse_int(args.value)
    msb(value)


# ============================================================
# CLI
# ============================================================

def build_parser():
    parser = argparse.ArgumentParser(
        description="Chess engine debugging toolkit"
    )

    subparsers = parser.add_subparsers(
        dest="command",
        required=True
    )

    # fen
    p = subparsers.add_parser(
        "fen",
        help="Visualize a FEN position"
    )
    p.add_argument(
        "value",
        help="FEN string"
    )
    p.set_defaults(func=command_fen)

    # sq
    p = subparsers.add_parser(
        "sq",
        help="Convert between algebraic notation and 0-63 square indexes both ways"
    )
    p.add_argument(
        "value",
        help="Signed decimal or hexadecimal square index"
    )
    p.set_defaults(func=command_sq)

    # mv
    p = subparsers.add_parser(
        "mv",
        help="Decode and visualize an encoded chess move"
    )
    p.add_argument(
        "value",
        help="Signed decimal or hexadecimal encoded Move"
    )
    p.set_defaults(func=command_move)

    # bb
    p = subparsers.add_parser(
        "bb",
        help="Visualize a bitboard"
    )
    p.add_argument(
        "value",
        help="Decimal or hexadecimal signed long"
    )
    p.set_defaults(func=command_bb)

    # bits
    p = subparsers.add_parser(
        "bits",
        help="Print raw 64-bit representation"
    )
    p.add_argument("value")
    p.set_defaults(func=command_bits)

    # squares
    p = subparsers.add_parser(
        "squares",
        help="List squares represented by set bits"
    )
    p.add_argument("value")
    p.set_defaults(func=command_squares)

    # popcount
    p = subparsers.add_parser(
        "popcount",
        help="Count set bits"
    )
    p.add_argument("value")
    p.set_defaults(func=command_popcount)

    # lsb
    p = subparsers.add_parser(
        "lsb",
        help="Find least-significant set bit"
    )
    p.add_argument("value")
    p.set_defaults(func=command_lsb)

    # msb
    p = subparsers.add_parser(
        "msb",
        help="Find most-significant set bit"
    )
    p.add_argument("value")
    p.set_defaults(func=command_msb)

    return parser


def main():
    parser = build_parser()
    args = parser.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()