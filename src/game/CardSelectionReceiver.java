package game;

/**
 * Used when a class needs to receive a selection of cards.
 * @author Gabriel
 *
 */
public interface CardSelectionReceiver {
	/**
	 * Receives input as an array of boolean. selected[i] means that the card at index i is selected.
	 * @param selected a boolean array containing information about which cards are selected
	 * @throws InvalidCombinationException
	 */
	public void receiveInput(boolean[] selected) throws InvalidCombinationException;
	/**
	 * Receives a selected combination. This is used for networking.
	 * @param combination the selected combination
	 * @throws InvalidCombinationException
	 */
	public void receiveInput(Combination combination) throws InvalidCombinationException;
}
