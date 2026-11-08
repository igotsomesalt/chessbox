package chessbox.game;

public class GameRuntimeException extends Exception {
    GameRuntimeException(Game game, Exception e) {
        super(String.format("%s\n%s\n%s\n%s", 
            e.getMessage(), game.getStartFen(), game.getFen(), game.getMoves()
        ));
    }
}
