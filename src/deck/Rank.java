package deck;

import java.io.Serializable;

/**
 * Represents the rank of the card. This is the rank in Big 2, so a 3 is the lowest,
 * while a 2 is the highest.
 * @author Gabriel
 *
 */
public enum Rank implements Serializable{
	THREE('3'),
	FOUR('4'),
	FIVE('5'),
	SIX('6'),
	SEVEN('7'),
	EIGHT('8'),
	NINE('9'),
	TEN('t'),
	JACK('j'),
	QUEEN('q'),
	KING('k'),
	ACE('a'),
	TWO('2');
	private final char identifier;
	private Rank(char identifier){
		this.identifier = identifier;
	}
	
	/**
	 * Returns the rank of the card based on the value of the rank.
	 * @param rank the index of the rank
	 * @return the Rank
	 * @throws IllegalArgumentException the rank must be in the range[0, 12]
	 */
	public static Rank getRank(int rank) throws IllegalArgumentException{
		try{
			return Rank.values()[rank];
		}
		catch(ArrayIndexOutOfBoundsException e){
			throw new IllegalArgumentException("Rank not in the range [0,12]");
		}
	}
	
	/**
	 * Returns the one character representation of the rank.
	 * @return the identifier
	 */
	public char identifier(){
		return identifier;
	}
	
	/**
	 * String representation of the rank.
	 */
	public String toString(){
		if (ordinal() < 7 || ordinal() == 12) // two - nine
			return "" + identifier;
		if (ordinal() == 7)
			return "10";
		if (ordinal() == 8)
			return "Jack";
		if (ordinal() == 9)
			return "Queen";
		if (ordinal() == 10)
			return "King";
		if (ordinal() == 11)
			return "Ace";
		return "error";
	}
}