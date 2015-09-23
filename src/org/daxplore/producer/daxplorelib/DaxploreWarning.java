/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib;

@SuppressWarnings("serial")
public class DaxploreWarning extends Exception {
	public DaxploreWarning(String message){
		super(message);
	}
	
	public DaxploreWarning(String message, Throwable cause){
		super(message, cause);
	}
}
