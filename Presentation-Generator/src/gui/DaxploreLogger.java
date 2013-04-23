package gui;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DaxploreLogger {

	  static private FileHandler fileTxt;
	  static private SimpleFormatter formatterTxt;

	  static public void setup() throws IOException {

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
