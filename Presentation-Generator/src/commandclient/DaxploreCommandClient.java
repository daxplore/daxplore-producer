package commandclient;

import com.beust.jcommander.JCommander;

public class DaxploreCommandClient {

	public static void main(String[] args) {
		//DaxploreCommandClient main = new DaxploreCommandClient();
		JCommander jc = new JCommander();
		jc.setProgramName("daxplore");
		ImportCommand ic = new ImportCommand();
		jc.addCommand("import", ic);
		
		jc.parse(args);
		
		String command = jc.getParsedCommand();
		if(command == null){
			jc.usage();
		} else if(jc.getParsedCommand().equals("import")){
			try {
				ic.run();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
