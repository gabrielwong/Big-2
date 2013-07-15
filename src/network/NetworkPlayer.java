package network;

import game.Combination;
import game.GameState;
import game.GameStateChangeListener;
import game.InvalidCombinationException;
import game.LocalPlayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.util.Util;

import deck.Card;

/**
 * An extension of LocalPlayer. It is important to understand that this is a
 * <b>reference</b> to the network player. It only contains an address which identifies
 * the NetworkPlayer
 * @author Soheil Koushan
 */
public class NetworkPlayer extends LocalPlayer implements Serializable,
		GameStateChangeListener {

	public Address address; // synced data field

	/* local data fields */
	public GameConnection connection;
	public Combination previousPlay = null;
	public Combination selectedCombination;
	public Thread gameLoopThread;
	public Card forcedCard;

	/**
	 * After this, setConnection(GameConnection) must be called for this network player to
	 * function
	 */
	public NetworkPlayer(Address a, int i) {
		address = a;
		index = i;
		name = address.toString();
	}

	/** Must be called by GameConnection to associate a network connection to this player. */
	public void setConnection(GameConnection c) {
		connection = c;
	}

	/** implementation of serializable. Only the address is synced */
	private void writeObject(ObjectOutputStream out) throws Exception {
		Util.writeAddress(address, new DataOutputStream(out));
		out.writeInt(index);
	}

	/** implementation of serializable. Only the address is synced */
	private void readObject(ObjectInputStream in) throws Exception {
		address = Util.readAddress(new DataInputStream(in));
		index = in.readInt();
	}

	@Override
	/** Send a message to the network player associated with this and sleeps the thread until there is a response */
	public Combination doTurn(GameState state) {
		previousPlay = state.getPreviousPlay();
		forcedCard = state.getForcedCard();
		System.out.format(
				"doTurn called on [%s] with previousPlay:%s, forcedCard:%s%n",
				address, previousPlay, forcedCard);

		/* send a message to the associated network player */
		Message msg = new Message(address, null, previousPlay);
		msg.putHeader(Command.HEADER_ID, new Command('t'));
		try {
			connection.channel.send(msg);
			System.out.format("[%s]: doTurn message sent to %s ", address,
					address);
		} catch (Exception e) {
			System.err.println("Unable to send the doTurn message");
			e.printStackTrace();
		}

		/*
		 * wait for a response (until host receives the cards, interrupts the thread, and
		 * sets the selected cards
		 */
		gameLoopThread = Thread.currentThread();
		try {
			System.out.println("Now waiting...");
			Thread.sleep(Long.MAX_VALUE); // wait until interrupted
		} catch (InterruptedException e) {
			System.out.println("Done waiting for" + address
					+ ". Thread interrupted by:" + e.getMessage());
		} // continue

		System.out.println("Returning the selected combination: "
				+ selectedCombination);
		Combination c = selectedCombination; // temp storage
		selectedCombination = null; // reset selected combination
		previousPlay = null;
		return c;
	}

	/** sets the selected combination and interrupts the sleeping thread */
	public void setSelectedCombination(Combination c) {
		System.out.format("[%s]: received %s and now setting.%n", address, c);
		selectedCombination = c;
		gameLoopThread.interrupt();
	}

	public void setPreviousPlay(Combination c) {
		previousPlay = c;
	}

	@Override
	public void receiveInput(boolean[] selected)
			throws InvalidCombinationException {
		System.out
				.println("Received input from GamePanel locally: " + selected);

		/* error checking for invalid selection */
		if (previousPlay == null)
			return;

		System.out.println("My hand is: " + hand);

		// For a pass
		if (selected == null) {
			// If passing on the first play in a trick
			if (previousPlay == Combination.getPassCombination())
				throw new InvalidCombinationException(
						"You cannot pass on a new trick.");
			selectedCombination = Combination.getPassCombination();
		} else {
			int nSelected = 0; // number of cards that are selected

			// Count number of cards that are selected
			for (int i = 0; i < size(); i++)
				if (selected[i])
					nSelected++;

			if (nSelected == 0)
				throw new InvalidCombinationException("No cards were selected."); // no
																					// cards
																					// selected,
																					// nothing
																					// happens
			if (nSelected != previousPlay.getLength()
					&& previousPlay.getLength() != Combination.PASS)
				throw new InvalidCombinationException(
						"You must play the same number of cards as the cards on the table."); // not
																								// the
																								// same
																								// type

			Card[] cards = new Card[nSelected]; // array of selected cards

			int j = 0; // index of cards
			for (int i = 0; i < size() && j < nSelected; i++) { // fill cards
				if (selected[i]) {
					cards[j] = get(i);
					j++;
				}
			}

			if (forcedCard != null) {
				boolean hasCard = false;
				for (int i = 0; i < cards.length; i++) {
					if (forcedCard.equals(cards[i])) {
						hasCard = true;
						break;
					}
				}
				if (!hasCard)
					throw new InvalidCombinationException("You must have a "
							+ forcedCard.toString() + " in your combination.");
			}

			Combination selectedCombination = Combination.getCombination(cards); // get
																					// combination
																					// and
																					// set
																					// it
																					// so
																					// do
																					// turn
																					// can
																					// continue
			if (selectedCombination.getValue() < previousPlay.getValue())
				throw new InvalidCombinationException(
						"The cards selected are of less value than the previously played cards.");

			this.selectedCombination = selectedCombination;
		}
		
		System.out.format("[%s]: I've locally selected %s%n", address,
				selectedCombination);
		Message msg = new Message(connection.channel.getView().getCreator(),
				address, selectedCombination);
		msg.putHeader(Command.HEADER_ID, new Command('c'));
		try {
			connection.channel.send(msg);
		} catch (Exception e) {
			System.err.println("Unable to send the selected cards!");
			e.printStackTrace();
		}
	}

	public String toString() {
		return "[" + address + "]";
	}

	@Override
	public void gameStateChanged(GameState state) {
		previousPlay = state.getPreviousPlay();
		hand = state.getPlayers()[index].getHand();
	}
}
