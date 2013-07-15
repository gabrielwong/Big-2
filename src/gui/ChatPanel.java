package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import network.Command;

import org.jgroups.JChannel;
import org.jgroups.Message;

/**
 * A simple panel consisting of a JTextArea for displaying a message thread and a
 * JTextField for receiving input.
 * @author Soheil Koushan
 * 
 */
public class ChatPanel extends JPanel implements ActionListener {

	private JTextArea messages = new JTextArea();
	private JTextField input = new JTextField();
	private JChannel channel;
	private JScrollPane sp;

	public ChatPanel(JChannel channel) {
		super();
		setLayout(new BorderLayout());

		this.channel = channel;

		/* set up messages and input components */
		messages.setEditable(false);
		input.addActionListener(this);
		sp = new JScrollPane(messages);
		sp.setPreferredSize(new Dimension(600, 200));
		
		/* add components */
		add(sp, BorderLayout.CENTER);
		add(input, BorderLayout.PAGE_END);

		input.requestFocusInWindow();
	}

	/** adds a message to the messages thread */
	public void add(Message msg) {
		messages.append("[" + msg.getSrc() + "] : " + (String) msg.getObject()
				+ "\n");
		sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
	}

	/** adds a string to the messages thread */
	public void add(String s) {
		messages.append(s + "\n");
	}

	@Override
	/** Called when a message is entered in the JTextField. Sends this message to all. */
	public void actionPerformed(ActionEvent evt) {
		try {
			/* send the message to all */
			Message msg = new Message(null, null, input.getText());
			msg.putHeader(Command.HEADER_ID, new Command('m'));
			channel.send(msg);
			input.setText("");
		} catch (Exception e) {
			input.setText("Unable to send message!");
			input.selectAll();
		}
	}
}
