package gui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class HTMLPane extends JPanel {

	private Main rootPanel;

	public HTMLPane(Main rootPanel, String resource) {
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		this.rootPanel = rootPanel;

		JButton back = new JButton("Back");
		back.addActionListener(new ButtonListener());
		add(back);

		URL url = this.getClass().getResource(resource);

		JEditorPane ep;
		try {
			ep = new JEditorPane(url);
			ep.setEditable(false);
			add(new JScrollPane(ep));
			ep.setAlignmentX(Component.LEFT_ALIGNMENT);
		} catch (IOException e) {
			e.printStackTrace();
			add(new JLabel("Unable to load the instructions file"));
		}
		back.setAlignmentX(Component.LEFT_ALIGNMENT);
	}

	class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			rootPanel.getCardLayout().first(rootPanel);
		}

	}

}
