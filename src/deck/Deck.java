package deck;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Deck implements Serializable{
	protected ArrayList<Card> list;
	public static final int CARD_LEFTSIDE = 12; // # of px to show on left side of card when overlapped
	public static final int CARD_TOP = 19; // # of pixels to show at top

	public Deck(ArrayList<Card> list){
		this.list = list;
	}

	/**
	 * Creates a new Deck object by creating a shallow clone of deck (the Card objects are not duplicated).
	 * @param deck the deck object to be copied
	 */
	@SuppressWarnings("unchecked")
	public Deck(Deck deck){
		this.list = (ArrayList<Card>)deck.list.clone();
	}

	/**
	 * Creates a new deck.
	 * @param filled whether the deck should be filled upon creation with the standard 52 cards.
	 */
	public Deck(boolean filled){
		if (filled){
			list = new ArrayList<Card>(52);
			addFullDeck();
		}
		else{
			list = new ArrayList<Card>();
		}
	}
	/**
	 * Creates an empty deck.
	 * Equivalent to Deck(false)
	 */
	public Deck(){
		this(false);
	}

	/**
	 * Adds all 52 standard cards to the deck ordered by increasing rank then increasing suit.
	 */
	public void addFullDeck(){
		for (int i = 0; i < 52; i++)
			list.add(new Card(i));
	}

	/**
	 * Adds all the cards in the specified deck to this.
	 * @param array the array of cards to be added
	 */
	public void addAll(ArrayList<Card> array){
		list.addAll(array);
	}
	
	public Card get(int index) throws IndexOutOfBoundsException{
		return list.get(index);
	}
	
	public boolean remove(Card c){
		return list.remove(c);
	}
	
	public int size(){
		return list.size();
	}
	
	/**
	 * Shuffles the deck once.
	 */
	public void shuffle(){
		shuffle(1);
	}

	/**
	 * Shuffles the deck by picking out cards in a random order.
	 * It reorders the Cards in the deck in a random order.
	 * @param iterations the number of times the deck should be shuffled
	 */
	public void shuffle(int iterations){
		if (list.size() == 0)
			return;
		for (int i = 0; i < iterations; i++){
			ArrayList<Card> newList = new ArrayList<Card>(list.size());
			for (int j = list.size()-1; j >= 0; j--){
				int pos = (int)(Math.random()*(j+1)); // random number between 0 and j, inclusive
				newList.add(list.get(pos)); // add the random card to new deck
				list.remove(pos); // remove the random card from old deck
			}
			list = newList; // change the deck
		}
	}

	/**
	 * Draws the cards, overlapped, on a BufferedImage. The maximum number of cards will be drawn on each row.
	 * The cards will be positioned into multiple rows only if necessary. 
	 * @param width the width of the BufferedImage to be returned
	 * @param height the height of the BufferedImage to be returned
	 * @return a BufferedImage with all the cards drawn on it
	 */
	public BufferedImage show(int width, int height){
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		if (list.size() == 0 )
			return image;

		// number of cards displayed per row
		int cardsPerRow = ((width - Card.WIDTH) // total pixels excluding the one full card
				/ CARD_LEFTSIDE) + 1; 				 // divide by CARD_LEFTSIDE to find how many overlapped can fit
		// +1 to include the one full card

		//number of rows of cards
		// +1 due to integer division
		int rows = list.size() / cardsPerRow + 1; // number of rows of cards
		
		// Save processing time when there are more rows than can be shown
		rows = Math.min(rows, height/Card.HEIGHT + 1);

		// draw the cards
		int i = 0; // counter for cards drawn
		for (int y = 0; y < rows && i < list.size(); y++){
			for (int x = 0; x < cardsPerRow-1 && i < list.size()-1; x++){
				// Draw the overlapped cards
				g.drawImage(list.get(i).getImage(), x * CARD_LEFTSIDE, y * Card.HEIGHT,
						(x+1) * CARD_LEFTSIDE+1, (y+1) * Card.HEIGHT+1, 0, 0, CARD_LEFTSIDE+1, Card.HEIGHT+1, null);
				//list.get(i).show(g, x * CARD_LEFTSIDE, y * Card.CARD_HEIGHT);
				i++; // increment counter for cards, so that the next iteration draws the next card
			}
			// Draw the last card in the row
			g.drawImage(list.get(i).getImage(),(i%cardsPerRow)*CARD_LEFTSIDE,y*Card.HEIGHT,null);
			i++;
		}

		return image;
	}

	/**
	 * Returns the card at the index specified by the int argument position. It also removes that card from the deck.
	 * @param	index	the index of the card to be dealt
	 * @return			the Card at the specified index
	 */
	public Card deal(int index){
		if (index > list.size()-1 || index < 0)
			return null;
		return list.remove(index); // get the card at position and remove it
	}

	/**
	 * Convenience method to deal the card at the top of the deck. It is the same as deal(0).
	 * @return	the Card at the index 0
	 */
	public Card deal(){
		return deal(0); // deal first card
	}

	/**
	 * Adds a card to the end of the deck.
	 * @param card	the Card which is to be added to the end of the deck
	 */
	public void add(Card card){
		list.add(card);
	}

	/**
	 * Searches for all occurrence of the Card specified by the argument item and returns the indexes.
	 * Item and the Card in the array does not have to be the same object (item == list.get(i) not always true).
	 * @param item the card to look for
	 * @param considerFaceUp whether the face up status of the card is also compared
	 * @param stopAfter specifies the maximum number of objects to find (if it is <= 0, find all)
	 * @return an array of all indexes, up to the amount specified by stopAfter, of the Cards that are the same as the Card item
	 */
	public int[] searchAll(Card item, boolean considerFaceUp, int stopAfter){
		ArrayList<Card> array = new ArrayList<Card>(list);
		int index; // index currently being processed
		int numRemoved = 0; // cards removed
		int numFound = 0; // cards found
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		do{
			index = array.indexOf(item); // get index of first Card equal to item
			if (index < 0)
				return arrayListToIntArray(indexes); // if there is none, then return the indexes already found
			
			// Ignore cards that do not have matching faceUp when considerFaceUp == true
			if (!(considerFaceUp && array.get(index).faceUp != item.faceUp)){
				indexes.add(index+numRemoved); // add the index of the item in the original array
				numFound++;
			}
			
			for (int i = 0; i <= index; i++){
				array.remove(0); // remove the items before the found item, for next indexOf
			}
			numRemoved += index + 1; // increase numRemoved by the number of items removed
			
		} while (array.size() > 0 && (numFound < stopAfter || stopAfter <= 0));
		// while there are still items left in the array and the maximum number of indexes was not reached

		return arrayListToIntArray(indexes); // reached when loop terminated by stopAfter
	}
	
	/**
	 * Searches for all occurrence of the Card specified by the argument item and returns the indexes.
	 * Item and the Card in the array does not have to be the same object (item == list.get(i) not always true).
	 * @param item the card to look for
	 * @param considerFaceUp whether the face up status of the card is also compared
	 * @return an array of all indexes of the Cards that are the same as the Card item
	 */
	public int[] searchAll(Card item, boolean considerFaceUp){
		return searchAll(item, considerFaceUp, 0);
	}

	/**
	 * Searches for all occurrence of the Card specified by the argument item and returns the indexes.
	 * Does not consider whether the Card is face up or face down.
	 * Item and the Card in the array does not have to be the same object (item == list.get(i) not always true).
	 * @param item the card to look for
	 * @return an array of all indexes of the Cards that are the same as the Card item
	 */
	public int[] searchAll(Card item){
		return searchAll(item, false, 0);
	}
	
	/**
	 * Searches for the first occurrence of the Card specified by the argument item and returns the index.
	 * Disregards whether the card is face up or face down.
	 * Equivalent to search(item, false)
	 * @param item the card to look for
	 * @param considerFaceUp whether the face up status of the card is compared
	 * @return the index of the first card in the deck that is the same as item. If none found, returns -1
	 */
	public int search(Card item, boolean considerFaceUp){
		try{
		return searchAll(item, considerFaceUp, 1)[0];
		} catch (ArrayIndexOutOfBoundsException e){
			return -1;
		}
	}
	
	/**
	 * Searches for the first occurrence of the Card specified by the argument item and returns the index.
	 * Disregards whether the card is face up or face down.
	 * Equivalent to search(item, false)
	 * @param item the card to look for
	 * @return the index of the first card in the deck that is the same as item, disregarding faceUp. If none found, returns -1
	 */
	public int search(Card item){
		return search(item,false);
	}
	
	/**
	 * Removes all cards from the deck.
	 */
	public void clear(){
		list.clear();
	}

	/**
	 * Converts an ArrayList<Integer> to int[]
	 * @param list the ArrayList to convert
	 * @return the int array
	 */
	private int[] arrayListToIntArray(ArrayList<Integer> list){
		list.trimToSize();
		Integer[] integerArray = new Integer[list.size()];
		list.toArray(integerArray); // convert arrayList to Integer array
		int[] returnArray = new int[integerArray.length];
		for (int i = 0; i < integerArray.length; i++)
			returnArray[i] = integerArray[i]; // convert Integer array to int array
		return returnArray;
	}

	/**
	 * Sorts the deck using a quicksort recursive algorithm by rank (A, 2...Q, K), ignoring suit.
	 */
	public void quicksort(){
		// call recursive quicksort method
		quicksort(list, 0, list.size()-1, true); 
	}

	private void quicksort(ArrayList<Card> list, int left, int right, boolean firstCall){
		int i = left; // Index of card that is greater than pivot
		int j = right; // Index of card that is less than pivot

		if (right - left < 1)
			return; // if there is less than 1 element, the array is sorted
		int pivot = quicksortNum(list.get(left));

		// While there is an element 
		while (j > i){
			// Loops until there is a card left of the j that greater than the pivot
			while (quicksortNum(list.get(i)) <= pivot && i <= right && j > i)
				i++;
			// Loops until there is a card right of i that is less than the pivot
			while (quicksortNum(list.get(j)) > pivot && j >= left && j >= i)
				j--;
			// Swaps items at i and j if they are in the wrong spot
			if (j > i)
				swap(list, i, j);
		}
		swap(list, left, j); // swap the pivot in the proper position

		// Recursive calls for partitions left and right of the pivot
		quicksort(list, left, j-1, false);
		quicksort(list, j+1, right, false);

		// Updates the ArrayList with the sorted array
		if (firstCall)
			this.list = list;
	}

	/**
	 * Sorts the deck using a combsort algorithm by suit (S, H, D, C), then by rank (A, 2...Q, K).
	 */
	public void combsort(){;
		int gap = list.size(); // gap from end of array to consider
		boolean swapped; // whether a swap occured (means the list is not guaranteed sorted)
		do{
			swapped = false;
			gap = newGap(gap); // shrink the gap
			for (int i = 0; i < (list.size() - gap); i++){ // for all elements before the gap
				if (combsortNum(list.get(i)) > combsortNum(list.get(i+gap))){
					swapped = true;
					swap(list,i,i+gap); // swap if necessary
				}
			}
		} while (gap > 1 || swapped);
	}

	/**
	 * Calculates the new gap.
	 * @param gap the old gap
	 * @return the new gap
	 */
	private static int newGap(int gap){
		gap = gap * 10 / 13; //
		if (gap == 9 || gap == 10)
			return 11;
		if (gap < 1)
			return 1;
		return gap;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Sorts the deck based on rank, then suit.
	 */
	public void sort(){
		Collections.sort(list);
	}
	
	private static int quicksortNum(Card c){
		int num = c.rank.ordinal(); // get ordinal number of card's rank
		num++; // shift index of ranks up by 1 to make space for ace
		if (num>=13)
			num = 0; // if it is an ace, make it lowest rank
		return num;
	}

	/**
	 * Returns the integer used to order cards in the combsort method.
	 * @param c the card to consider
	 * @return the value of the card
	 */
	private static int combsortNum(Card c){
		int rankOrder = quicksortNum(c); // order by rank
		int suitOrder = 3 - c.suit.ordinal(); // order by suit
		return (suitOrder * 13) + rankOrder; // sorted by suit then rank
	}

	/**
	 * Swaps two cards in a list.
	 * @param list the list containing the two cards
	 * @param index1 the index of the first card
	 * @param index2 the index of the second card
	 */
	private static void swap(ArrayList<Card> list, int index1, int index2){
		Card temp = list.get(index1);
		list.set(index1, list.get(index2));
		list.set(index2, temp);
	}

	/**
	 * Returns the deck as an array of cards.
	 * @return the deck as an array of cards
	 */
	public Card[] toArray(){
		Card[] array = new Card[list.size()];
		list.toArray(array);
		return array;
	}
	
	/**
	 * Returns the deck as an ArrayList<Card>.
	 * @return the deck as an ArrayList<Card>
	 */
	public ArrayList<Card> toArrayList(){
		ArrayList<Card> newArray = new ArrayList<Card>(list.size());
		for (int i = 0; i < list.size(); i++)
			newArray.add(list.get(i));
		return newArray;
	}
	
	/**
	 * String representation of all the cards in the deck.
	 */
	public String toString() {
		String s = "{";
		for (int i = 0; i < list.size(); i++)
			s += (list.get(i).toString() + ", ");
		s += "}";
		return s;
	}
}
