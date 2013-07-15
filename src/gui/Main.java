package gui;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Content pane of the frame.
 * @author Gabriel
 *
 */
public class Main extends JPanel{
	private CardLayout cardLayout;
	public Main() {
		cardLayout = new CardLayout();
		setLayout(cardLayout);

		new MainPanel(this);
	}

	/**
	 * Returns the CardLayout used by this panel.
	 * @return this panel's CardLayout
	 */
	public CardLayout getCardLayout(){
		return cardLayout;
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Network Big 2 Card Game");
		frame.setContentPane(new Main());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

}
