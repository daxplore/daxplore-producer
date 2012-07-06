package cli;

import java.io.File;

import cli.ExportCommand.ExportStructureCommand;
import cli.ExportCommand.ExportTextsCommand;
import cli.ImportCommand.ImportMetaStructureCommand;
import cli.ImportCommand.ImportMetaTextsCommand;
import cli.ImportCommand.ImportSpssCommand;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

public class DaxploreCLI {

	@Parameter(names={"--file","-f"}, description = "Daxplore file", converter = FileConverter.class, required = true)
	static File file;
	
	public static void main(String[] args) {
		//DaxploreCommandClient main = new DaxploreCommandClient();

		JCommander jc = new JCommander();
		jc.setProgramName("daxplore");
		
		ImportCommand importCommand = new ImportCommand();
		JCommander jc_import = addCommand(jc, "import", importCommand);
		ImportSpssCommand importSpssCommand = importCommand.getSpssCommand();
		addCommand(jc_import, "spss", importSpssCommand);
		ImportMetaStructureCommand importMetaStructureCommand = importCommand.getMetaStructureCommand();
		addCommand(jc_import, "structure", importMetaStructureCommand);
		ImportMetaTextsCommand importMetaTextsCommand = importCommand.getMetaTextsCommand();
		addCommand(jc_import, "texts", importMetaTextsCommand);
		
		ExportCommand exportCommand = new ExportCommand();
		JCommander jc_export = addCommand(jc, "export", exportCommand);
		ExportStructureCommand exportStructureCommand = exportCommand.getStructureCommand();
		addCommand(jc_export, "structure", exportStructureCommand);
		ExportTextsCommand exportTextsCommand = exportCommand.getTextsCommand();
		addCommand(jc_export, "texts", exportTextsCommand);
		
		
		InfoCommand infocommand = new InfoCommand();
		jc.addCommand("info", infocommand);
		
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
				importCommand.run(file);
		} else if(command.equals("info")){
			infocommand.run(file);
		} else if(command.equals("meta")){
			metacommand.run(file);
		}

	}
	
	private static JCommander addCommand(JCommander parentCommand,
	        String commandName, Object commandObject) {
	    parentCommand.addCommand(commandName, commandObject);
	    return parentCommand.getCommands().get(commandName);
	}
	
}
