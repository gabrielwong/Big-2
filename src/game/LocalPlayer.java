package game;

import deck.Card;

/**
 * Represents the local player. This is only used for single player games.
 * @author Gabriel
 *
 */
public class LocalPlayer extends AbstractPlayer implements CardSelectionReceiver{
	private Combination selectedCombination;
	private GameState state = null;
	private Thread gameLoopThread;

	@Override
	/**
	 * Wait for user input and return the selected combination. This will lock up the thread.
	 */
	public Combination doTurn(GameState state) {
		this.state = state;
		gameLoopThread = Thread.currentThread();

		try {
			Thread.sleep(Long.MAX_VALUE); // wait until interrupted
		} catch (InterruptedException e) {} // continue

		Combination c = selectedCombination; // temp storage
		selectedCombination = null; // reset selected combination
		this.state = null;
		return c;
	}

	/**
	 * Receives input and checks if it is valid in the game state.
	 */
	public void receiveInput(Combination combination) throws InvalidCombinationException{
		if (state == null)
			return;
		
		// Check for same length
		if (combination.getLength() != state.getPreviousPlay().getLength() && state.getPreviousPlay().getLength() != Combination.PASS)
			throw new InvalidCombinationException("You must play the same number of cards as the cards on the table."); // not the same type
		
		// Check if it has the forced card
		if (state.getForcedCard() != null){
			boolean hasCard = false;
			for (Card c : combination.getCards()){
				if (state.getForcedCard().equals(c)){
					hasCard = true;
					break;
				}
			}
			if (! hasCard)
				throw new InvalidCombinationException("You must have a " + state.getForcedCard().toString() + 
						" in your combination.");
		}
		
		// Checks if the card is higher than the last one.
		if (combination.getValue() < state.getPreviousPlay().getValue())
			throw new InvalidCombinationException("The cards selected are of less value than the previously played cards.");
		selectedCombination = combination;
		
		gameLoopThread.interrupt(); // interrupt the game loop thread
	}
	
	/**
	 * Receives input and checks if it is valid in the game state.
	 */
	public void receiveInput(boolean[] selected) throws InvalidCombinationException{
		if (state == null)
			return;

		// For a pass
		if (selected == null){
			// If passing on the first play in a trick
			if (state.getPreviousPlay() == Combination.getPassCombination())
				throw new InvalidCombinationException("You cannot pass on a new trick.");
			selectedCombination = Combination.getPassCombination();
			gameLoopThread.interrupt();
		} else {
			int nSelected = 0; // number of cards that are selected

			// Count number of cards that are selected
			for(int i = 0; i < size(); i++)
				if (selected[i])
					nSelected++;

			// Check for valid length
			if (nSelected == 0)
				throw new InvalidCombinationException("No cards were selected.");	// no cards selected, nothing happens
			if (nSelected != state.getPreviousPlay().getLength() && state.getPreviousPlay().getLength() != Combination.PASS)
				throw new InvalidCombinationException("You must play the same number of cards as the cards on the table."); // not the same type

			Card[] cards = new Card[nSelected]; // array of selected cards

			int j = 0; // index of cards
			for (int i = 0; i < size() && j < nSelected; i++){ // fill cards
				if (selected[i]){
					cards[j] = get(i);
					j++;
				}
			}

			// Check for forced card
			if (state.getForcedCard() != null){
				boolean hasCard = false;
				for (int i = 0; i < cards.length; i++){
					if (state.getForcedCard().equals(cards[i])){
						hasCard = true;
						break;
					}
				}
				if (! hasCard)
					throw new InvalidCombinationException("You must have a " + state.getForcedCard().toString() + 
							" in your combination.");
			}

			// Check if the selected combination is higher than the previously played one
			Combination selectedCombination = Combination.getCombination(cards); // get combination and set it so do turn can continue
			if (selectedCombination.getValue() < state.getPreviousPlay().getValue())
				throw new InvalidCombinationException("The cards selected are of less value than the previously played cards.");

			this.selectedCombination = selectedCombination;
			gameLoopThread.interrupt(); // interrupt game loop thread
		}
	}
}

