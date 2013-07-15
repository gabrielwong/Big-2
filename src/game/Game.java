package game;

import java.util.ArrayList;

import network.NetworkPlayer;

import deck.Card;
import deck.Deck;
import deck.Rank;
import deck.Suit;

/**
 * Contains the logic for the gameplay. This class is responsible for asking players for their input,
 *  and notifying all players that the state of the game has changed.
 * @author Gabriel
 *
 */
public class Game implements Runnable{
	private ArrayList<GameStateChangeListener> gameStateChangeListeners;
	private GameState state;
	private final Card THREE_OF_DIAMONDS = new Card(Rank.THREE, Suit.DIAMONDS);

	/**
	 * Initializes a new game with the given players.
	 * @param players the players in the new game
	 */
	public Game(AbstractPlayer[] players){
		state = new GameState(players);
		gameStateChangeListeners = new ArrayList<GameStateChangeListener>();
		newGame();
	}

	/**
	 * Replaces a player with a given player.
	 * @param index the index of the player to be replaced
	 * @param player the player that substitutes the old player
	 */
	public void setPlayer (int index, AbstractPlayer player){
		// Finishes the player that is being replaced's turn.
		// This avoids the game loop from being locked due to the replaced player never giving input
		if (state.getCurrentPlayerIndex() == index && state.getCurrentPlayer() instanceof NetworkPlayer){
			((NetworkPlayer) state.getPlayer(index)).setSelectedCombination(player.doTurn(state));
		}
		state.setPlayer(index, player); // set the player
		notifyGameStateChangeListeners(); // notify listeners of the change
	}

	/**
	 * Creates a new single player game.
	 * @return the new game
	 */
	public static Game createSinglePlayerGame(){
		// Create players for a single player game
		AbstractPlayer[] players = new AbstractPlayer[4];
		players[0] = new LocalPlayer();
		players[0].setName("Player");
		for (int i = 1; i < players.length; i++)
			players[i] = new CPUPlayer();

		return new Game(players);
	}

	/**
	 * Resets the game state to simulate a new game.
	 */
	public void newGame(){
		// Allow first player to play anything
		state.setPreviousPlay(Combination.getPassCombination());

		// Reset the passed state of all players
		for (int i = 0; i < state.getNumPlayers(); i++)
			state.setPassed(false, i);

		// Remove all cards from the player's hands
		for (AbstractPlayer p : state.getPlayers()){
			p.clear();
		}

		deal(state); // Deal all players new cards

		// Make player with three of diamonds play first
		int index = search(new Card(Rank.THREE, Suit.DIAMONDS));
		if (index != -1)
			state.setCurrentPlayerIndex(index);

		notifyGameStateChangeListeners(); // Notify listeners of the new game
	}

	/**
	 * Searches for the player with a certain card.
	 * @param card the card to search for
	 * @return the index of the player with the card, -1 if there is none
	 */
	private int search(Card card){
		// Check all players for card
		for (int i = 0; i < state.getPlayers().length; i++){
			if (state.getPlayer(i).search(card) != -1)
				return i; // Return index of player if it is in their hand
		}
		return -1; // Otherwise return -1
	}
	
	/**
	 * Deals a full deck of cards
	 * @param state the GameState containing all the players
	 */
	private void deal(GameState state){
		// Create a new deck with shuffled cards
		Deck deck = new Deck(false);
		deck.addFullDeck();
		deck.shuffle();
		
		// Deal the cards to players
		for (int i = 0; i < 52; i++){
			state.getPlayer(i % state.getPlayers().length).add(deck.deal());
		}
		
		// Sort the players' hands
		for (AbstractPlayer p : state.getPlayers())
			p.sort();
	}

	/**
	 * Add a GameStateChangeListener. This listener will be informed when the game state is changed.
	 * @param listener the listener to be added
	 */
	public void addGameStateChangeListener(GameStateChangeListener listener){
		gameStateChangeListeners.add(listener);
		listener.gameStateChanged(state);
	}
	
	/**
	 * Remove a GameStateChangeListener.
	 * @param listener the listener to be removed
	 * @return if the listener was removed
	 */
	public boolean removeGameStateChangeListener(GameStateChangeListener listener){
		return gameStateChangeListeners.remove(listener);
	}
	
	/**
	 * Notify all added GameStateChangeListeners that the game state was changed.
	 */
	protected void notifyGameStateChangeListeners(){
		for (int i = 0; i < gameStateChangeListeners.size(); i++){
			gameStateChangeListeners.get(i).gameStateChanged(state);
		}
	}

	/**
	 * Start playing the game. To access this method, use run().
	 */
	private void runGameLoop(){
		int consecutivePasses = 0; // number of consecutive passes
		int lastPlayerPlaying = 0; // index of last player to play a card

		while (true){
			// Check if all players are done their cards
			if (state.isGameOver()){
				notifyGameStateChangeListeners();
				break;
			}
			
			if (! (state.getCurrentPlayer().isDone() || state.getPassed(state.getCurrentPlayerIndex())))
				notifyGameStateChangeListeners();

			Combination play; // The cards that the player is playing
			// If a player is done, they will always pass
			// You must pass if you have already passed that round
			if (state.getCurrentPlayer().isDone() || state.getPassed(state.getCurrentPlayerIndex()))
				play = Combination.getPassCombination();
			else{
				// Ask for player to give a card if they are not done
				// Get combination from player

				// If it is the first play, force 3 of diamonds
				if (state.getCurrentPlayer().search(THREE_OF_DIAMONDS) != -1)
					state.setForcedCard(THREE_OF_DIAMONDS);
				else
					state.setForcedCard(null);
				
				// Sleep thread for 1 seconds so player can view cards played
				if (state.getCurrentPlayer() instanceof CPUPlayer){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
				}
				// Ask for combination and remove combination
				play = state.getCurrentPlayer().doTurn(state);
				state.getCurrentPlayer().removeCombination(play);
			}

			if (play.getLength() == Combination.PASS){ // player passed or is done
				consecutivePasses++; // increment number of consecutive passes
				state.setPassed(true, state.getCurrentPlayerIndex());

				// If all except 1 passed
				if (consecutivePasses == state.getNumPlayers() - 1){
					notifyGameStateChangeListeners();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
					// Allow last player that played to play anything
					state.setPreviousPlay(Combination.getPassCombination());
					consecutivePasses = 0;
					state.setCurrentPlayerIndex(lastPlayerPlaying);
					state.resetPlayersPassed();
				} else {
					state.incrementCurrentPlayer();
				}
			} else { // a combination was played
				consecutivePasses = 0;
				state.setPreviousPlay(play);
				// Player just finished (Allow next player to play anything)
				if (state.getCurrentPlayer().isDone()){
					notifyGameStateChangeListeners();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
					state.incrementCurrentPlayer();
					state.setPreviousPlay(Combination.getPassCombination());
					state.resetPlayersPassed();
				} else {
					lastPlayerPlaying = state.getCurrentPlayerIndex();
					state.incrementCurrentPlayer(); // give turn to next player
				}
			}
		}
	}
	
	/**
	 * Get the game state.
	 * @return the game state
	 */
	public GameState getGameState () {
		return state;
	}
	
	/**
	 * This invokes runGameLoop() which starts the game. This method should be accessed in a new Thread,
	 *  not the event dispatch thread. This method will lock up the thread until the game is over.
	 */
	@Override
	public void run() {
		runGameLoop();
	}
}
