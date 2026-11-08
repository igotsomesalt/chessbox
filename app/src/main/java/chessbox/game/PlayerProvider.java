package chessbox.game;

public interface PlayerProvider {
    Player acquire() throws Exception;
    void release(Player player) throws Exception;
}
