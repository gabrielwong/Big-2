package network;

/** used for any class that cares about changes in the NetworkConnection */
public interface NetworkChangeListener {
	public void networkConnectionChanged(NetworkConnection c);

}
