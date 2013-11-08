package org.daxplore.producer.gui.event;

public class ErrorMessageEvent {
	private String userMessage;
	private Exception cause;
	
	public ErrorMessageEvent(String userMessage) {
		this.userMessage = userMessage;
	}
	
	public ErrorMessageEvent(Exception cause) {
		this.cause = cause;
	}
	
	public ErrorMessageEvent(String userMessage, Exception cause) {
		this.userMessage = userMessage;
		this.cause = cause;
	}
	
	public String getUserMessage() {
		if(userMessage != null) {
			return userMessage;
		}
		return cause.getMessage();
	}
	
	public Exception getCause() {
		return cause;
	}
}
