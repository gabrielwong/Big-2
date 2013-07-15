package network;

import game.AbstractPlayer;
import game.CPUPlayer;
import game.Combination;
import game.Game;
import game.GameState;
import game.GameStateChangeListener;
import gui.GamePanel;
import gui.LobbyPanel;
import gui.NetworkSplitPane;
import gui.Main;

import java.awt.CardLayout;
import java.util.List;

import javax.swing.JPanel;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.protocols.BARRIER;
import org.jgroups.protocols.FD_ALL;
import org.jgroups.protocols.FD_SOCK;
import org.jgroups.protocols.FRAG2;
import org.jgroups.protocols.MERGE2;
import org.jgroups.protocols.MFC;
import org.jgroups.protocols.PING;
import org.jgroups.protocols.UDP;
import org.jgroups.protocols.UFC;
import org.jgroups.protocols.UNICAST2;
import org.jgroups.protocols.VERIFY_SUSPECT;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.protocols.pbcast.STATE_TRANSFER;
import org.jgroups.stack.ProtocolStack;

/**
 * The connection used with any network game (including set-up)
 * 
 * @author Soheil Koushan
 * 
 */
public class GameConnection extends NetworkConnection implements
		GameStateChangeListener {

	public Main rootPanel;

	private LobbyPanel lobbyPanel;
	// private LobbyConnection lobbyConnection;
	private NetworkPlayer player;
	private Game game = null;
	private GamePanel gamePanel; // displays the game
	private Thread gameThread;

	@Override
	public void viewAccepted(View new_view) {
		if (view != null && !view.getCreator().equals(new_view.getCreator())) {
			System.out.println("Host has left.");
			quit();
		}
		// host = new_view.getCreator();
		System.out.println("New view in GameConnection:" + new_view);

		if (game != null && isHost()) { // game has started & I'm host
			for (int i = 0; i < view.size(); i++)
				// finds members who left
				if (!new_view.containsMember(view.getMembers().get(i))) {
					AbstractPlayer[] players = game.getGameState().getPlayers();
					NetworkPlayer p = (NetworkPlayer) players[i]; // get missing
																	// player
					System.out.println("The player that's missing is " + p);
					System.out.println("His cards were: " + p.getCards());
					CPUPlayer cpu = new CPUPlayer();
					cpu.addAll(p.getCards());
					game.setPlayer(i, cpu); // replace with CPU
				}
		}
		super.viewAccepted(new_view);
	}

	public GamePanel getPanel() {
		return gamePanel;
	}

	public boolean isHost() {
		return address.equals(channel.getView().getCreator());
	}

	/**
	 * Setups up the gamePanel and starts the game if is host
	 * 
	 * @param state
	 * @throws Exception
	 */
	public void start(GameState state) throws Exception {
		System.out.println("**start() invoked on " + address);
		lobbyPanel.getConnection().disconnect();
		gamePanel = new GamePanel(rootPanel, this);

		if (isHost() || state == null) { // set up the game
			System.out.format("I [%s] am host, so I'm starting the game. ",
					address);
			/* set up game and gamePanel */
			game = new Game(initPlayers());
			game.addGameStateChangeListener(this);
			notifyNetworkChangeListeners();
			gamePanel.gameStateChanged(game.getGameState());

			/* send initialize command */
			Message msg = new Message(null, null, game.getGameState());
			msg.putHeader(Command.HEADER_ID, new Command('i'));
			channel.send(msg);
			System.out.println("Sent the initialize command to all.");
		} else
			// use the GameState sent over
			gamePanel.gameStateChanged(state);

		/* setup the current player with the gamePanel */
		AbstractPlayer[] players = gamePanel.getState().getPlayers();
		player = (NetworkPlayer) players[view.getMembers().indexOf(address)];
		player.setConnection(this); // player will use this connection
		player.gameStateChanged(gamePanel.getState());
		gamePanel.setLocalPlayerIndex(player.getIndex());
		gamePanel.addCardSelectionReceiver(player);

		/* display this game with a networkPanel (includes the chat) */
		rootPanel.add(new NetworkSplitPane(gamePanel, this), "game");
		rootPanel.getCardLayout().show(rootPanel, "game");

		if (isHost()) {
			/* start the game on a new Thread */
			System.out.format(
					"I [%s] am host so I'm starting the game loop.%n", address);
			gameThread = new Thread(game);
			gameThread.start();
		}
	}

	/**
	 * Registers the network players and sets remaining players as CPU's
	 * 
	 * @return AbstractPlayer[]
	 */
	private AbstractPlayer[] initPlayers() {
		System.out.format("[%s] is initializing players. ", address);
		List<Address> networkPlayers = view.getMembers();
		System.out.println("Network players are: " + networkPlayers);

		AbstractPlayer[] players = new AbstractPlayer[4];
		int i;
		for (i = 0; i < networkPlayers.size(); i++) {
			players[i] = new NetworkPlayer(networkPlayers.get(i), i);
		}
		while (i < 4) {
			players[i] = new CPUPlayer();
			i++;
		}

		return players;
	}

	public GameConnection(Main rootPanel, LobbyPanel lobbyPanel,
			String cluster_name, String username) throws Exception {
		super(cluster_name, username);
		this.rootPanel = rootPanel;
		this.lobbyPanel = lobbyPanel;
	}



	@Override
	/**
	 * Takes actions based on messages received
	 */
	public void receive(Message msg) {
		super.receive(msg);
		Command hdr = (Command) msg.getHeader(Command.HEADER_ID);
		switch (hdr.command) {
		case 's': // sync state
			System.out.format(
					"[%s] Received command to sync gameState from %s: %s%n",
					address, msg.getSrc(), (GameState) msg.getObject());
			if (gamePanel != null) {
				GameState new_state = (GameState) msg.getObject();
				gamePanel.gameStateChanged(new_state);
				player.gameStateChanged(new_state);
			}
			break;
		case 'i': // initialize the game
			if (!msg.getSrc().equals(address)) { // if not sent by myself
				try {
					System.out
							.format("[%s]: received initialize command, with GameState %s%n",
									address, (GameState) msg.getObject());
					start((GameState) msg.getObject());
				} catch (Exception e) {
					System.err.println("unable to start game");
					e.printStackTrace();
				}
			}
			break;
		case 'c': // process received cards
			System.out.format("[%s]: Received and setting : %s from %s%n.",
					address, (Combination) msg.getObject(), msg.getSrc());
			System.out.println("current player being "
					+ game.getGameState().getCurrentPlayer());
			NetworkPlayer p = (NetworkPlayer) game.getGameState()
					.getCurrentPlayer(); // get current player
			p.setSelectedCombination((Combination) msg.getObject()); // set his
																		// cards
			gameStateChanged(game.getGameState()); // sync to all
			break;
		case 't': // a notice that it's this player's turn
			if (gamePanel == null)
				System.out
						.format("%s is asking for a turn, but gamePanel is null!");
			else
				System.out
						.format("[%s]: %s is asking for a turn. LocalPlayerIndex is %d. State is %s%n",
								address, msg.getSrc(),
								gamePanel.getLocalPlayerIndex(),
								gamePanel.state);
			break;
		}
	}

	public void quit() {
		/* dispose of this gameConnection */
		if (game != null)
			game = null;

		channel.close();
		gamePanel = null;

		/* show the lobby */
		lobbyPanel.getConnection().restart();
		((CardLayout) lobbyPanel.getLayout()).first(lobbyPanel);
		rootPanel.getCardLayout().show(rootPanel, "lobby");
	}

	@Override
	/** sends the game state to all network players as a JGroups message*/
	public synchronized void gameStateChanged(GameState state) {
		System.out
				.format("[%s]: gameStateChanged() called: %s", address, state);
		System.out.format("I'm host so I'm telling everyone%n", address);
		Message msg = new Message(null, null, state);
		msg.putHeader(Command.HEADER_ID, new Command('s'));
		try {
			channel.send(msg);
		} catch (Exception e) {
			System.err.println("Unable to send the GameState message");
			e.printStackTrace();
		}

	}

	/**
	 * Terminates the game and gamePanel and returns to the game-setup screen so
	 * a new game can be started
	 */
	public void restart() {
		gameThread = null;
		gamePanel = null;
		rootPanel.getCardLayout().show(rootPanel, "lobby");
		((CardLayout) lobbyPanel.getLayout()).show(lobbyPanel, "game-setup");
	}

}
