package commandclient;

import java.io.File;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

public class DaxploreCommandClient {

	@Parameter(names={"--file","-f"}, description = "Daxplore file", converter = FileConverter.class, required = true)
	static File file;
	
	public static void main(String[] args) {
		//DaxploreCommandClient main = new DaxploreCommandClient();

		JCommander jc = new JCommander();
		jc.setProgramName("daxplore");
		ImportCommand importcommand = new ImportCommand();
		jc.addCommand("import", importcommand);
		InfoCommand infocommand = new InfoCommand();
		jc.addCommand("info", infocommand);
		MetaCommand metacommand = new MetaCommand();
		jc.addCommand("meta", metacommand);
	
		try{
			jc.parse(args);
		} catch (ParameterException e){
			System.out.println(e.getMessage());
			/*System.out.println("Exception caught");
			System.out.println(jc.getParsedCommand());
			e.printStackTrace();*/
			System.exit(-1);
			return;
		} catch (Exception e) {
			System.out.println("Unknown exception");
			e.printStackTrace();
			return;
		}
		String command = jc.getParsedCommand();
		
		if(file.isDirectory()){
			System.out.println("Project file is a directory");
			return;
		}
		
		if(command == null){
			jc.usage();
		} else if(command.equals("import")){
				importcommand.run(file);
		} else if(command.equals("info")){
			infocommand.run(file);
		} else if(command.equals("meta")){
			metacommand.run(file);
		}

	}
}
