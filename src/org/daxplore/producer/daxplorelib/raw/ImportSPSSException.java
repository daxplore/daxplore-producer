package org.daxplore.producer.daxplorelib.raw;

public class ImportSPSSException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4015862126487945499L;
	private Exception originalException;
	
	public ImportSPSSException(String message){
		super(message);
	}
	
	public ImportSPSSException(String message, Exception originalException){
		super(message);
		this.originalException = originalException;
	}
	
	public Exception getOriginalException(){
		return originalException;
	}
}