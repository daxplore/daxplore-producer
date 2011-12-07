package daxplorelib;

public class DaxploreException extends Exception {

	final Exception originalException;
	/**
	 * 
	 */
	private static final long serialVersionUID = -6597721123663875455L;

	public DaxploreException(String message){
		this(message, null);
	}
	
	public DaxploreException(String message, Exception originalException){
		super(message);
		this.originalException = originalException;
	}
	
	public Exception getOriginalException(){
		return originalException;
	}
}
