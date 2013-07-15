package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import network.GameConnection;
import network.LobbyConnection;
import network.NetworkChangeListener;
import network.NetworkConnection;

import org.jgroups.Address;

/**
 * A JPanel that displays the network set-up screen
 * @author Soheil Koushan
 * 
 */
public class NetworkGamePanel extends JPanel implements NetworkChangeListener,
		ConnectionContainer {

	private JList<Address> players;
	private DefaultListModel<Address> players_model = new DefaultListModel<Address>();

	// private LobbyConnection lobbyConnection;
	private GameConnection connection;

	private Main rootPanel;
	private LobbyPanel lobbyPanel;

	/** Creates and displays the network set up screen. */
	public NetworkGamePanel(Main rootPanel, LobbyPanel lobbyPanel,
			String host, String username) {
		super(new BorderLayout());
		this.rootPanel = rootPanel;
		this.lobbyPanel = lobbyPanel;

		/* set up the game connection */
		try {
			connection = new GameConnection(rootPanel,
					lobbyPanel, host, username);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"Could not create a new GameConnection! Here's the error: "
							+ e.toString(), "GameConnection Setup Failed!",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}
		connection.addNetworkChangeListener(this);

		/* set up players JList */
		players = new JList<Address>(players_model);
		players.setLayoutOrientation(JList.VERTICAL);

		/* sets up the buttons */
		JPanel buttons = new JPanel();
		ButtonListener listener = new ButtonListener();

		if (host.equals(username)) { // add start button if host
			JButton start = new JButton("Start");
			start.addActionListener(listener);
			start.setActionCommand("start");
			buttons.add(start);
		}

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(listener);
		cancel.setActionCommand("cancel");
		buttons.add(cancel);

		add(new JLabel("Network Players:"), BorderLayout.PAGE_START);
		add(players, BorderLayout.CENTER);
		add(buttons, BorderLayout.PAGE_END);
	}

	@Override
	/** updates the JList whenever the network connection changes */
	public void networkConnectionChanged(NetworkConnection c) {
		connection = (GameConnection) c;
		if (players_model != null) {
			players_model.clear();
			List<Address> channelMembers = connection.view.getMembers();
			for (int i = 0; i < channelMembers.size(); i++)
				players_model.addElement(channelMembers.get(i));
		}
	}

	/** listens to button clicks and takes appropriate action */
	class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			if (evt.getActionCommand().equals("cancel")) { // return to lobby
				connection.close();
				lobbyPanel.getConnection().restart();
			} else if (evt.getActionCommand().equals("start")) { // start the game
				lobbyPanel.getConnection().disconnect();
				try {
					rootPanel.add(new NetworkSplitPane(connection.getPanel(),
							connection), "game");
					rootPanel.getCardLayout().show(rootPanel, "game");
					connection.start(null); // null because this player is host
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null,
							"Unable to start the game! Here's the message: "
									+ e.getMessage(), "Game Start Failed!",
							JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
			((CardLayout) lobbyPanel.getLayout()).first(lobbyPanel);
		}
	}

	@Override
	public GameConnection getConnection() {
		return connection;
	}
}
