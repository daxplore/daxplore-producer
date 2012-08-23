package daxplorelib;

public class DaxploreException extends Exception {
	/**
	 * Generated serial version ID
	 */
	private static final long serialVersionUID = -6597721123663875455L;

	public DaxploreException(String message){
		super(message);
	}
	
	public DaxploreException(String message, Throwable cause){
		super(message, cause);
	}
}
