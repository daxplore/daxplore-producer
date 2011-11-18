package commandclient;

import java.io.File;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Import data into project")
public class ImportCommand {

	@Parameter(names = "--spss")
	public boolean spssImport = true;
	
	@Parameter(names = "--discard-old")
	public boolean discardold = false;
	
	@Parameter(names = "--create")
	public boolean create = false;
	
	@Parameter(description = "daxplore project file, data import file", converter = FileConverter.class)
	public List<File> files;
	
	public void run() {
		System.out.println("RUUUUUN");
		for(File f: files){
			System.out.println(f.getName() + (f.exists()?" exists":" nonexistant"));
		}
	}
}
