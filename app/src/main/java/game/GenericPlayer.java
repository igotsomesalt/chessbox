package game;

import java.util.function.Supplier;

import chess.Position;

public class GenericPlayer implements Player {

    private Supplier<String> player;
    private String name;
    private int id;

    public GenericPlayer(String name, int id, Supplier<String> player) {
        this.player = player;
        this.id = id;
    }

    @Override
    public String getMove(Position position) {
        return player.get();
    }

    @Override 
    public String name() {
        return name;
    }

    @Override 
    public int id() {
        return id;
    }
}
