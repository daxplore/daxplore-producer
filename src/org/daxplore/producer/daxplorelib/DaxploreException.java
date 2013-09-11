package org.daxplore.producer.daxplorelib;

@SuppressWarnings("serial")
public class DaxploreException extends Exception {
	public DaxploreException(String message){
		super(message);
	}
	
	public DaxploreException(String message, Throwable cause){
		super(message, cause);
	}
}
