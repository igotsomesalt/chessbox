package chessbox.game;

import java.util.UUID;

import chessbox.opening.OpeningBook;
import chessbox.util.Indentifiable;

public record MatchConfig(UUID id, PlayerProvider p1, PlayerProvider p2, OpeningBook openingBook) implements Indentifiable<UUID> {}
