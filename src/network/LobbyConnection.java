package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.util.Util;

/**
 * A connection used by LobbyPanel to show all the players
 * @author Soheil Koushan
 * 
 */
public class LobbyConnection extends NetworkConnection {

	private ArrayList<Address> clients = new ArrayList<Address>();
	private ArrayList<Address> hosts = new ArrayList<Address>();

	public ArrayList<Address> getClients() {
		return clients;
	}

	public ArrayList<Address> getHosts() {
		return hosts;
	}

	/** Removes any players who have left or joined from the lists */
	public void viewAccepted(View new_view) {
		super.viewAccepted(new_view);

		/* remove players who were in the hold view but not the new_view */
		if (clients != null)
			for (int i = 0; i < clients.size(); i++)
				if (!new_view.containsMember(clients.get(i)))
					clients.remove(i);
		if (hosts != null)
			for (int i = 0; i < hosts.size(); i++)
				if (!new_view.containsMember(hosts.get(i)))
					hosts.remove(i);

		System.out.println("** View: " + new_view);
		syncState();
	}

	/** Write the state to the OutputStream (used by JGroups) */
	public void getState(OutputStream output) throws Exception {
		System.out.println("get state invoked from "
				+ channel.getAddressAsString());
		Util.writeAddresses(hosts, new DataOutputStream(output));
	}

	/** Reads in the state sent over from the InputStream */
	public void setState(InputStream input) throws Exception {
		clients = new ArrayList<Address>();
		hosts = new ArrayList<Address>();
		System.out.println("Set state invoked from "
				+ channel.getAddressAsString());
		clients.clear();
		hosts.clear();

		/* read in hosts */
		ArrayList<Address> hostAddresses = (ArrayList<Address>) Util
				.readAddresses(new DataInputStream(input), ArrayList.class);
		if (hostAddresses != null)
			hosts.addAll(hostAddresses);

		/* the rest connected to the network and clients */
		for (int i = 0; i < view.size(); i++) {
			Address memb = view.getMembers().get(i);
			if (!hosts.contains(memb))
				clients.add(memb);
		}
		System.out.println("New players and hosts are " + this);
		notifyNetworkChangeListeners();
	}

	/**
	 * The username to use for the JGroups cluster
	 * @param username
	 * @throws Exception
	 */
	public LobbyConnection(String username) throws Exception {
		super("lobby", username);
		if (view.size() < 2) // if first player, getState(InputStream) is not called
			// therefore, this must add itself manually
			clients.add(channel.getAddress());
		else
			channel.getState(null, 10000);
	}

	/** called when a message is received (always a sync command) */
	public void receive(Message msg) {
		super.receive(msg);
		Command hdr = (Command) msg.getHeader(Command.HEADER_ID);
		switch (hdr.command) {
		case 's': // sync messages
			try {
				channel.getState(msg.getSrc(), 10000);
			} catch (Exception e) {
				System.err.println("Unable to getState and sync information.");
				e.printStackTrace();
			}
			break;
		}
	}

	public void becomeHost() throws Exception {
		hosts.add(channel.getAddress());
		syncState();
	}

	/** tells all clients to getState from me */
	public void syncState() {
		Message msg = new Message(null, null, null);
		msg.putHeader(Command.HEADER_ID, new Command('s'));
		try {
			channel.send(msg);
		} catch (Exception e) {
			System.err.println("Unable to send the sync message");
			e.printStackTrace();
		}
		notifyNetworkChangeListeners();
	}

	public String toString() {
		return String.format("Clients: %s, Hosts: %s", clients, hosts);
	}

	/** disconnects from the cluster and reconnects */
	public void restart() {
		try {
			channel.disconnect();
			channel.connect("lobby");
			if (view.size() < 2) // if first player
				clients.add(channel.getAddress()); // add myself to clients
			else
				channel.getState(null, 10000);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		syncState();
	}

	public void close() {
		channel.close();
	}

}
