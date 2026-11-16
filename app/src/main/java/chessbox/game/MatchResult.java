package chessbox.game;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import chessbox.opening.OpeningBook;

public record MatchResult(        
    int games, int threads, int completed, int failed, int cancelled,
    MatchStatus status, Map<String, Integer> outcomes, 
    OpeningBook openingBook, Duration duration, Instant timestamp
) {

}
