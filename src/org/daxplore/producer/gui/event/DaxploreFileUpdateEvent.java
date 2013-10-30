package org.daxplore.producer.gui.event;

import org.daxplore.producer.daxplorelib.DaxploreFile;

public class DaxploreFileUpdateEvent {
	private DaxploreFile daxploreFile;
	
	public DaxploreFileUpdateEvent(DaxploreFile daxploreFile) {
		this.daxploreFile = daxploreFile;
	}
	
	public DaxploreFile getDaxploreFile() {
		return daxploreFile;
	}
}
