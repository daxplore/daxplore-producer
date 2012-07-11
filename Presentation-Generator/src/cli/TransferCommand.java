package cli;

import java.io.File;
import java.util.Locale;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import daxplorelib.DaxploreException;
import daxplorelib.DaxploreFile;
import daxplorelib.metadata.MetaData;

@Parameters
public class TransferCommand {
	
	@Parameter(names = "--locale", description = "Locale to export", converter = LocaleConverter.class)
	public Locale locale = new Locale("sv");
	
	public void run(File file) {
		if(!file.exists()) {
			System.out.println("File does not exist");
			return;
		} else if(!file.canRead() || !file.canWrite() ){
			System.out.println("File is not readwritable");
			return;
		}
		DaxploreFile dax;
		try {
			dax = new DaxploreFile(file, false);
		} catch (DaxploreException e) {
			System.out.println("Could not open daxplorefile (not a daxplorefile?)");
			System.out.println(e.getMessage());
			Exception e2 = e.getOriginalException();
			if(e2 != null) {
				e2.printStackTrace();
			}
			e.printStackTrace();
			return;
		}
		MetaData metadata;
		try {
			metadata = dax.getMetaData();
		} catch (DaxploreException e) {
			System.out.println("Could not get metadata");
			System.out.println(e.getMessage());
			Exception e2 = e.getOriginalException();
			if(e2 != null) {
				e2.printStackTrace();
			}
			e.printStackTrace();
			return;
		}
		try {
			metadata.importFromRaw(dax, locale);
		} catch (DaxploreException e) {
			System.out.println("Could not transfer metadata");
			System.out.println(e.getMessage());
			Exception e2 = e.getOriginalException();
			if(e2 != null) {
				e2.printStackTrace();
			}
			e.printStackTrace();
			return;
		}
		
		try {
			dax.close();
		} catch (DaxploreException e) {
			System.out.println("Closing error");
			System.out.println(e.getMessage());
			Exception e2 = e.getOriginalException();
			if(e2 != null) {
				e2.printStackTrace();
			}
			e.printStackTrace();
			return;
		}
	}
}