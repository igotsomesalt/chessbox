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