package commandclient;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class DaxploreCommandClient {
	/*@Parameter
	public List<String> parameters = Lists.newArrayList();*/

	@Parameter(names = { "-log", "-verbose" }, description = "Level of verbosity")
	public Integer verbose = 1;

	@Parameter(names = "-debug", description = "Debug mode", hidden = true)
	public boolean debug = false;

	public static void main(String[] args) {
		//DaxploreCommandClient main = new DaxploreCommandClient();
		JCommander jc = new JCommander();
		ImportCommand ic = new ImportCommand();
		jc.addCommand("import", ic);
		
		jc.parse(args);
		
		jc.usage();
		
		if(jc.getParsedCommand().equals("import")){
			ic.run();
		}
		//String[] argv = { "-log", "2", "-groups", "unit", "a", "b", "c" };

		/*
		System.out.println(jct.verbose.intValue() + " = 2?");
		System.out.println(jct.verbose.intValue() + " = 2?");
		System.out.println(jct.verbose.intValue() + " = 2?");
		*/
	}
}
