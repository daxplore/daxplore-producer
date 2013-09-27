package org.daxplore.producer.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Locale;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.metadata.MetaData;
import org.daxplore.producer.daxplorelib.metadata.MetaData.L10nFormat;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;
import com.google.common.base.Charsets;

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
			try (DaxploreFile dax = DaxploreFile.createFromExistingFile(file)) {
				File outfile = files.get(0);
				if(outfile.exists()) {
					System.out.println("File " + outfile.getName() + " already exists");
					return;
				}
				
				try {
					if(!outfile.createNewFile() || !outfile.canWrite()) {
						System.out.println("Can't write to file: " + outfile.getName());
						return;
					} 
				} catch (IOException e) {
					System.out.println("Couln't create file: " + outfile.getName());
					e.printStackTrace();
					return;
				}

				try (FileWriter fw = new FileWriter(outfile);
						Writer w = new BufferedWriter(fw)) {
					MetaData metadata = dax.getMetaData();
					metadata.exportStructure(w);
					w.flush();
				} catch (DaxploreException | IOException e) {
					//TODO handle exception
					System.out.println("Error exporting structure");
					e.printStackTrace();
				}
			} catch (DaxploreException | IOException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
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
			
			File outfile = files.get(0);
			if(outfile.exists()) {
				System.out.println("File " + outfile.getName() + " already exists");
				return;
			}
			try {
				if(!outfile.createNewFile() || !outfile.canWrite()) {
					System.out.println("Can't write to file: " + outfile.getName());
					return;
				} 
			} catch (IOException e) {
				//TODO handle exception
				System.out.println("Couln't create file: " + outfile.getName());
				e.printStackTrace();
				return;
			}
			try (FileOutputStream fos = new FileOutputStream(outfile);
					OutputStreamWriter osw = new OutputStreamWriter(fos, Charsets.UTF_8);
					Writer w = new BufferedWriter(osw);
					DaxploreFile dax = DaxploreFile.createFromExistingFile(file)) {
				MetaData metadata = dax.getMetaData();
				metadata.exportL10n(w, L10nFormat.PROPERTIES, locale);
				w.flush();
			} catch (DaxploreException | IOException e) {
				//TODO handle exception
				System.out.println("Error exporting texts");
				e.printStackTrace();
			}
		}
	}
}
