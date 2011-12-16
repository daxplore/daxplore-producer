package commandclient;

import com.beust.jcommander.JCommander;

public class DaxploreCommandClient {

	public static void main(String[] args) {
		//DaxploreCommandClient main = new DaxploreCommandClient();
		JCommander jc = new JCommander();
		jc.setProgramName("daxplore");
		ImportCommand importcommand = new ImportCommand();
		jc.addCommand("import", importcommand);
		InfoCommand infocommand = new InfoCommand();
		jc.addCommand("info", infocommand);
		
		jc.parse(args);
		
		String command = jc.getParsedCommand();
		if(command == null){
			jc.usage();
		} else if(jc.getParsedCommand().equals("import")){
			importcommand.run();
		} else if(jc.getParsedCommand().equals("info")){
			infocommand.run();
		}
	}
}
