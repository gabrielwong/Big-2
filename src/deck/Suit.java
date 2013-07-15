package deck;

/**
 * Represents the suit of a card.
 * @author Gabriel
 *
 */
public enum Suit{
	DIAMONDS('d'),
	CLUBS('c'),
	HEARTS('h'),
	SPADES('s');
	private final char identifier;
	private Suit(char identifier){
		this.identifier = identifier;
	}
	
	/**
	 * Returns a suit based on the value of the suit.
	 * @param suit the index of the suit
	 * @return the suit
	 * @throws IllegalArgumentException suit must be in the range[0, 3]
	 */
	public static Suit getSuit(int suit) throws IllegalArgumentException{
		try{
			return Suit.values()[suit];
		}
		catch(ArrayIndexOutOfBoundsException e){
			throw new IllegalArgumentException("Suit not in the range [0,3].");
		}
	}
	
	/**
	 * Returns the one character identifier of the suit.
	 * @return the identifier
	 */
	public char identifier(){
		return identifier;
	}
	
	/**
	 * String representation of the suit.
	 */
	public String toString(){
		char[] chars;
		
		chars = name().toLowerCase().toCharArray(); // char array of the name of the enum in lower case
		chars[0] = Character.toUpperCase(chars[0]); // make first letter upper case
		
		return new String(chars);
	}
}