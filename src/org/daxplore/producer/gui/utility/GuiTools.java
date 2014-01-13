/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.utility;

/**
 * Various help methods for the GUI are located in this class.
 * @author hkfs89
 *
 */
public class GuiTools {
	
	static boolean checkFlag = false; // activate the version check?

	/**
	 * Checks if user is running Java 7 or above. Returns true if so.
	 * @return
	 */
	public static boolean javaVersionCheck() {
		
		if (checkFlag == false)
			return true;
		
		String javaVersion = System.getProperty("java.version");
		String[] javaVersionSplit = javaVersion.split("\\.");
		int indexZero = Integer.parseInt(javaVersionSplit[0]);
		int indexOne = Integer.parseInt(javaVersionSplit[1]);

		if (!(indexZero >= 1) || (indexOne < 7))
		{
			return false;
		}
		
		return true;
	}
}
