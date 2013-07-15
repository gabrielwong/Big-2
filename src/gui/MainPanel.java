package gui;

import game.Game;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * Panel which displays the main menu for the program.
 * @author Gabriel
 *
 */
public class MainPanel extends JPanel implements ActionListener {
	private static final BufferedImage TITLE_IMAGE = readTitleImage();
	private static final BufferedImage CARD_IMAGE = readCardImage();
	private static final int IMAGE_SPACING = 50; // space between title image and card image
	public Main rootPanel;

	/* workaround to CardLayout no way of checking whether a component is already added */
	private boolean lobbyAdded = false;

	public MainPanel(Main rootPanel) {
		this.rootPanel = rootPanel;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // for the buttons

		// Create buttons and lay them out on the panel properly
		String[] buttonText = { "Single Player", "Multiplayer", "Instructions",
				"Quit" };
		JButton[] buttons = new JButton[buttonText.length];

		add(new Box.Filler(new Dimension(0, 30 + TITLE_IMAGE.getHeight()),
				new Dimension(0, 80 + TITLE_IMAGE.getHeight()), new Dimension(
						0, Short.MAX_VALUE)));
		Dimension min = new Dimension(0, 0);
		Dimension max = new Dimension(0, 10);
		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new JButton(buttonText[i]);
			buttons[i].setAlignmentX(Component.CENTER_ALIGNMENT);
			buttons[i].setActionCommand(buttonText[i]);
			buttons[i].addActionListener(this);
			add(buttons[i]);
			add(new Box.Filler(min, max, max));
		}
		add(Box.createVerticalGlue());
		
		// Set sizes
		setMinimumSize(new Dimension(680, 325));
		setPreferredSize(new Dimension(750, 700));

		/* add to mainPanel and show it */
		rootPanel.add(this, "home");
		rootPanel.getCardLayout().show(rootPanel, "home");
	}

	/**
	 * Paint the background, title image and card image.
	 */
	public void paintComponent(Graphics g) {
		g.drawImage(GamePanel.BACKGROUND, 0, 0, null); // draw background
		g.drawImage(TITLE_IMAGE, (getWidth() - TITLE_IMAGE.getWidth()
				- CARD_IMAGE.getWidth() - IMAGE_SPACING) / 2, 20, null); // draw title image
		g.drawImage(CARD_IMAGE, (getWidth() - CARD_IMAGE.getWidth()
				+ TITLE_IMAGE.getWidth() + IMAGE_SPACING) / 2, 10, null); // draw card image
	}

	/**
	 * Handle button clicks.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("Single Player")) {
			// Create a new single player game
			GamePanel panel = new GamePanel(rootPanel);
			Game game = Game.createSinglePlayerGame();
			game.addGameStateChangeListener(panel);
			panel.addCardSelectionReceiver(game.getGameState().getPlayer(0));
			panel.setSinglePlayerGame(game);
			rootPanel.add(panel, "single");
			rootPanel.getCardLayout().show(rootPanel, "single");
			(new Thread(game)).start();
		} else if (cmd.equals("Multiplayer")) {
			// Send you to lobby
			if (!lobbyAdded) {
				rootPanel.add(new NetworkSplitPane(new LobbyPanel(rootPanel)),
						"lobby");
				lobbyAdded = true;
			}
			((CardLayout) rootPanel.getLayout()).show(rootPanel, "lobby");
		} else if (cmd.equals("Instructions")) {
			// Display instructions
			rootPanel.add(new HTMLPane(rootPanel,
					"/instructions/instructions.html"), "instructions");
			rootPanel.getCardLayout().show(rootPanel, "instructions");
		} else if (cmd.equals("Quit")) {
			System.exit(0);
		}
	}

	/**
	 * Reads the title image from file.
	 * @return the title image
	 */
	private static BufferedImage readTitleImage() {
		URL path = "".getClass().getResource("/Title.png");
		try {
			return ImageIO.read(path);
		} catch (Exception e) {
			return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		}
	}

	/**
	 * Reads the card image from file.
	 * @return the card image
	 */
	private static BufferedImage readCardImage() {
		URL path = "".getClass().getResource("/Cards.png");
		try {
			return ImageIO.read(path);
		} catch (Exception e) {
			return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		}
	}
}
