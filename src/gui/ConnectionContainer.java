package gui;

import javax.swing.JSplitPane;

import network.NetworkConnection;

/**
 * Used to specify that the class contains a NetworkConnection
 * @author Soheil Koushan
 * 
 */
public interface ConnectionContainer {
	public NetworkConnection getConnection();
}
