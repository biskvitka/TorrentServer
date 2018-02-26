package bg.uni.sofia.fmi.peer.exception;

public class InvalidCommandException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidCommandException() {
		super();
	}

	public InvalidCommandException(String message) {
		super(message);
	}
}
