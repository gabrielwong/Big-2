package gui;

import game.AbstractPlayer;
import game.CardSelectionReceiver;
import game.Combination;
import game.Game;
import game.GameState;
import game.GameStateChangeListener;
import game.InvalidCombinationException;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import network.GameConnection;
import deck.Card;
import deck.Deck;

/**
 * A panel which displays the game to a user. It is also capable of receiving input from the user.
 * @author Gabriel
 *
 */
public class GamePanel extends JPanel
implements KeyListener, GameStateChangeListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<CardSelectionReceiver> cardSelectionReceivers = new ArrayList<CardSelectionReceiver>(); // receiver to send input to
	public GameState state; // state to be displayed, temporarily made public
	private boolean[] selectedCards = new boolean[13]; // array of which cards are currently selected

	private String errorText = null; // string to be displayed at the top (for errors)
	private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12); // font used in text

	public static final BufferedImage BACKGROUND = readBackground(); // image of the background
	private static final int BORDER_WIDTH = 30; // Width of border around the panel
	private static final int BUTTON_SEPARATION = 25; // Number of px between the buttons

	private int localPlayerIndex = 0; // index of player

	/* the main panel (the JFrame's content pane) */
	public Main rootPanel;
	public CardLayout layout;

	private GameConnection connection = null;
	private Game singlePlayerGame = null;

	/**
	 * Initializes a new GamePanel with the specified RootPane.
	 * @param rootPanel the RootPanel
	 */
	public GamePanel(Main rootPanel){
		this (rootPanel, null);
	}
	/**
	 * Constructor for a multiplayer game.
	 * @param rootPanel the RootPanel
	 * @param connection the GameConnection the panel should connect to
	 */
	public GamePanel(Main rootPanel, GameConnection connection){
		this.rootPanel = rootPanel;
		this.connection = connection;

		addKeyListener(this); // listen for key input

		// lay components out
		setLayout(new BorderLayout(BORDER_WIDTH, BORDER_WIDTH));
		GameDisplayPanel panel = new GameDisplayPanel(this);
		setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
		add(panel, BorderLayout.CENTER);
		add(new GameButtonPanel(this), BorderLayout.SOUTH);

		// Set sizes
		setMinimumSize(new Dimension(490,550));
		setPreferredSize(new Dimension(780, 550));
		setMaximumSize(new Dimension(1920, 1080));

		requestFocus();
	}
	/**
	 * Overrides paintComponent in Component. This displays the background and the error message if there is one.
	 */
	public void paintComponent(Graphics g){
		g.drawImage(BACKGROUND, 0, 0, null); // draw background
		// draw error message
		if (errorText != null){
			g.setColor(Color.WHITE);
			g.setFont(FONT);
			FontMetrics m = g.getFontMetrics();
			g.drawString(errorText, (getWidth() - m.stringWidth(errorText)) / 2, (BORDER_WIDTH + m.getHeight()) / 2);
		}
	}

	/**
	 * Add a listener that will be notified when the user gives input.
	 * @param csr the listener to be notified
	 */
	public void addCardSelectionReceiver(CardSelectionReceiver csr){
		if (cardSelectionReceivers.indexOf(csr) == -1)
			cardSelectionReceivers.add(csr);
	}

	/**
	 * Remove a CardSelectionReceiver.
	 * @param csr the listener to be removed
	 * @return whether the listener was removed
	 */
	public boolean removeCardSelectionReceiver(CardSelectionReceiver csr){
		return cardSelectionReceivers.remove(csr);
	}

	/**
	 * Runs when the user presses the play button. Notifies all listeners of the selection.
	 */
	private void submitCards(){
		// Only do stuff if the local player is the current player
		if (state.getCurrentPlayerIndex() == localPlayerIndex){
			if (state.getCurrentPlayer().size() == 1 && ! selectedCards[0])
				selectedCards[0] = true; // automatically selects the last card when there is one left for convenience
			try {
				sendInput(selectedCards); // Try to submit cards to listener
				resetErrorText();
			} catch (InvalidCombinationException e) {
				// Set error text to the error so that it can be displayed to user (invalid combinations)
				errorText = e.getMessage();
			}
			resetSelectedCards(); // make all cards deselected
			repaint();
		}
	}
	/**
	 * Runs when the user passes. Notifies all listeners of the pass.
	 */
	private void pass(){
		// Meaningless if it is not the local player's turn.
		if (state.getCurrentPlayerIndex() == localPlayerIndex){
			try {
				sendInput(null); //Try to pass
				resetErrorText();
			} catch (InvalidCombinationException e) {
				// Sets error text so that it can be displayed to user (cannot pass on first turn)
				errorText = e.getMessage();
			}
			resetSelectedCards();
			repaint();
		}
	}

	/**
	 * Deselect all the cards.
	 */
	private void deselectAll(){
		resetSelectedCards();
		repaint();
	}

	/**
	 * Quit the game and free up resources.
	 */
	private void quit(){
		if (connection != null)
			connection.quit();
		else{
			rootPanel.getCardLayout().show(rootPanel, "home");
			rootPanel.getCardLayout().removeLayoutComponent(this);
		}
	}

	/**
	 * Toggle the selected state of the card at cardIndex.
	 * @param cardIndex the index of the card to be toggled
	 */
	private void toggleCard(int cardIndex){
		if (cardIndex != -1 && // if a card was actually selected
				state.getCurrentPlayerIndex() == localPlayerIndex && // and it is local player's turn
				cardIndex < state.getPlayer(localPlayerIndex).size()){ // and it is not out of range
			resetErrorText();
			selectedCards[cardIndex] = ! selectedCards[cardIndex]; // toggle selected state
			repaint();
		}
	}

	/**
	 * Deselect all cards. Does not repaint.
	 */
	private void resetSelectedCards(){
		Arrays.fill(selectedCards, false);
	}

	/**
	 * Resets the error text.
	 */
	private void resetErrorText(){
		if (errorText != null){
			errorText = null;
		}
	}

	/**
	 * Send input to all listeners.
	 * @param selected the selection
	 * @throws InvalidCombinationException
	 */
	private void sendInput(boolean[] selected) throws InvalidCombinationException{
		for (int i = 0; i < cardSelectionReceivers.size(); i++){
			cardSelectionReceivers.get(i).receiveInput(selected);
		}
	}

	/**
	 * Reads the background image from the file system.
	 * @return the background image
	 */
	private static BufferedImage readBackground(){
		URL path = "".getClass().getResource("/background.jpg");
		try{
			return ImageIO.read(path);
		} catch (Exception e){
			BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
			Graphics g = image.createGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			return image;
		}
	}

	/**
	 * Sets the index of the local player. This is done so that computers over the network do not all
	 * display the same player.
	 * @param localPlayerIndex the index of the local player
	 */
	public void setLocalPlayerIndex(int localPlayerIndex){
		this.localPlayerIndex = localPlayerIndex;
	}

	/**
	 * Returns the index of the local player.
	 * @return the index of the local player
	 */
	public int getLocalPlayerIndex(){
		return localPlayerIndex;
	}

	public void setSinglePlayerGame(Game singlePlayerGame){
		this.singlePlayerGame = singlePlayerGame;
	}

	/**
	 * Part of the GameStateChangeListener. This method is called whenever the game state is changed.
	 */
	public void gameStateChanged(GameState state){
		this.state = state; // Updates the game state

		// If game is over
		if (state.isGameOver()){
			int input = JOptionPane.showConfirmDialog(this, "Would you like to play again?", "Game Over", JOptionPane.YES_NO_OPTION);
			if (input == JOptionPane.YES_OPTION){
				if (connection != null){
					connection.restart();
				} else if (singlePlayerGame != null){
					singlePlayerGame.newGame();
					(new Thread(singlePlayerGame)).start();
				}
			} else{
				quit();
			}
			return;
		} else{
			repaint();
		}
	}

	/**
	 * Processes user keyboard input. This allows for keyboard shortcuts.
	 */
	public void keyPressed(KeyEvent e){
		int keyCode = e.getKeyCode();

		switch (keyCode){
		// Toggles a card
		case KeyEvent.VK_BACK_QUOTE:
			toggleCard(0);
			break;
		case KeyEvent.VK_1:
			toggleCard(1);
			break;
		case KeyEvent.VK_2:
			toggleCard(2);
			break;
		case KeyEvent.VK_3:
			toggleCard(3);
			break;
		case KeyEvent.VK_4:
			toggleCard(4);
			break;
		case KeyEvent.VK_5:
			toggleCard(5);
			break;
		case KeyEvent.VK_6:
			toggleCard(6);
			break;
		case KeyEvent.VK_7:
			toggleCard(7);
			break;
		case KeyEvent.VK_8:
			toggleCard(8);
			break;
		case KeyEvent.VK_9:
			toggleCard(9);
			break;
		case KeyEvent.VK_0:
			toggleCard(10);
			break;
		case KeyEvent.VK_MINUS:
			toggleCard(11);
			break;
		case KeyEvent.VK_EQUALS:
			toggleCard(12);
			break;

			// Play
		case KeyEvent.VK_Q:
			submitCards();
			break;
		case KeyEvent.VK_ENTER:
			submitCards();
			break;

			// Pass
		case KeyEvent.VK_W:
			pass();
			break;
		case KeyEvent.VK_SPACE:
			pass();
			break;

			// Deselect all
		case KeyEvent.VK_E:
			resetSelectedCards();
			break;
		}
	}
	public void keyReleased(KeyEvent e){}
	public void keyTyped(KeyEvent e){}

	/**
	 * Panel that displays the hands, names of players, cards on table, turn indicator and pass indicators.
	 * @author Gabriel
	 *
	 */
	private class GameDisplayPanel extends JPanel implements MouseListener{

		private static final long serialVersionUID = 1L;
		private final GamePanel gamePanel;

		private static final int MAX_TABLE_CARD_SEPARATION = 15; // max px between cards on table

		// Minimum px distance between player hand and cards on table
		private static final int MIN_PLAYER_TABLE_SEPARATION = 30;
		private static final int CARD_TEXT_SEPARATION = 5;
		private static final int INDICATOR_GAP = 3; // gap between cards and indicator

		private BufferedImage passImage;
		private BufferedImage[] turnImages;

		/**
		 * Initializes a new panel.
		 * @param gamePanel the GamePanel containing this panel
		 */
		public GameDisplayPanel(GamePanel gamePanel){
			createPassImage();
			createTurnImages();
			this.gamePanel = gamePanel;
			resetSelectedCards();
			addMouseListener(this);
			setOpaque(false); // make transparent to let background show
		}

		/**
		 * Overrides paintComponent in Component. This displays the hands, pass indicators, turn indicators
		 * and player names.
		 */
		public void paintComponent(Graphics g){
			if (state != null){
				drawTableCards(g, state.getPreviousPlay());

				g.setFont(FONT);
				g.setColor(Color.WHITE);

				// Draw individual players
				drawNorthPlayer(g, state.getPlayer((localPlayerIndex + 2) % 4));
				drawEastPlayer(g, state.getPlayer((localPlayerIndex + 3) % 4));
				drawWestPlayer(g,state.getPlayer((localPlayerIndex + 1) % 4));
				drawSouthPlayer(g, state.getPlayer(localPlayerIndex));
				drawPassedIndicators(g);
				drawTurnIndicator(g);
			}
		}

		/**
		 * Draws the south player.
		 * @param g the graphics context to draw on
		 * @param player the player to be drawn
		 */
		private void drawSouthPlayer(Graphics g, AbstractPlayer player){
			// Draw name
			FontMetrics metrics = g.getFontMetrics();
			g.drawString(player.getName(), (getWidth() - getImageWidth(player.size())) / 2 - metrics.stringWidth(player.getName()) - CARD_TEXT_SEPARATION,
					getHeight() - metrics.getDescent());

			// Draw cards
			for (int i = 0; i < player.size(); i++){
				// Integer added to y position of card to move it up or down when selected or not
				int selectedModifier = selectedCards[i] ? 0 : Deck.CARD_TOP;
				g.drawImage(player.getCards()[i].getImage(true),
						(getWidth() - getImageWidth(player.size())) / 2 + (i * Deck.CARD_LEFTSIDE),
						getHeight() - (Card.HEIGHT + Deck.CARD_TOP) + selectedModifier, null);
			}
		}

		/**
		 * Draws the east player.
		 * @param g the graphics context to draw on
		 * @param player the player to be drawn
		 */
		private void drawEastPlayer(Graphics g, AbstractPlayer player){
			// Draw name
			FontMetrics metrics = g.getFontMetrics();
			g.drawString(player.getName(), getWidth() - metrics.stringWidth(player.getName()),
					(getHeight() - getImageWidth(player.size())) / 2 - CARD_TEXT_SEPARATION);

			// Draw cards
			for (int i = 0; i < player.size(); i++){
				g.drawImage(Card.getHorizontalBack(), getWidth() - Card.HEIGHT,
						(getHeight() - getImageWidth(player.size())) / 2 + (i * Deck.CARD_LEFTSIDE), null);
			}
		}

		/**
		 * Draws the west player.
		 * @param g the graphics context to draw on
		 * @param player the player to be drawn
		 */
		private void drawWestPlayer(Graphics g, AbstractPlayer player){
			// Draw name
			g.drawString(player.getName(), 0, (getHeight() - getImageWidth(player.size())) / 2 - CARD_TEXT_SEPARATION);

			// Draw cards
			for (int i = 0; i < player.size(); i++){
				g.drawImage(Card.getHorizontalBack(), 0,
						(getHeight() - getImageWidth(player.size())) / 2 + (i * Deck.CARD_LEFTSIDE), null);
			}
		}

		/**
		 * Draws the north player.
		 * @param g the graphics context to draw on
		 * @param player the player to be drawn
		 */
		private void drawNorthPlayer(Graphics g, AbstractPlayer player){
			// Draw name
			FontMetrics metrics = g.getFontMetrics();
			g.drawString(player.getName(), (getWidth() - getImageWidth(player.size())) / 2 - metrics.stringWidth(player.getName()) - CARD_TEXT_SEPARATION,
					Card.HEIGHT);

			// Draw cards
			for (int i = 0; i < player.size(); i++){
				g.drawImage(Card.getVerticalBack(),
						(getWidth() - getImageWidth(player.size())) / 2 + (i * Deck.CARD_LEFTSIDE), 0, null);
			}
		}

		/**
		 * Draw the cards on the table.
		 * @param g the graphics context to draw on
		 * @param combination the combination on the table
		 */
		private void drawTableCards(Graphics g, Combination combination){
			// Draw nothing if there is nothing to draw
			if (combination.getLength() == Combination.PASS)
				return;

			Card[] cards = combination.getCards();

			// Amount of horizontal space where we can draw cards
			int workableArea = getWidth() - 2 * (Card.HEIGHT + MIN_PLAYER_TABLE_SEPARATION);

			int cardSeparation; // separation between cards (can be negative to overlap)
			cardSeparation = (cards.length == 1) ? 0 : Math.min(MAX_TABLE_CARD_SEPARATION,
					(workableArea - Card.WIDTH * cards.length) / (cards.length - 1));

			// Width of image to be drawn
			int imageWidth = cards.length * Card.WIDTH + (cards.length - 1) * cardSeparation;

			for (int i = 0; i < cards.length; i++){
				g.drawImage(cards[i].getImage(true), (getWidth() - imageWidth) / 2 + // left side of image
						i * (Card.WIDTH + cardSeparation), // move images over each iteration
						(getHeight() - Card.HEIGHT) / 2, // top of image
						null);
			}
			g.setFont(FONT);
			g.setColor(Color.WHITE);
			FontMetrics metrics = g.getFontMetrics();
			g.drawString(combination.getType(), (getWidth() - metrics.stringWidth(combination.getType())) / 2,
					(getHeight() + Card.HEIGHT) / 2 + metrics.getHeight() + CARD_TEXT_SEPARATION);
		}

		/**
		 * Draw graphics to indicate which players have passed.
		 * @param g the graphics context to draw on
		 */
		private void drawPassedIndicators(Graphics g){
			// For all players
			for (int i = 0; i < 4; i++){
				int index = (4 + i - localPlayerIndex) % 4; // add 4 to avoid a negative number
				if (state.getPassed(i) && ! state.getPlayer(i).isDone()){
					switch(index){
					case 0 : // South
						g.drawImage(passImage, (getWidth() - passImage.getWidth()) / 2, getHeight() - Card.HEIGHT - INDICATOR_GAP - passImage.getHeight(), null);
						break;
					case 1 : // West
						g.drawImage(passImage, (Card.HEIGHT + INDICATOR_GAP), (getHeight() - passImage.getHeight()) / 2, null);
						break;
					case 2 : // North
						g.drawImage(passImage, (getWidth() - passImage.getWidth()) / 2, Card.HEIGHT + INDICATOR_GAP, null);
						break;
					case 3 : // East
						g.drawImage(passImage, getWidth() - Card.HEIGHT - INDICATOR_GAP - passImage.getWidth(), (getHeight() - passImage.getHeight()) / 2, null);
					}
				}
			}
		}

		/**
		 * Draw graphics to indicate which player is next to play.
		 * @param g
		 */
		private void drawTurnIndicator(Graphics g){
			if (state.getPassed(state.getCurrentPlayerIndex()))
				return;
			int index = (4 + state.getCurrentPlayerIndex() - localPlayerIndex) % 4; // gets current player relative to local player
			// 4 is added to avoid a negative number
			switch(index){
			case 0 : // South
				int selectedModifier = 0; // shift arrow up if a card is selected
				for (boolean b : selectedCards){
					if (b){
						selectedModifier = Deck.CARD_TOP;
						break;
					}
				}
				g.drawImage(turnImages[index], (getWidth() - turnImages[index].getWidth()) / 2, getHeight() - turnImages[state.getCurrentPlayerIndex()].getHeight() - Card.HEIGHT - INDICATOR_GAP - selectedModifier, null);
				break;
			case 1 : // West
				g.drawImage(turnImages[index], (Card.HEIGHT + INDICATOR_GAP), (getHeight() - turnImages[index].getHeight()) / 2, null);
				break;
			case 2 : // North
				g.drawImage(turnImages[index], (getWidth() - turnImages[index].getWidth()) / 2, Card.HEIGHT + INDICATOR_GAP, null);
				break;
			case 3 : // East
				g.drawImage(turnImages[index], getWidth() - Card.HEIGHT - INDICATOR_GAP - turnImages[index].getWidth(), (getHeight() - turnImages[state.getCurrentPlayerIndex()].getHeight()) / 2, null);
			}
		}

		/**
		 * Returns the width of a hand of a given size when drawn.
		 * @param size the number of cards in the hand
		 * @return the width of the hand when drawn
		 */
		private int getImageWidth(int size){
			return ((size - 1) * Deck.CARD_LEFTSIDE + Card.WIDTH);
		}

		/**
		 * Returns the index of the card that is clicked.
		 * @param x the x coordinate of the click
		 * @param y the y coordinate of the click
		 * @param handsize the number of cards in the south player's hand
		 * @return the index of the card that is clicked, -1 if no card is selected
		 */
		private int getCardIndexFromMouse(int x, int y, int handsize){
			if (handsize < 1 || handsize > 13)
				return -1; // invalid parameter

			int cardNum;
			int imageHeight = (Card.HEIGHT + Deck.CARD_TOP); // height of hand when drawn
			int imageWidth = getImageWidth(state.getPlayer(localPlayerIndex).size()); // width of image

			// y value out of range
			if (y < (getHeight() - imageHeight))
				return -1;

			// x value out of range
			if (x < (getWidth() - imageWidth) / 2 ||
					x > (getWidth() + imageWidth) / 2)
				return -1;

			// Get index of card
			cardNum = (x - ((getWidth() - imageWidth) / 2)) / Deck.CARD_LEFTSIDE;

			// Makes sure the index is correct when user clicks the rightmost card in the hand (wider than others)
			if (cardNum >= state.getPlayer(localPlayerIndex).size())
				cardNum = state.getPlayer(localPlayerIndex).size() - 1;

			return cardNum;
		}

		/**
		 * This is run when the user clicks within the bounds of the panel.
		 */
		public void mouseClicked(MouseEvent e) {
			// Try to toggle the card that is selected
			int cardIndex = getCardIndexFromMouse(e.getX(), e.getY(), state.getPlayer(localPlayerIndex).size());
			toggleCard(cardIndex);

			gamePanel.requestFocus();
		}
		public void mouseEntered(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
		public void mousePressed(MouseEvent arg0) {}
		public void mouseReleased(MouseEvent arg0) {}

		/**
		 * Draw pass image (red square)
		 */
		private void createPassImage(){
			passImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
			Graphics g = passImage.getGraphics();
			g.setColor(Color.RED);
			g.fillRect(0, 0, passImage.getWidth(), passImage.getHeight()); // draw red square
		}

		/**
		 * Draw turn images (green triangle)
		 */
		private void createTurnImages(){
			// Create BufferedImages
			turnImages = new BufferedImage[4];
			for (int i = 0; i < turnImages.length; i++)
				turnImages[i] = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);

			// Set up polygon
			Polygon triangle = new Polygon();
			triangle.addPoint(0, 0); // top left corner
			triangle.addPoint(turnImages[0].getWidth() / 2, turnImages[0].getHeight()); // bottom middle
			triangle.addPoint(turnImages[0].getWidth(), 0); // top right

			for (int i = 0 ; i < turnImages.length; i++){
				Graphics2D g = turnImages[i].createGraphics();
				g.setColor(Color.GREEN);

				// Translate graphics to get drawing to be in the right position after rotation
				switch(i){
				case 0 :
					break;
				case 1 :
					g.translate(turnImages[i].getWidth(), 0);
					break;
				case 2:
					g.translate(turnImages[i].getWidth(), turnImages[i].getHeight());
					break;
				case 3:
					g.translate(0, turnImages[i].getHeight());
				}
				g.rotate(Math.PI / 2 * i);
				g.fillPolygon(triangle); // draw the triangle
			}
		}
	}

	/**
	 * Panel that displays the buttons and handles button events.
	 * @author Gabriel
	 *
	 */
	private class GameButtonPanel extends JPanel implements MouseListener, ActionListener{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final GamePanel gamePanel;

		/**
		 * Initializes a new panel.
		 * @param gamePanel
		 */
		public GameButtonPanel(final GamePanel gamePanel){
			this.gamePanel = gamePanel;
			setOpaque(false); // make it transparent to allow background to show
			addMouseListener(this); // pass mouse event to GamePanel
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS)); // set layout manager
			String[] buttonText = {"Play", "Pass", "Deselect All", "Quit"}; // text of buttons
			JButton[] buttons = new JButton[buttonText.length]; // array of buttons

			// Create and add buttons
			for (int i = 0; i < buttonText.length; i++){
				buttons[i] = new JButton(buttonText[i]);
				buttons[i].addActionListener(this);
				buttons[i].setOpaque(false);
				add(buttons[i]);
			}

			// Space out the buttons
			Dimension minSize = new Dimension(0, 0);
			Dimension prefSize = new Dimension(BUTTON_SEPARATION, 0);
			Dimension maxSize = prefSize;
			add(new Box.Filler(minSize, prefSize, maxSize), 1);
			add(new Box.Filler(minSize, prefSize, maxSize), 3);
			add(Box.createHorizontalGlue(), 5);
		}

		/**
		 * Gives focus back to gamePanel when clicked.
		 */
		public void mouseClicked(MouseEvent e){
			gamePanel.requestFocus();
		}
		public void mouseEntered(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
		public void mousePressed(MouseEvent arg0) {}
		public void mouseReleased(MouseEvent arg0) {}

		/**
		 * Handle button clicks.
		 */
		public void actionPerformed(ActionEvent e){
			String name = ((JButton)e.getSource()).getText();
			if ("Play".equals(name)){
				submitCards();

			} else if ("Pass".equals(name)){
				pass();
			} else if ("Deselect All".equals(name)){
				deselectAll();
			} else if ("Quit".equals(name)){
				quit();
				return;
			}
			gamePanel.requestFocus(); // give focus back to gamePanel
		}
	}

	/**
	 * Returns the current game state
	 * @return the game state
	 */
	public GameState getState () {
		return state;
	}

}
