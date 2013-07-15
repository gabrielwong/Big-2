package gui;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import network.NetworkConnection;

/**
 * A JSplitPane that includes another panel as well as its connection's associated chat
 * pane.
 * @author Soheil Koushan
 */
public class NetworkSplitPane extends JSplitPane {


	/**
	 * 
	 * @param main should implement ConnectionContainer
	 */
	public NetworkSplitPane (JPanel main){
		super (JSplitPane.VERTICAL_SPLIT);
		setTopComponent (main);
		
		if (main instanceof ConnectionContainer) {
			ConnectionContainer c = (ConnectionContainer)main;
			setBottomComponent (c.getConnection().getChat());
		}
		setOneTouchExpandable(true);
	}
	

	/**
	 * Used to specify the NetworkConnection.
	 * @param main the main content (top component)
	 * @param c
	 */
	public NetworkSplitPane (JPanel main, NetworkConnection c){
		super (JSplitPane.VERTICAL_SPLIT, main, c.getChat());
		setOneTouchExpandable(true);
	}
}
