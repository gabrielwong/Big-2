package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import network.LobbyConnection;
import network.NetworkChangeListener;
import network.NetworkConnection;

import org.jgroups.Address;

/**
 * A JPanel that displays the lobby.
 * @author Soheil Koushan
 * 
 */
public class LobbyPanel extends JPanel implements NetworkChangeListener,
		ListSelectionListener, ConnectionContainer {

	private JList<Address> hosts;
	private JList<Address> clients;

	/* the data for the JLists */
	private DefaultListModel<Address> hosts_model = new DefaultListModel<Address>();
	private DefaultListModel<Address> clients_model = new DefaultListModel<Address>();

	private JButton joinButton;
	private String joinString = "Join";
	private JButton hostButton;
	private String hostString = "Host";
	private JButton refreshButton;
	private String refreshString = "Refresh";
	private JButton backButton;

	private ButtonListener buttonListener;

	private Main rootPanel;
	private LobbyConnection connection;

	public LobbyPanel(Main rootPanel) {
		super(new CardLayout());
		this.rootPanel = rootPanel;

		/* get a username to use for the network connection */
		String username = (String) JOptionPane.showInputDialog(rootPanel,
				"Enter your username.", "Network Username Setup",
				JOptionPane.PLAIN_MESSAGE);
		try {
			connection = new LobbyConnection(username);
			connection.addNetworkChangeListener(this); // listen to changes
		} catch (Exception e) {
			System.err
					.println("Couldn't establish connection to the pool of players.");
			e.printStackTrace();
			return;
		}

		buttonListener = new ButtonListener(this);

		/* set up hosts and clients data */
		replaceAll(hosts_model, connection.getHosts());
		replaceAll(clients_model, connection.getClients());

		/* create hosts and clients JLists */
		hosts = new JList<Address>(hosts_model);
		clients = new JList<Address>(clients_model);
		hosts.addListSelectionListener(this);

		/* set up players list panel */
		JPanel players = new JPanel();
		hostButton = new JButton(hostString);
		joinButton = new JButton(joinString);
		joinButton.setEnabled(false);

		players.add(createListPane("Hosts", hosts, hosts_model, joinButton,
				joinString));
		players.add(createListPane("Clients", clients, clients_model,
				hostButton, hostString));

		/* set up title JLabel and refresh button */
		JPanel page_start = new JPanel();
		refreshButton = new JButton(refreshString);
		refreshButton.setActionCommand(refreshString);
		refreshButton.addActionListener(buttonListener);
		backButton = new JButton("Back");
		backButton.setActionCommand("Back");
		backButton.addActionListener(buttonListener);

		page_start.add(backButton);
		page_start.add(new JLabel("Network Setup"));
		page_start.add(refreshButton);

		JPanel lobbyPanel = new JPanel(new BorderLayout());
		lobbyPanel.add(page_start, BorderLayout.PAGE_START);
		lobbyPanel.add(players, BorderLayout.CENTER);

		add(lobbyPanel, "lobby");
		((CardLayout) getLayout()).show(this, "lobby");

		/* add and show this panel */
		rootPanel.add(new NetworkSplitPane(this), "lobby");
		rootPanel.getCardLayout().show(rootPanel, "lobby");
	}

	/* sets up a list pane for hosts and clients */
	private JPanel createListPane(String title, JList<Address> list,
			DefaultListModel<Address> model, JButton button, String action) {
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setBorder(BorderFactory.createTitledBorder(title));
		JScrollPane sp = new JScrollPane(list);
		sp.setPreferredSize(new Dimension(200, 150));

		button.setActionCommand(action);
		button.addActionListener(buttonListener);
		button.setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		pane.add(sp);
		pane.add(button);

		return pane;
	}

	/** listens to all the buttons and takes appropriate actions */
	class ButtonListener implements ActionListener {
		LobbyPanel lobbyPanel;

		public ButtonListener(LobbyPanel p) {
			lobbyPanel = p;
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			String actionCommand = evt.getActionCommand(); // get command
			if (actionCommand.equals(hostString)) {
				try {
					String name = connection.address.toString();
					/* set-up and show a new NetworkGamePanel */
					lobbyPanel.add(new NetworkGamePanel(rootPanel, lobbyPanel,
							name, name), "game-setup");
					((CardLayout) lobbyPanel.getLayout()).show(lobbyPanel,
							"game-setup");
					connection.becomeHost();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(
							null,
							"Could not become the host! Here's the error: "
									+ e.toString(), "Hosting Failed!",
							JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			} else if (actionCommand.equals(joinString)) {
				try {
					// connection.disconnect(); // disconnect from lobby
					/* set-up and show a new NetworkGamePanel */
					lobbyPanel.add(new NetworkGamePanel(rootPanel, lobbyPanel,
							hosts.getSelectedValue().toString(),
							connection.address.toString()), "network");
					((CardLayout) lobbyPanel.getLayout()).show(lobbyPanel,
							"network");
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null,
							"Could not connect to the host! Here's the error: "
									+ e.toString(), "Host Connection Failed!",
							JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			} else if (actionCommand.equals(refreshString)) {
				refresh();
			} else if (actionCommand.equals("Back")) {
				rootPanel.getCardLayout().first(rootPanel);
			}
		}

	}

	/** replaces all of the JList data with the data of an ArrayList */
	public void replaceAll(DefaultListModel<Address> lm, ArrayList<Address> l) {
		lm.clear();
		for (int i = 0; i < l.size(); i++) {
			lm.addElement(l.get(i));
		}
	}

	/** called by the refresh button. calls restart() on the connection */
	public void refresh() {
		connection.restart();
		replaceAll(hosts_model, connection.getHosts());
		replaceAll(clients_model, connection.getClients());
	}

	@Override
	public void networkConnectionChanged(NetworkConnection c) {
		LobbyConnection connection = (LobbyConnection) c;
		replaceAll(hosts_model, connection.getHosts());
		replaceAll(clients_model, connection.getClients());
	}

	@Override
	/** disables join button when there are no hosts */
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {
			if (hosts.getSelectedIndex() == -1) {
				// No selection, disable fire button.
				joinButton.setEnabled(false);

			} else {
				// Selection, enable the fire button.
				joinButton.setEnabled(true);
			}
		}
	}

	@Override
	public LobbyConnection getConnection() {
		return connection;
	}

}
