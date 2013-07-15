package game;

import java.io.Serializable;
import java.util.Arrays;

import deck.Card;

/**
 * Representation of the cards a player can play in a single turn.
 * @author Gabriel
 *
 */
public abstract class Combination implements Comparable<Combination>,
		Serializable {
	/**
	 * The cards in the combination.
	 */
	protected final Card[] cards;
	/**
	 * A pass combination.
	 */
	private static final Pass pass = new Pass();

	/**
	 * Length of each type of combination.
	 */
	public static final int PASS = 0, SINGLE = 1, DOUBLE = 2, TRIPLE = 3,
			POKER_HAND = 5;

	/**
	 * Initializes the combination.
	 * @param cards the cards in the combination
	 */
	private Combination(Card[] cards) {
		this.cards = cards;
		Arrays.sort(cards); // sort the cards
	}

	/**
	 * Returns a pass combination.
	 * @return a pass combination
	 */
	public static Combination getPassCombination() {
		return pass;
	}

	/**
	 * Gets the combination made by the given cards.
	 * @param cards the cards in the combination
	 * @return the combination made with the given cards
	 * @throws InvalidCombinationException not a valid combination
	 */
	public static Combination getCombination(Card[] cards)
			throws InvalidCombinationException {
		// These can throw InvalidCombinationException for things other than length
		if (cards.length == PASS)
			return getPassCombination();
		if (cards.length == SINGLE)
			return new Single(cards);
		if (cards.length == DOUBLE)
			return new Double(cards);
		if (cards.length == TRIPLE)
			return new Triple(cards);
		if (cards.length == POKER_HAND)
			return new PokerHand(cards);

		// Cards of invalid length
		throw new InvalidCombinationException(
				"0, 1, 2, 3 or 5 cards must be selected. " + cards.length
						+ " cards were selected.");
	}

	/**
	 * Checks if the cards form a valid combination.
	 * @param cards the cards to be checked
	 * @return if the cards are a valid combination
	 */
	public static boolean isValid(Card[] cards) {
		if (cards == null || cards.length == 0)
			return true;
		return Single.isValid(cards) || Double.isValid(cards)
				|| Triple.isValid(cards) || PokerHand.isValid(cards);
	}

	/**
	 * Return a the type of combination as a string.
	 * @return the combination type
	 */
	public String getType() {
		switch (cards.length) {
		case PASS:
			return "Pass";
		case SINGLE:
			return "Single";
		case DOUBLE:
			return "Double";
		case TRIPLE:
			return "Triple";
		case POKER_HAND:
			return "Poker Hand";
		default:
			return "error";
		}
	}

	/**
	 * Returns the length of the combination.
	 * @return the length of the combination
	 */
	public int getLength() {
		return cards.length;
	}

	/**
	 * Compares two combinations.
	 */
	public int compareTo(Combination c) {
		return getValue() - c.getValue();
	}

	/**
	 * Returns the cards in the combination.
	 * @return the cards in the combination
	 */
	public Card[] getCards() {
		return cards;
	}

	/**
	 * Returns the sum of the values of each card.
	 * @return the sum of the cards' values
	 */
	public int getSumCardValues() {
		int total = 0;
		for (Card c : cards) {
			total += c.getOrdinalNumber();
		}
		return total;
	}

	/**
	 * String representation of the combianation.
	 */
	public String toString() {
		String output = getType() + ": ";
		for (int i = 0; i < cards.length - 1; i++)
			output = output.concat(cards[i].toString() + ", ");

		// temporary, for error checking
		if (cards.length > 0)
			output = output.concat(cards[cards.length - 1].toString());
		return output;
	}

	/**
	 * Returns the value of the combination.
	 * @return the value of the combination
	 */
	public abstract int getValue();

	/**
	 * Represents a pass.
	 * @author Gabriel
	 *
	 */
	public static class Pass extends Combination {
		protected Pass() {
			super(new Card[0]);
		}

		public int getValue() {
			return -1;
		}
	}

	/**
	 * Represents a single card.
	 * @author Gabriel
	 *
	 */
	public static class Single extends Combination {
		public static final int NUM_VALUES = 52; // number of possible singles

		protected Single(Card[] cards) throws InvalidCombinationException {
			super(cards);
			if (cards.length != 1)
				throw new InvalidCombinationException(
						"cards.length must be 1. cards.length is "
								+ cards.length);
		}

		/**
		 * Returns if the given cards form a valid single.
		 * @param cards the cards to check
		 * @return if it is a valid single
		 */
		public static boolean isValid(Card[] cards) {
			if (cards.length != 1)
				return false;
			return true;
		}

		public int getValue() {
			return cards[0].getOrdinalNumber();
		}
	}

	/**
	 * Represents a double.
	 * @author Gabriel
	 *
	 */
	public static class Double extends Combination {
		public static final int NUM_VALUES = 39; // number of possible values of
													// doubles

		protected Double(Card[] cards) throws InvalidCombinationException {
			super(cards);

			// Check for length
			if (cards.length != 2)
				throw new InvalidCombinationException(
						"cards.length must be 2. cards.length is "
								+ cards.length);

			// Check for same rank
			if (cards[0].rank != cards[1].rank)
				throw new InvalidCombinationException(
						"Cards in a double must have the same rank.");
		}

		/**
		 * Returns if the cards form a valid double.
		 * @param cards the cards to check
		 * @return if the cards form a valid double
		 */
		public static boolean isValid(Card[] cards) {
			if (cards.length != 2)
				return false;
			if (cards[0].rank != cards[1].rank)
				return false;
			return true;
		}

		public int getValue() {
			// Ordinal number of the largest suit. 1 is subtracted because in a
			// double, there will never be a diamond as the largest
			int maxSuit = Math.max(cards[0].suit.ordinal(),
					cards[1].suit.ordinal()) - 1;

			int rank = cards[0].rank.ordinal();
			return (rank * 3 + maxSuit) + Single.NUM_VALUES; // Values of
																// singles added
																// so doubles
																// will be
																// larger than
																// singles
		}
	}

	/**
	 * Represents a triple.
	 * @author Gabriel
	 *
	 */
	public static class Triple extends Combination {
		public static final int NUM_VALUES = 13;

		protected Triple(Card[] cards) throws InvalidCombinationException {
			super(cards);

			// Check for length
			if (cards.length != 3)
				throw new InvalidCombinationException(
						"cards.length must be 3. cards.length is "
								+ cards.length);

			// Check for same rank
			if (cards[0].rank != cards[1].rank
					|| cards[0].rank != cards[2].rank)
				throw new InvalidCombinationException(
						"Cards in a triple must have the same rank.");
		}

		/**
		 * Returns if the cards form a valid triple.
		 * @param cards the cards to check
		 * @return if the cards form a valid triple
		 */
		public static boolean isValid(Card[] cards) {
			if (cards.length != 3)
				return false;
			if (cards[0].rank != cards[1].rank
					|| cards[0].rank != cards[2].rank)
				return false;
			return true;
		}

		public int getValue() {
			return cards[0].rank.ordinal() + Single.NUM_VALUES
					+ Double.NUM_VALUES; // Values of singles and doubles added
											// to make triples larger
		}
	}

	/**
	 * Represents a poker hand.
	 * @author Gabriel
	 *
	 */
	public static class PokerHand extends Combination {

		public final int type;

		/**
		 * Type property is straight.
		 */
		public static final int STRAIGHT = 0;
		/**
		 * Type property is flush.
		 */
		public static final int FLUSH = 1;
		/**
		 * Type property is full house.
		 */
		public static final int FULL_HOUSE = 2;
		/**
		 * Type property is four of a kind.
		 */
		public static final int FOUR_OF_A_KIND = 3;
		/**
		 * Type property is straight flush.
		 */
		public static final int STRAIGHT_FLUSH = 4;

		public static final int NUM_STRAIGHT = 36, NUM_FLUSH = 32,
				NUM_FULL_HOUSE = 13, NUM_FOUR_OF_A_KIND = 13,
				NUM_STRAIGHT_FLUSH = 36;

		protected PokerHand(Card[] cards) throws InvalidCombinationException {
			super(cards);

			// Check for length
			if (cards.length != 5)
				throw new InvalidCombinationException(
						"cards.length must be 5. cards.length is "
								+ cards.length);

			// Check for all types of poker hands
			if (isStraight(cards))
				type = STRAIGHT;
			else if (isFlush(cards))
				type = FLUSH;
			else if (isFullHouse(cards))
				type = FULL_HOUSE;
			else if (isFourOfAKind(cards))
				type = FOUR_OF_A_KIND;
			else if (isStraightFlush(cards))
				type = STRAIGHT_FLUSH;
			else
				throw new InvalidCombinationException(
						"The cards do not form a poker hand.");
		}

		/**
		 * Returns if the cards form a valid straight. False if it is a straight flush.
		 * @param cards the cards to check
		 * @return if the cards form a valid straight
		 */
		public static boolean isStraight(Card[] cards) {
			return isStraight(cards, true);
		}

		/**
		 * Returns if the cards form a valid straight.
		 * @param cards the cards to check
		 * @param checkStraightFlush whether a straight flush will return false
		 * @return if the cards form a valid straight
		 */
		public static boolean isStraight(Card[] cards,
				boolean checkStraightFlush) {
			// check for straight
			for (int i = 1; i < cards.length; i++) {
				// If this card's rank is one higher than the last card's rank
				if (cards[i].rank.ordinal() != cards[i - 1].rank.ordinal() + 1)
					return false;
			}

			if (checkStraightFlush && isFlush(cards, false))
				return false;

			return true;
		}

		/**
		 * Returns if the cards form a valid flush. A straight flush will return false.
		 * @param cards the cards to check
		 * @return if the cards form a valid flush
		 */
		public static boolean isFlush(Card[] cards) {
			return isFlush(cards, true);
		}

		/**
		 * Returns if the cards form a valid flush.
		 * @param cards the cards to check
		 * @param checkStraightFlush whether a straight flush will return false
		 * @return if the cards form a valid flush
		 */
		public static boolean isFlush(Card[] cards, boolean checkStraightFlush) {
			for (int i = 1; i < cards.length; i++) {
				// If this card's suit is the same as the last card's suit
				if (cards[i].suit != cards[i - 1].suit)
					return false;
			}

			if (checkStraightFlush && isStraight(cards, false))
				return false;

			return true;
		}

		/**
		 * Returns if the cards form a valid straight flush.
		 * @param cards the cards to check
		 * @return if the cards form a valid straight flush
		 */
		public static boolean isStraightFlush(Card[] cards) {
			// is both straight and flush
			return isStraight(cards, false) && isFlush(cards, false);
		}

		/**
		 * Returns if the cards form a valid four of a kind.
		 * @param cards the cards to check
		 * @return if the cards form a valid four of a kind
		 */
		public static boolean isFourOfAKind(Card[] cards) {
			// Check first four cards
			if (cards[0].rank == cards[1].rank
					&& cards[0].rank == cards[2].rank
					&& cards[0].rank == cards[3].rank)
				return true;

			// Check last four cards
			if (cards[1].rank == cards[2].rank
					&& cards[1].rank == cards[3].rank
					&& cards[1].rank == cards[4].rank)
				return true;

			return false; // not four of a kind
		}

		/**
		 * Returns if the cards form a valid full house.
		 * @param cards the cards to check
		 * @return if the cards form a valid full house
		 */
		public static boolean isFullHouse(Card[] cards) {
			// Check for triple first
			if (cards[0].rank == cards[1].rank
					&& cards[0].rank == cards[2].rank) {
				// Check for double in last 2 cards
				if (cards[3].rank == cards[4].rank)
					return true;
				return false;
			}

			// Check for triple last
			if (cards[2].rank == cards[3].rank
					&& cards[2].rank == cards[4].rank) {
				// Check for double in last 2 cards
				if (cards[0].rank == cards[1].rank)
					return true;
				return false;
			}

			return false; // no triple
		}

		public static boolean isValid(Card[] cards) {
			return isStraight(cards) || isFlush(cards) || isFourOfAKind(cards)
					|| isFullHouse(cards);
		}

		/**
		 * Returns the type of poker hand as a string.
		 */
		@Override
		public String getType() {
			switch (type) {
			case STRAIGHT:
				return "Straight";
			case FLUSH:
				return "Flush";
			case FULL_HOUSE:
				return "Full House";
			case FOUR_OF_A_KIND:
				return "Four of a Kind";
			case STRAIGHT_FLUSH:
				return "Straight Flush";
			default:
				return "error";
			}
		}

		public int getValue() {
			int previousValues = Single.NUM_VALUES + Double.NUM_VALUES
					+ Triple.NUM_VALUES; // number of smaller values (104)

			switch (type) {
			case STRAIGHT:
				// return largest card subtracted by number of cards that cannot
				// be highest in a straight
				// add previous values to have straights larger than other
				// combinations
				return (cards[4].getOrdinalNumber() - (52 - NUM_STRAIGHT))
						+ previousValues;

			case FLUSH:
				// return largest card subtracted by number of cards that cannot
				// be highest in a flush
				return (cards[4].getOrdinalNumber() - (52 - NUM_FLUSH)
						+ previousValues + NUM_STRAIGHT);

			case FULL_HOUSE:
				// return rank of triple plus previous values
				int rankTriple;
				if (cards[0].rank == cards[1].rank
						&& cards[0].rank == cards[2].rank) // triple first
					rankTriple = cards[0].rank.ordinal();
				else
					rankTriple = cards[4].rank.ordinal(); // triple last
				return (rankTriple + previousValues + NUM_STRAIGHT + NUM_FLUSH);

			case FOUR_OF_A_KIND:
				// return rank of four of a kind plus previous values
				int rankQuad;
				if (cards[0].rank == cards[1].rank
						&& cards[0].rank == cards[2].rank
						&& cards[0].rank == cards[3].rank)
					rankQuad = cards[0].rank.ordinal();
				else
					rankQuad = cards[4].rank.ordinal();
				return (rankQuad + previousValues + NUM_STRAIGHT + NUM_FLUSH + NUM_FULL_HOUSE);

			case STRAIGHT_FLUSH:
				// return largest card subtracted by number of cards that cannot
				// be highest in a full house
				// add previous values to have straight flush larger than
				// everything else
				return (cards[4].getOrdinalNumber() - (52 - NUM_STRAIGHT_FLUSH) + previousValues
						+ NUM_STRAIGHT + NUM_FLUSH + NUM_FULL_HOUSE + NUM_FOUR_OF_A_KIND);
			}

			return -1; // for compilation and error checking
		}
	}
}
