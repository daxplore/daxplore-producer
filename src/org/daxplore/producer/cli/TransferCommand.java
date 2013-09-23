package org.daxplore.producer.cli;

import java.io.File;
import java.util.Locale;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.metadata.MetaData;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

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
		
		try (DaxploreFile dax = DaxploreFile.createFromExistingFile(file)) {
			
			MetaData metadata;
			try {
				metadata = dax.getMetaData();
			} catch (DaxploreException e) {
				System.out.println("Could not get metadata");
				System.out.println(e.getMessage());
				Throwable e2 = e.getCause();
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
				Throwable e2 = e.getCause();
				if(e2 != null) {
					e2.printStackTrace();
				}
				e.printStackTrace();
				return;
			}
			
		} catch (DaxploreException e) {
			System.out.println("Could not open daxplorefile (not a daxplorefile?)");
			System.out.println(e.getMessage());
			Throwable e2 = e.getCause();
			if(e2 != null) {
				e2.printStackTrace();
			}
			e.printStackTrace();
			return;
		}
	}
}
