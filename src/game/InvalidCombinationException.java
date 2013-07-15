package game;

/**
 * The cards do not form a valid combination.
 * @author Gabriel
 *
 */
public class InvalidCombinationException extends Exception {
	public InvalidCombinationException(){
		super("Illegal combination!");
	}
	public InvalidCombinationException(String message){
		super(message);
	}
}
