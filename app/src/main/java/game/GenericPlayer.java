package game;

import java.util.function.Supplier;

import chess.Position;

public class GenericPlayer implements Player {

    private Supplier<String> player;
    private String name;

    public GenericPlayer(String name, Supplier<String> player) {
        this.player = player;
    }

    @Override
    public String getMove(Position position) {
        return player.get();
    }

    @Override 
    public String name() {
        return name;
    }
}
