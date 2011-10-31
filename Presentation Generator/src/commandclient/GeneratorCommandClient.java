package commandclient;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class GeneratorCommandClient {
	public static void main(String[] args){
		Options options = createOptions();
		CommandLineParser parser = new GnuParser();
		try {
			CommandLine commandLine = parser.parse(options, args);
			if(commandLine.hasOption('t')){
				System.out.println("TRANSFER!");
			}else{
				System.out.println("derpa!");
			}
		} catch (ParseException e) {
			System.out.println("Unable to parse the command line arguments.");
			System.out.println(e.getMessage() + "\n");
			printHelp(options);
			return;
		}
	}
	
	protected static Options createOptions(){
		Options options = new Options();
		options.addOption("h", "help", false, "print this help message");
		options.addOption("t", "transfer-data", false, "transfer data from an spss-file to the project database");
		return options;
	}
	
	protected static void printHelp(Options options){
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp("Generator Command Client", options, true);
	}
	
}
