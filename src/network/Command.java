/**
 * 
 */
package network;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.jgroups.Header;
import org.jgroups.util.Streamable;

/**
 * A custom header containing a char which represents a command. A header is
 * used so that this information is separate from the message payload.
 * 
 * @author Soheil Koushan
 */
public class Command extends Header implements Streamable {

	/**
	 * Used by JGroups
	 */
	public static short HEADER_ID = 1900;

	/**
	 * <ul>
	 * <li>i - initialize game
	 * <li>t - doTurn
	 * <li>c - a Combination (returned by doTurn)
	 * <li>s - syncState
	 * <li>m - chat message
	 * </ul>
	 */
	public char command;

	public Command() {
	}

	public Command(char command) {
		this.command = command;
	}

	public String toString() {
		return "Command=" + command;
	}

	@Override
	public void writeTo(DataOutput out) throws IOException {
		out.writeChar(command);
	}

	@Override
	public void readFrom(DataInput in) throws IOException,
			IllegalAccessException, InstantiationException {
		command = in.readChar();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgroups.Header#size()
	 */
	@Override
	public int size() {
		return 2;
	}

}
