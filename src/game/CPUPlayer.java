package game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import deck.Card;
import deck.Rank;
import deck.Suit;

/**
 * A computer player.
 * @author Gabriel
 *
 */
public class CPUPlayer extends AbstractPlayer implements Serializable{
	private ArrayList<Combination.PokerHand> pokerHands; // array of available poker hands
	private ArrayList<Combination.Triple> triples;
	private ArrayList<Combination.Double> doubles;
	private ArrayList<Combination.Single> singles;

	@Override
	public Combination doTurn(GameState state) {
		if (hand.size() == 0)
			return Combination.getPassCombination();

		searchForCombinations();

		// Remove combinations without the forced card, if the is one
		if (state.getForcedCard() != null){
			for (int i = 0; i < pokerHands.size(); i++){
				boolean hasForcedCard = false;
				for (Card c : pokerHands.get(i).getCards()){
					if (c.equals(state.getForcedCard())){
						hasForcedCard = true;
					}
				}
				if (! hasForcedCard){
					pokerHands.remove(i);
					i--;
				}
			}
			for (int i = 0; i < triples.size(); i++){
				boolean hasForcedCard = false;
				for (Card c : triples.get(i).getCards()){
					if (c.equals(state.getForcedCard())){
						hasForcedCard = true;
					}
				}
				if (! hasForcedCard){
					triples.remove(i);
					i--;
				}
			}
			for (int i = 0; i < doubles.size(); i++){
				boolean hasForcedCard = false;
				for (Card c : doubles.get(i).getCards()){
					if (c.equals(state.getForcedCard())){
						hasForcedCard = true;
					}
				}
				if (! hasForcedCard){
					doubles.remove(i);
					i--;
				}
			}
			for (int i = 0; i < singles.size(); i++){
				boolean hasForcedCard = false;
				for (Card c : singles.get(i).getCards()){
					if (c.equals(state.getForcedCard())){
						hasForcedCard = true;
					}
				}
				if (! hasForcedCard){
					singles.remove(i);
					i--;
				}
			}
		}
		// If can play any card, play in decreasing order of length and increasing order of value
		if (state.getPreviousPlay() == Combination.getPassCombination()){

			// Play the lowest value poker hand using the cheapest cards
			if (pokerHands.size() > 0)
				return pokerHands.get(0);

			if (triples.size() > 0){
				return triples.get(0);
			}
			if (doubles.size() > 0){
				return doubles.get(0);
			}

			if (singles.size() > 0)
				return singles.get(0);

			Card[] cards = {hand.get(0)};
			try {
				return Combination.getCombination(cards);
			} catch (InvalidCombinationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// If there is card already played, play the lowest valued combination possible
		switch (state.getPreviousPlay().getLength()){
		case Combination.SINGLE :
			for (int i = 0; i < singles.size(); i++){
				if (singles.get(i).getValue() > state.getPreviousPlay().getValue())
					return singles.get(i);
			}
			break;
		case Combination.DOUBLE :
			for (int i = 0; i < doubles.size(); i++)
				if (doubles.get(i).getValue() > state.getPreviousPlay().getValue())
					return doubles.get(i);
			break;
		case Combination.TRIPLE :
			for (int i = 0; i < triples.size(); i++)
				if (triples.get(i).getValue() > state.getPreviousPlay().getValue())
					return triples.get(i);
			break;
		case Combination.POKER_HAND :
			for (int i = 0; i < pokerHands.size(); i++)
				if (pokerHands.get(i).getValue() > state.getPreviousPlay().getValue())
					return pokerHands.get(i);
			break;
		}

		return Combination.getPassCombination(); // pass if can do nothing
	}

	@SuppressWarnings("unchecked")
	/**
	 * Search for available combinations of poker hands, triples, doubles and singles.
	 */
	private void searchForCombinations(){
		ArrayList<Card> cardsLeft = hand.toArrayList(); // copy of hand
		Collections.sort(cardsLeft);

		pokerHands = searchForPokerHands(cardsLeft, true);
		triples = searchForTriples(cardsLeft, true);
		doubles = searchForDoubles(cardsLeft, true);
		singles = searchForSingles(cardsLeft, true);

		Collections.sort(pokerHands);
		Collections.sort(triples);
		Collections.sort(doubles);
		Collections.sort(singles);
	}

	@SuppressWarnings("unchecked")
	/**
	 * Search for poker hands.
	 * @param hand the ArrayList to search
	 * @param removeCards whether the cards that are found should be removed
	 * @return the combinations that were found
	 */
	private ArrayList<Combination.PokerHand> searchForPokerHands(ArrayList<Card> hand, boolean removeCards){
		if (!removeCards){
			hand = (ArrayList<Card>) hand.clone(); // copy of hand
			Collections.sort(hand);
		}

		ArrayList<Combination.PokerHand> pokerHands = new ArrayList<Combination.PokerHand>(); 

		pokerHands.addAll(searchForStraightFlushes(hand, true));
		pokerHands.addAll(searchForQuads(hand, true));
		pokerHands.addAll(searchForFullHouses(hand, true));
		pokerHands.addAll(searchForFlushes(hand, true));
		pokerHands.addAll(searchForStraights(hand, true));

		return pokerHands;
	}

	/**
	 * Search for straights.
	 * @param hand the ArrayList to search
	 * @param removeCards whether the cards that are found should be removed
	 * @return the combinations that were found
	 */
	private ArrayList<Combination.PokerHand> searchForStraights(ArrayList<Card> hand, boolean removeCards){
		if (hand.size() < 5)
			return new ArrayList<Combination.PokerHand>();

		ArrayList<Combination.PokerHand> straights = new ArrayList<Combination.PokerHand>(); // array of straights
		// Search for straights
		int consecutive = 1;
		int previousRank = hand.get(0).rank.ordinal();
		for (int i = 1; i < hand.size() - 1; i++){
			if (hand.get(i).rank.ordinal() - 1 == previousRank){
				previousRank = hand.get(i).rank.ordinal();
				consecutive++;
			} else if (hand.get(i).rank.ordinal() > previousRank + 1){
				previousRank = hand.get(i).rank.ordinal();
				consecutive = 1;
			}
			if (consecutive >= 5){
				Card[] cards = new Card[5];

				// Find consecutive cards
				int nextRank = previousRank - 4;
				int k = 0; // number already added
				for (int j = 0; j < hand.size() && k < 5; j++){
					if (hand.get(j).rank.ordinal() == nextRank){
						cards[k] = hand.get(j);
						nextRank++;
						k++;
					}
				}

				// Get straight
				try {
					straights.add((Combination.PokerHand)Combination.getCombination(cards));
				} catch (InvalidCombinationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullPointerException e){
					e.printStackTrace();
				}

				if (removeCards){
					for (Card c : cards){
						hand.remove(c);
					}
				}
			}
		}

		return straights;
	}

	/**
	 * Search for flushes.
	 * @param hand the ArrayList to search
	 * @param removeCards whether the cards that are found should be removed
	 * @return the combinations that were found
	 */
	private ArrayList<Combination.PokerHand> searchForFlushes(ArrayList<Card> hand, boolean removeCards){
		if (hand.size() < 5)
			new ArrayList<Combination.PokerHand>();

		ArrayList<Combination.PokerHand> flushes = new ArrayList<Combination.PokerHand>(); // array of flushes

		ArrayList<ArrayList<Card>> suits = new ArrayList<ArrayList<Card>>(Suit.values().length); // array of arrays of cards by suit
		for (int i = 0; i < Suit.values().length; i++)
			suits.add(new ArrayList<Card>()); // create arrays

		// Add all cards to the appropriate arrayList by suit
		for (int i = 0; i < hand.size(); i++){
			suits.get(hand.get(i).suit.ordinal()).add(hand.get(i));
		}

		// Process each suit
		for (int i = 0; i < suits.size(); i++){
			// Has flush if 5 or more cards of same suit
			if (suits.get(i).size() >= 5){
				// Add flushes to combination array
				for (int j = 0; j + 5 < suits.get(i).size(); j++){
					Card[] cards = new Card[5];
					for (int k = 0; k < 5; k++){
						cards[k] = suits.get(i).get(j + k);
					}
					try {
						flushes.add((Combination.PokerHand)Combination.getCombination(cards));
					} catch (InvalidCombinationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				// Remove 5 lowest cards of a suit with 5 or more cards
				if (removeCards){
					int nRemoved = 0;
					for (int k = 0; k < hand.size(); k++){
						boolean removed = true; // a card was removed
						// process two or more consecutive cards with the same suit
						while (removed && nRemoved < 5 && k < hand.size()){
							removed = false;
							if (hand.get(k).suit.ordinal() == i){
								removed = true;
								hand.remove(k);
								nRemoved++;
							}
						}
					}
				}
			}
		}

		return flushes;
	}

	/**
	 * Search for full houses.
	 * @param hand the ArrayList to search
	 * @param removeCards whether the cards that are found should be removed
	 * @return the combinations that were found
	 */
	private ArrayList<Combination.PokerHand> searchForFullHouses(ArrayList<Card> hand, boolean removeCards){
		if (hand.size() < 5)
			return new ArrayList<Combination.PokerHand>();

		ArrayList<Combination.PokerHand> fullHouse = new ArrayList<Combination.PokerHand>(); // array of full houses

		// Search for full house
		@SuppressWarnings("unchecked")
		ArrayList<Card> handClone = (ArrayList<Card>)hand.clone(); // clone hand to avoid removing cards from hand
		ArrayList<Combination.Triple> triples = searchForTriples(handClone, true); // search for triples and remove them from future search
		ArrayList<Combination.Double> doubles = searchForDoubles(handClone, true); // search for doubles not part of triples

		// Combine doubles and triples into poker hands
		for (int i = 0; i < triples.size() & i < doubles.size(); i++){
			for (int j = 0; j < doubles.size() && j < triples.size(); j++){
				Card[] cards = new Card[5];
				Card[] tripleCards = triples.get(i).getCards();
				Card[] doubleCards = doubles.get(i).getCards();

				// Add triple to cards
				for (int k = 0; k < tripleCards.length; k++)
					cards[k] = tripleCards[k];

				// Add double to cards
				for (int k = 0; k < doubleCards.length; k++)
					cards[k + tripleCards.length] = doubleCards[k];

				try {
					fullHouse.add((Combination.PokerHand)Combination.getCombination(cards));
				} catch (InvalidCombinationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// Removes all doubles and triples if there is more than 1 of each
		if (removeCards && triples.size() >= 1 && doubles.size() >= 1){
			// Remove triples
			for (int i = 0; i < triples.size() & i < doubles.size(); i++){
				Card[] cards = triples.get(i).getCards();
				for (int j = 0; j < cards.length; j++)
					hand.remove(cards[j]);
			}

			// Remove doubles
			for (int i = 0; i < doubles.size() & i < triples.size(); i++){
				Card[] cards = doubles.get(i).getCards();
				for (int j = 0; j < cards.length; j++)
					hand.remove(cards[j]);
			}
		}

		return fullHouse;
	}

	/**
	 * Search for four of a kinds.
	 * @param hand the ArrayList to search
	 * @param removeCards whether the cards that are found should be removed
	 * @return the combinations that were found
	 */
	private ArrayList<Combination.PokerHand> searchForQuads(ArrayList<Card> hand, boolean removeCards){
		if (hand.size() < 5)
			return new ArrayList<Combination.PokerHand>();

		ArrayList<Combination.PokerHand> quads = new ArrayList<Combination.PokerHand>(); // array of full houses

		// Search for four of a kind
		for (int i = 0; i < hand.size() - 3; i++){
			// If there is four of the same rank
			if (hand.get(i).rank == hand.get(i + 1).rank && hand.get(i).rank == hand.get(i + 2).rank && hand.get(i).rank == hand.get(i + 3).rank){
				Card[] cards = new Card[5];

				// populate cards with the quad
				for (int k = 0; k < 4; k++)
					cards[k] = hand.get(i + k);

				// add poker hand with single card before the quad
				for (int j = 0; j < hand.size(); j++){
					if (hand.get(j).rank != hand.get(i).rank){ // if the card is not part of the quad
						cards[4] = hand.get(j);
						try {
							quads.add((Combination.PokerHand) Combination.getCombination(cards));
						} catch (InvalidCombinationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				// Removes only the quad
				if (removeCards){
					for (int j = 0; j < 4; j++)
						hand.remove(i);
				}
			}
		}

		return quads;
	}

	/**
	 * Search for straight flushes.
	 * @param hand the ArrayList to search
	 * @param removeCards whether the cards that are found should be removed
	 * @return the combinations that were found
	 */
	private ArrayList<Combination.PokerHand> searchForStraightFlushes(ArrayList<Card> hand, boolean removeCards){
		if (hand.size() < 5)
			return new ArrayList<Combination.PokerHand>();

		ArrayList<Combination.PokerHand> straightFlushes = new ArrayList<Combination.PokerHand>(); // array of straightFlushes

		// Search for straight flush
		int consecutive = 1;
		int previousRank = hand.get(0).rank.ordinal();
		Suit previousSuit = hand.get(0).suit;
		for (int i = 1; i < hand.size() - 1; i++){
			if (hand.get(i).rank.ordinal() - 1 == previousRank && hand.get(i).suit == previousSuit){
				previousRank = hand.get(i).rank.ordinal();
				previousSuit = hand.get(i).suit;
				consecutive++;
			} else if (hand.get(i).rank.ordinal() > previousRank + 1){
				consecutive = 1;
				previousRank = hand.get(i).rank.ordinal();
			}
			if (consecutive >= 5){
				Card[] cards = new Card[5];

				// Find consecutive cards
				int nextRank = previousRank - 4;
				final Suit suit = hand.get(i).suit;
				int k = 0; // number already added
				for (int j = 0; j < hand.size(); j++){
					if (hand.get(j).rank.ordinal() == nextRank && suit == hand.get(i).suit){
						cards[k] = hand.get(j);
						nextRank++;
						k++;
					}
				}

				// Get straight
				try {
					straightFlushes.add((Combination.PokerHand)Combination.getCombination(cards));
				} catch (InvalidCombinationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (removeCards){
					for (Card c : cards){
						hand.remove(c);
					}
				}
			}
		}

		return straightFlushes;
	}

	/**
	 * Search for triples.
	 * @param hand the ArrayList to search
	 * @param removeCards whether the cards that are found should be removed
	 * @return the combinations that were found
	 */
	private ArrayList<Combination.Triple> searchForTriples(ArrayList<Card> hand, boolean removeCards){
		ArrayList<Combination.Triple> triples = new ArrayList<Combination.Triple>();

		// Go through each card looking at the 2 cards before it and seeing if they make a triple
		for (int i = 0; i < hand.size() - 2; i++){
			Card[] cards = new Card[Combination.TRIPLE];
			cards[0] = hand.get(i);
			cards[1] = hand.get(i + 1);
			cards[2] = hand.get(i + 2);
			try{
				triples.add((Combination.Triple)Combination.getCombination(cards)); // if it is a triple, add it
				if (removeCards){
					for (int j = 0; j < 3; j++)
						hand.remove(i); // remove it from the arraylist
				}
			} catch (InvalidCombinationException e){} // not a triple, don't add it
		}
		return triples;
	}
	
	/**
	 * Search for doubles.
	 * @param hand the ArrayList to search
	 * @param removeCards whether the cards that are found should be removed
	 * @return the combinations that were found
	 */
	private ArrayList<Combination.Double> searchForDoubles(ArrayList<Card> hand, boolean removeCards){
		ArrayList<Combination.Double> doubles = new ArrayList<Combination.Double>();

		// Go through each card looking at the card before it to check if it's a double
		for (int i = 0; i < hand.size() - 1; i++){
			Card[] cards = new Card[2];
			cards[0] = hand.get(i);
			cards[1] = hand.get(i + 1);

			try{
				doubles.add((Combination.Double)Combination.getCombination(cards)); // if it is a double, add it
				if (removeCards){
					for (int j = 0; j < 2; j++)
						hand.remove(i); // remove from arraylist
				}
			} catch (InvalidCombinationException e){} // not a double, don't add it
		}
		return doubles;
	}
	
	/**
	 * Search for singles.
	 * @param hand the ArrayList to search
	 * @param removeCards whether the cards that are found should be removed
	 * @return the combinations that were found
	 */
	private ArrayList<Combination.Single> searchForSingles(ArrayList<Card> hand, boolean removeCards){
		ArrayList<Combination.Single> singles = new ArrayList<Combination.Single>();

		// All cards are singles
		for (int i = 0; i < hand.size(); i++){
			Card[] cards = {hand.get(i)};
			try {
				singles.add((Combination.Single)Combination.getCombination(cards));
			} catch (InvalidCombinationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (removeCards){
			hand.clear();
		}
		return singles;
	}

	public String toString() {
		return "[CPU Player]";
	}
	
	@Override
	public void receiveInput(boolean[] selected){}

	@Override
	public void receiveInput(Combination combination){}
}
