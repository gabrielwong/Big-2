package game;

/**
 * Used when a class needs to be notified when the game state is changed.
 * @author Gabriel
 *
 */
public interface GameStateChangeListener {
	/**
	 * Called when the game state is changed.
	 * @param state the new game state
	 */
	public void gameStateChanged(GameState state);
}
