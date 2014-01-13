/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.cli;

import java.io.File;

import org.daxplore.producer.cli.ExportCommand.ExportStructureCommand;
import org.daxplore.producer.cli.ExportCommand.ExportTextsCommand;
import org.daxplore.producer.cli.ImportCommand.ImportMetaStructureCommand;
import org.daxplore.producer.cli.ImportCommand.ImportMetaTextsCommand;
import org.daxplore.producer.cli.ImportCommand.ImportSpssCommand;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

public class DaxploreCLI {

	@Parameter(names={"--file","-f"}, description = "Daxplore file", converter = FileConverter.class, required = true)
	static File file;
	
	public static void main(String[] args) {
		//DaxploreCommandClient main = new DaxploreCommandClient();
		String[] realargs = new String[args.length -1];
		
		JCommander jc = new JCommander();
		jc.setProgramName("daxplore");
		
		ImportCommand importCommand = new ImportCommand();
		JCommander jc_import = addCommand(jc, "import", importCommand);
		ImportSpssCommand importSpssCommand = importCommand.getSpssCommand();
		addCommand(jc_import, "spss", importSpssCommand);
		ImportMetaStructureCommand importMetaStructureCommand = importCommand.getStructureCommand();
		addCommand(jc_import, "structure", importMetaStructureCommand);
		ImportMetaTextsCommand importMetaTextsCommand = importCommand.getTextsCommand();
		addCommand(jc_import, "texts", importMetaTextsCommand);
		
		ExportCommand exportCommand = new ExportCommand();
		JCommander jc_export = addCommand(jc, "export", exportCommand);
		ExportStructureCommand exportStructureCommand = exportCommand.getStructureCommand();
		addCommand(jc_export, "structure", exportStructureCommand);
		ExportTextsCommand exportTextsCommand = exportCommand.getTextsCommand();
		addCommand(jc_export, "texts", exportTextsCommand);
		
		InfoCommand infoCommand = new InfoCommand();
		addCommand(jc, "info", infoCommand);
		
		CreateCommand createCommand = new CreateCommand();
		addCommand(jc, "create", createCommand);
		
		TransferCommand transferCommand = new TransferCommand();
		addCommand(jc, "transfer", transferCommand);
		
		ConsolidateCommand consolidateCommand = new ConsolidateCommand();
		addCommand(jc, "scalemerge", consolidateCommand);
		
		if(args.length > 1	) {
			file = new File(args[0]);
			if(file.isDirectory()) {
				System.out.println("Project file is a directory");
				jc.usage();
			}
			for(int i = 1; i < args.length; i++){
				realargs[i-1] = args[i];
			}
		} else {
			System.out.println("Not enough arguments");
			jc.usage();
		}
		
		
		try{
			jc.parse(realargs);
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
		//System.out.println("Command was: " + command);
		
		if(command == null){
			jc.usage();
		} else if(command.equals("import")){
			JCommander jc2 = jc.getCommands().get(command);
			String command2 = jc2.getParsedCommand();
			//System.out.println("Command2 was: " + command2);
			if(command2.equals("spss")) {
				importCommand.getSpssCommand().run(file);
			} else if(command2.equals("texts")) {
				importCommand.getTextsCommand().run(file);
			} else if(command2.equals("structure")) {
				importCommand.getStructureCommand().run(file);
			} else {
				System.out.println("Unrecognized command");
			}
		} else if(command.equals("export")) {
			JCommander jc2 = jc.getCommands().get(command);
			String command2 = jc2.getParsedCommand();
			//System.out.println("Command2 was: " + command2);
			if(command2.equals("texts")) {
				exportCommand.getTextsCommand().run(file);
			} else if(command2.equals("structure")) {
				exportCommand.getStructureCommand().run(file);
			} else {
				System.out.println("Unrecognized command");
			}
		} else if(command.equals("info")) {
			infoCommand.run(file);
		} else if(command.equals("create")) {
			createCommand.run(file);
		} else if(command.equals("transfer")) {
			transferCommand.run(file);
		} else if(command.equals("scalemerge")) {
			consolidateCommand.run(file);
		}

	}
	
	private static JCommander addCommand(JCommander parentCommand,
	        String commandName, Object commandObject) {
	    parentCommand.addCommand(commandName, commandObject);
	    return parentCommand.getCommands().get(commandName);
	}
	
}
