package chessbox.game;

import java.util.UUID;
import java.util.function.Supplier;

import chessbox.chess.Position;

public class GenericPlayer implements Player {

    private Supplier<String> player;
    private String name;
    private UUID id = UUID.randomUUID();

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

    @Override 
    public UUID id() {
        return id;
    }
}
