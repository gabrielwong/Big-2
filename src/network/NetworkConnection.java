package network;

import java.util.ArrayList;

import gui.ChatPanel;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.stack.ProtocolStack;

/**
 * An extension of ReceiverAdapter containing generic fields and methods for any JGroups connection
 * in this game
 * 
 * @author Soheil Koushan
 */
public class NetworkConnection extends ReceiverAdapter {

	public JChannel channel;
	public View view;
	public Address address;
	public String username;
	public ChatPanel chat = null;

	private ArrayList<NetworkChangeListener> listeners = new ArrayList<NetworkChangeListener>();

	public void notifyNetworkChangeListeners() {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).networkConnectionChanged(this);
		}
	}

	public void addNetworkChangeListener(NetworkChangeListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
		listener.networkConnectionChanged(this);
	}

	public boolean removeNetworkChangeListener(NetworkChangeListener listener) {
		return listeners.remove(listener);
	}

	public NetworkConnection(String cluster_name) throws Exception {
		this(cluster_name, null, null);
	}

	public NetworkConnection(String cluster_name, String username)
			throws Exception {
		this(cluster_name, username, null);
	}

	/**
	 * The complete constructor.
	 * 
	 * @param cluster_name
	 *            name of JGroups cluster
	 * @param username
	 * @param stack
	 *            used to specify the protocol stack used by the channel
	 */
	public NetworkConnection(String cluster_name, String username,
			ProtocolStack stack) throws Exception {
		System.setProperty("java.net.preferIPv4Stack" , "true");

		if (stack != null) {
			channel = new JChannel(false);
			channel.setProtocolStack(stack);
			stack.init();
		} else
			channel = new JChannel();
		
		if (username != null && username.length() > 0) {
			this.username = username;
			channel.setName(username);
		}
		channel.setReceiver(this);

		// define the custom header at Command.HEADER_ID
		if (ClassConfigurator.get(Command.HEADER_ID) == null)
			ClassConfigurator.add(Command.HEADER_ID, Command.class);

		channel.connect(cluster_name);
		address = channel.getAddress();

		chat = new ChatPanel(channel);
	}

	public void viewAccepted(View new_view) {
		view = new_view;
		notifyNetworkChangeListeners();
	}

	public ChatPanel getChat() {
		return chat;
	}

	public void receive(Message msg) {
		Command hdr = (Command) msg.getHeader(Command.HEADER_ID);
		if (hdr.command == 'm' && chat != null)
			chat.add(msg);
	}

	public void close() {
		channel.close();
	}

	public void disconnect() {
		channel.disconnect();
	}
}
