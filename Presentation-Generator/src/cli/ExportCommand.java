package cli;

import com.beust.jcommander.Parameters;

public class ExportCommand {
	
	private ExportStructureCommand structureCommand;
	private ExportTextsCommand textsCommand;
	
	public ExportStructureCommand getStructureCommand() {
		if(structureCommand == null) {
			structureCommand = new ExportStructureCommand();
		}
		return structureCommand;
	}
	
	public ExportTextsCommand getTextsCommand() {
		if(textsCommand == null) {
			textsCommand = new ExportTextsCommand();
		}
		return textsCommand;
	}
	
	@Parameters
	public class ExportStructureCommand {
		
	}

	@Parameters
	public class ExportTextsCommand {
		
	}
}
