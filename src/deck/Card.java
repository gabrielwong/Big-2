package deck;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * Representation of a card in a standard poker deck. Each card has a corresponding suit, rank and image.
 * @author Gabriel
 *
 */
public class Card implements Comparable, Serializable {
	public final Rank rank;
	public final Suit suit;
	protected boolean faceUp = true;

	private static BufferedImage[][] cardImages; // array of card images
													// [Rank][Suit]
	private static BufferedImage verticalCardBack;
	private static BufferedImage horizontalCardBack;

	public static final int WIDTH = 72, // width of the cards
			HEIGHT = 96; // height of the cards

	static {
		loadImages();
	}
	
	/**
	 * Construct a new card object.
	 * 
	 * @param rank
	 *            the rank of the new card
	 * @param suit
	 *            the suit of the new card
	 */
	public Card(Rank rank, Suit suit) {
		this.rank = rank;
		this.suit = suit;
	}

	/**
	 * Construct a new card object out of the 52 standard cards in a deck. The
	 * cards are ordered by increasing rank from 2 to Ace, then by suit. (new
	 * Card(number)).getOrdinalNumber() == number
	 * 
	 * @param number
	 *            the number of the card from 0 - 51.
	 * @throws IllegalArgumentException
	 *             If number is not in the interval [0,51]
	 */
	public Card(int number) throws IllegalArgumentException {
		this(Rank.getRank(number / 4), Suit.getSuit(number % 4));
	}

	/**
	 * Returns the image of the card
	 * 
	 * @return the image of the card
	 */
	public BufferedImage getImage() {
		return getImage(faceUp);
	}

	/**
	 * Returns the image of the card when either face up or face down.
	 * 
	 * @param faceUp
	 *            return face up image or face down image
	 * @return the image of the card
	 */
	public BufferedImage getImage(boolean faceUp) {
		if (cardImages == null)
			loadImages();
		return faceUp ? cardImages[rank.ordinal()][suit.ordinal()]
				: verticalCardBack;
	}

	/**
	 * Returns the back of a card rotated in a portrait format.
	 * @return the image of the back of the card
	 */
	public static BufferedImage getVerticalBack() {
		if (verticalCardBack == null)
			loadImages();
		return verticalCardBack;
	}

	/**
	 * Returns the back of the card rotated in a landscape format.
	 * @return the image of the back of the card
	 */
	public static BufferedImage getHorizontalBack() {
		if (horizontalCardBack == null)
			loadImages();
		return horizontalCardBack;
	}

	/**
	 * Draws the image of the card on g with the top left corner at (x,y).
	 * 
	 * @param g the graphics context to paint the card on
	 * @param x the x value of the top left corner of the location where the card will be painted
	 * @param y the y value of the top left corner of the location where the card will be painted
	 */
	public void show(Graphics g, int x, int y) {
		if (getImage() == null)
			return;
		g.drawImage(getImage(), x, y, null);
	}

	/**
	 * Flips the card over. (!faceUp)
	 */
	public void flip() {
		faceUp = !faceUp;
	}

	/**
	 * @return if the card is face up
	 */
	public boolean isFaceUp() {
		return faceUp;
	}

	/**
	 * Sets the face up status of the card.
	 * 
	 * @param faceUp
	 */
	public void setFaceUp(boolean faceUp) {
		this.faceUp = faceUp;
	}

	/**
	 * Returns whether the two cards have equal rank and suit
	 * 
	 * @param c
	 *            the card to compare with
	 * @return if the cards have equal rank and suit
	 */
	public boolean isSameCard(Card c) {
		return (rank.equals(c.rank) && suit.equals(c.suit));
	}

	/**
	 * Returns the ordinal number of the card. Cards are ordered by rank, then
	 * suit. (new Card(number)).getOrdinalNumber() == number
	 * 
	 * @return the ordinal number of the card.
	 */
	public int getOrdinalNumber() {
		return rank.ordinal() * 4 + suit.ordinal();
	}

	/**
	 * Compares the ordinal number of two cards.
	 */
	public int compareTo(Object obj) {
		Card c = (Card) obj;
		return (getOrdinalNumber() - c.getOrdinalNumber());
	}

	/**
	 * Ignores whether the card is face up.
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof Card))
			return false;
		Card c = (Card) obj;
		return (rank.equals(c.rank) && suit.equals(c.suit));
	}

	/**
	 * String representation of the card.
	 */
	public String toString() {
		return rank.toString() + " of " + suit.toString();
	}

	/**
	 * Load images from the file system and store them in images[][].
	 */
	public static void loadImages() {

		cardImages = new BufferedImage[Rank.values().length][Suit.values().length];
		// create blank array for images

		for (Rank rank : Rank.values()) {
			for (Suit suit : Suit.values()) {
				String imageName = "/cards/" + rank.identifier()
						+ (suit.identifier() + ".png");
				URL path = imageName.getClass().getResource(imageName);
				try {
					// read image into array
					cardImages[rank.ordinal()][suit.ordinal()] = ImageIO
							.read(path);
				} catch (Exception e) {
					// blank images
					cardImages[rank.ordinal()][suit.ordinal()] = new BufferedImage(
							WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
				}
			}

			URL cardBackPath = "".getClass().getResource("/cards/back.png"); // get
																				// image
																				// for
																				// back
																				// of
																				// card

			try {
				verticalCardBack = ImageIO.read(cardBackPath);
			} catch (Exception e) {
				// black rectangle for back of card
				verticalCardBack = new BufferedImage(WIDTH, HEIGHT,
						BufferedImage.TYPE_INT_ARGB);
				Graphics g = verticalCardBack.getGraphics();
				g.setColor(java.awt.Color.BLACK);
				g.fillRect(0, 0, WIDTH, HEIGHT);
			}

			horizontalCardBack = new BufferedImage(
					verticalCardBack.getHeight(), verticalCardBack.getWidth(),
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = horizontalCardBack.createGraphics();
			g.rotate(Math.PI / 2);
			g.drawImage(verticalCardBack, 0, -HEIGHT, null);
		}
	}
}
