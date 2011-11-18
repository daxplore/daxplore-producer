package commandclient;

import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;

public class DaxploreCommandClient {
	@Parameter
	public List<String> parameters = Lists.newArrayList();

	@Parameter(names = { "-log", "-verbose" }, description = "Level of verbosity")
	public Integer verbose = 1;

	@Parameter(names = "-groups", description = "Comma-separated list of group names to be run")
	public String groups;

	@Parameter(names = "-debug", description = "Debug mode")
	public boolean debug = false;

	public static void main(String[] args) {
		DaxploreCommandClient jct = new DaxploreCommandClient();
		String[] argv = { "-log", "2", "-groups", "unit", "a", "b", "c" };
		new JCommander(jct, argv);
		 
		System.out.println(jct.verbose.intValue() + " = 2?");
		System.out.println(jct.verbose.intValue() + " = 2?");
		System.out.println(jct.verbose.intValue() + " = 2?");
		System.out.println(jct.groups);
	}
}
