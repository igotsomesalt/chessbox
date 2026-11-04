package game;

import java.util.function.Supplier;

import chess.Position;

public class GenericPlayer implements Player {
    
    Supplier<String> player;

    public GenericPlayer(Supplier<String> player) {
        this.player = player;
    }

    @Override
    public String getMove(Position position) {
        return player.get();
    }
}
