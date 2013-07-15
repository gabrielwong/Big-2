package game;

import java.io.Serializable;
import java.util.Arrays;

import deck.Card;

/**
 * A representation of the state of a game. This class contains all relevant information for
 *  a game to be displayed and for the gameplay logic.
 * @author Gabriel
 *
 */
public class GameState implements Serializable {
	private AbstractPlayer[] players = null;
	private Combination previousPlay = Combination.getPassCombination();
	private int currentPlayer = 0;
	private boolean[] playersPassed = null;
	private int lastPlayerPlayed = 0;
	private Card forcedCard = null;
	private int[] winOrder = null;

	/**
	 * Create a new game state with the given players.
	 * @param players the players in the game
	 */
	public GameState(AbstractPlayer[] players) {
		this.players = players;
		playersPassed = new boolean[players.length];
		resetPlayersPassed();
		winOrder = new int[players.length];
	}

	/**
	 * Returns the players.
	 * @return an array of the players
	 */
	public AbstractPlayer[] getPlayers() {
		return players;
	}

	/**
	 * Set the player at index.
	 * @param index the index the player should be at
	 * @param player the player to set
	 */
	public void setPlayer (int index, AbstractPlayer player){
		players[index] = player;
	}
	
	/**
	 * Returns a player at index.
	 * @param index the index to look at
	 * @return the player at index
	 */
	public AbstractPlayer getPlayer(int index) {
		return players[index];
	}

	/**
	 * Returns the player that we are waiting for to play a card.
	 * @return the current player
	 */
	public AbstractPlayer getCurrentPlayer() {
		return players[currentPlayer];
	}

	/**
	 * Sets the players.
	 * @param players the players to set
	 */
	protected void setPlayers(AbstractPlayer[] players) {
		this.players = players;
	}

	/**
	 * Returns the last combination played.
	 * @return the last combination played
	 */
	public Combination getPreviousPlay() {
		return previousPlay;
	}

	/**
	 * Sets the last combination played.
	 * @param previousPlay the last combination played
	 */
	protected void setPreviousPlay(Combination previousPlay) {
		this.previousPlay = previousPlay;
	}

	/**
	 * Returns the index of the player who should be playing a card.
	 * @return the index of the current player
	 */
	public int getCurrentPlayerIndex() {
		return currentPlayer;
	}

	/**
	 * Sets the index of the player who should be playing a card.
	 * @param currentPlayerIndex the index of the current player
	 */
	protected void setCurrentPlayerIndex(int currentPlayerIndex) {
		this.currentPlayer = currentPlayerIndex;
	}

	/**
	 * Increments the index of the current player.
	 */
	protected void incrementCurrentPlayer() {
		currentPlayer = (currentPlayer + 1) % 4;
	}

	/**
	 * Returns a boolean array containing information about which players passed this round.
	 * getPlayersPassed()[3] means that player at index 3 has passed.
	 * @return a boolean array of players that passed
	 */
	public boolean[] getPlayersPassed() {
		return playersPassed;
	}

	/**
	 * Returns whether a player has passed this round.
	 * @param player the index of the player in question
	 * @return whether the player has passed this round
	 */
	public boolean getPassed(int player) {
		return playersPassed[player];
	}

	/**
	 * Sets the players that passed this round.
	 * @param playersPassed a boolean array containing information about which players have passed
	 * @see #getPlayersPassed()
	 */
	protected void setPlayersPassed(boolean[] playersPassed) {
		this.playersPassed = playersPassed;
	}

	/**
	 * Sets whether a player has passed this round.
	 * @param passed whether the player has passed
	 * @param player the index of the player
	 */
	protected void setPassed(boolean passed, int player) {
		playersPassed[player] = passed;
	}

	/**
	 * Makes all players not passed this round.
	 */
	protected void resetPlayersPassed() {
		Arrays.fill(playersPassed, false);
	}

	/**
	 * Get the last player that played a card.
	 * @return the index of the last player that played a card.
	 */
	public int getLastPlayerPlayed() {
		return lastPlayerPlayed;
	}

	/**
	 * Sets the last player that played a card.
	 * @param lastPlayerPlayed the index of the last player that played a card
	 */
	protected void setLastPlayerPlayed(int lastPlayerPlayed) {
		this.lastPlayerPlayed = lastPlayerPlayed;
	}

	/**
	 * Get the number of players.
	 * @return the number of players
	 */
	public int getNumPlayers() {
		return players.length;
	}
	
	/**
	 * Sets the card which is forced (the current player must play this card).
	 * @param forcedCard the forced card
	 */
	protected void setForcedCard(Card forcedCard){
		this.forcedCard = forcedCard;
	}
	
	/**
	 * Returns the card that is forced.
	 * @return the forced card
	 */
	public Card getForcedCard(){
		return forcedCard;
	}
	
	/**
	 * Returns whether the game is over (all players but one are done discarding).
	 * @return whether the game is over
	 */
	public boolean isGameOver(){
		int playersDone = 0;
		for (AbstractPlayer p : getPlayers()){
			if (p.isDone()){
				playersDone++;
			}
		}
		if (playersDone >= getNumPlayers() - 1)
			return true;
		return false;
	}
	
	/**
	 * Resets the win order (makes all null)
	 */
	public void resetWinOrder(){
		winOrder = new int[players.length];
		Arrays.fill(winOrder, -1);
	}
	
	/**
	 * Adds the a player to the win order array.
	 * @param index the index to add
	 */
	public void addWinner(int index){
		for (int i = 0; i < winOrder.length; i++){
			if (winOrder[i] == -1){
				winOrder[i] = index;
				return;
			}
		}
	}
	
	/**
	 * Returns the order in which the players won.
	 * @return the order in which the players won
	 */
	public int[] getWinOrder(){
		return winOrder;
	}
	
	public String toString() {
		String playersString = "";
		for (AbstractPlayer p : players) {
			playersString += p;
		}
		String playersPassedString = "";
		for (boolean b : playersPassed) {
			playersPassedString += b;
		}
		return String
				.format("{Players: %s, Previous play: %s, "
						+ "Current player: %s, Players passed: %s, Last player played: %s}%n",
						playersString, previousPlay, currentPlayer,
						playersPassedString, lastPlayerPlayed);
	}
}
