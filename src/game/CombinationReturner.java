package game;

/**
 * Used when a class should be able to return a combination (simulate a player's turn)
 * @author Gabriel
 *
 */
public interface CombinationReturner {
	/**
	 * Simulates one turn of the player.
	 * @param state the current state of the game
	 * @return the combination of cards played by the player
	 */
	public Combination doTurn (GameState state);
}
