package org.daxplore.producer.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.metadata.MetaData;
import org.daxplore.producer.daxplorelib.metadata.MetaData.L10nFormat;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

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

		@Parameter(description = "file to import", arity = 1, converter = FileConverter.class, required = true)
		public List<File> files;
		
		public void run(File file) {
			DaxploreFile dax;
			Writer w;
			try {
				dax = DaxploreFile.createFromExistingFile(file);
			} catch (DaxploreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			File outfile = files.get(0);
			if(outfile.exists()) {
				System.out.println("File " + outfile.getName() + " already exists");
				return;
			} else {
				try {
					outfile.createNewFile();
					if(!outfile.canWrite()) {
						System.out.println("Can't write to file: " + outfile.getName());
						return;
					} 
				} catch (IOException e) {
					System.out.println("Couln't create file: " + outfile.getName());
					e.printStackTrace();
					return;
				}
				FileWriter fw = null;
				try {
					fw = new FileWriter(outfile);
				} catch (IOException e) {
					System.out.println("Could not open file for writing");
					e.printStackTrace();
					return;
				}
				w = new BufferedWriter(fw);
			}
			MetaData metadata;
			try {
				metadata = dax.getMetaData();
			} catch (DaxploreException e) {
				try {
					w.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//TODO handle exception
				System.out.println("Could not get metadata");
				e.printStackTrace();
				return;
			}
			
			try {
				metadata.exportStructure(w);
			} catch (DaxploreException e) {
				//TODO handle exception
				System.out.println("Error exporting structure");
				e.printStackTrace();
				return;
			} catch (IOException e) {
				//TODO handle exception
				System.out.println("Error exporting structure");
				e.printStackTrace();
				return;
			} catch (SQLException e) {
				//TODO handle exception
				System.out.println("Error exporting structure");
				e.printStackTrace();
				return;
			}
		}
		
	}

	@Parameters
	public class ExportTextsCommand {
		
		@Parameter(names = "--locale", description = "Locale to export", converter = LocaleConverter.class)
		public Locale locale = new Locale("sv");
		
		@Parameter(description = "file to import", arity = 1, converter = FileConverter.class, required = true)
		public List<File> files;
		
		public void run(File file) {
			DaxploreFile dax;
			Writer w;
			try {
				dax = DaxploreFile.createFromExistingFile(file);
			} catch (DaxploreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			File outfile = files.get(0);
			if(outfile.exists()) {
				System.out.println("File " + outfile.getName() + " already exists");
				return;
			} else {
				try {
					outfile.createNewFile();
					if(!outfile.canWrite()) {
						System.out.println("Can't write to file: " + outfile.getName());
						return;
					} 
				} catch (IOException e) {
					System.out.println("Couln't create file: " + outfile.getName());
					e.printStackTrace();
					return;
				}
				FileWriter fw = null;
				try {
					fw = new FileWriter(outfile);
				} catch (IOException e) {
					System.out.println("Could not open file for writing");
					e.printStackTrace();
					return;
				}
				w = new BufferedWriter(fw);
			}
			MetaData metadata;
			try {
				metadata = dax.getMetaData();
			} catch (DaxploreException e) {
				//TODO handle exception
				try {
					w.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("Could not get metadata");
				e.printStackTrace();
				return;
			}
			
			try {
				metadata.exportL10n(w, L10nFormat.PROPERTIES, locale);
			} catch (DaxploreException e) {
				//TODO handle exception
				System.out.println("Error exporting texts");
				e.printStackTrace();
				return;
			} catch (IOException e) {
				//TODO handle exception
				System.out.println("Error exporting texts");
				e.printStackTrace();
				return;
			}
			try {
				w.flush();
			} catch (IOException e) {
				//TODO handle exception
				System.out.println("Huh!?!");
				e.printStackTrace();
			}
			
		}
		
	}
}