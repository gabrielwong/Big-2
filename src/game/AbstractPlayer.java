package game;

import java.io.Serializable;

import deck.Card;
import deck.Deck;

/**
 * This class is an abstract representation of a player. It contains a player's hand,
 * name and index.
 * @author Gabriel
 *
 */
public abstract class AbstractPlayer implements CombinationReturner,
		CardSelectionReceiver, Serializable {
	protected Deck hand;
	protected String name = "CPU"; // default
	protected int index;
	
	public AbstractPlayer() {
		hand = new Deck();
	}

	public AbstractPlayer(Card[] cards) {
		this();
		addAll(cards);
	}

	/**
	 * Add a card to the player's hand.
	 * @param card the card to be added
	 */
	public void add(Card card) {
		hand.add(card);
	}

	/**
	 * Add the cards in the array into the player's hand.
	 * @param cards the cards to be added
	 */
	public void addAll(Card[] cards) {
		for (Card c : cards)
			hand.add(c);
	}

	/**
	 * Sort the player's hand by value.
	 */
	public void sort() {
		hand.sort();
	}

	/**
	 * Whether the player is finished discarding their hand.
	 * @return if the player has no cards in their hand
	 */
	public boolean isDone() {
		return size() <= 0;
	}

	/**
	 * Returns the number of cards in the player's hand
	 * @return the number of cards in the hand
	 */
	public int size() {
		return hand.size();
	}

	/**
	 * Get the card in the player's hand at a certain index.
	 * @param index the index of the card
	 * @return the card at index
	 */
	public Card get(int index) {
		return hand.get(index);
	}

	/**
	 * Removes all cards from the player's hand.
	 */
	public void clear() {
		hand.clear();
	}

	/**
	 * Searches for the first index of a card.
	 * @param c the card to search for
	 * @return the index of c, -1 if not found
	 */
	public int search(Card c) {
		return hand.search(c);
	}

	/**
	 * Returns the cards in the player's hand.
	 * @return the cards in the player's hand
	 */
	public Card[] getCards() {
		return hand.toArray();
	}

	/**
	 * Return the player's hand as a deck.
	 * @return the player's hand
	 */
	public Deck getHand() {
		return hand;
	}

	/**
	 * Remove all cards in a Combination.
	 * @param combination the combination to remove
	 * @return combination
	 */
	public Combination removeCombination(Combination combination) {
		if (combination.getLength() != Combination.PASS)
			for (Card c : combination.cards) {
				hand.remove(c);
			}
		return combination;
	}

	/**
	 * Returns the name of the player
	 * @return name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of the player
	 * @param name name of the player
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * Returns the index of the player in the game state.
	 * @return index of the player
	 */
	public int getIndex() {
		return index;
	}
}
