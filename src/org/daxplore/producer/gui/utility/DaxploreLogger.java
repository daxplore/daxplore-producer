/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.utility;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//TODO fix broken logger
public class DaxploreLogger {

	  static private FileHandler fileTxt;
	  static private SimpleFormatter formatterTxt;

	  static public void setup() throws IOException {

		  //TODO logger settings are lost in OpenJDK (see FindBugs)
	    // Get the global logger to configure it
	    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	    logger.setLevel(Level.CONFIG);
	   // fileTxt = new FileHandler("Logging.txt");

	    // Create txt Formatter
	    formatterTxt = new SimpleFormatter();
	    //fileTxt.setFormatter(formatterTxt);
	    //logger.addHandler(fileTxt);
	  }
	} 
