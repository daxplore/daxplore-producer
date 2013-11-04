package org.daxplore.producer.gui.event;

public class HistoryAvailableEvent {
	
	private boolean available;
	
	public HistoryAvailableEvent(boolean available) {
		this.available = available;
	}
	
	public boolean isAvailable() {
		return available;
	}
	
}
