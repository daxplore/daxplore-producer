/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.event;

public class InfoMessageEvent {
	private String userMessage;
	private String logMessage;
	
	public InfoMessageEvent(String userMessage) {
		this.userMessage = userMessage;
	}
	
	public InfoMessageEvent(String userMessage, String logMessage) {
		this.userMessage = userMessage;
		this.logMessage = logMessage;
	}
	
	public String getUserMessage() {
		return userMessage;
	}
	
	public boolean hasLogMessage() {
		return logMessage != null;
	}
	
	public String getLogMessage() {
		return logMessage;
	}
}
