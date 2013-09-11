package org.daxplore.producer.daxplorelib.raw;

@SuppressWarnings("serial")
public class ImportSPSSException extends Exception {
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