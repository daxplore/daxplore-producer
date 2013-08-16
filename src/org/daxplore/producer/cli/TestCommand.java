package org.daxplore.producer.cli;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Locale;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.DaxploreMemoryFile;
import org.daxplore.producer.daxplorelib.metadata.MetaData;
import org.daxplore.producer.daxplorelib.metadata.MetaData.L10nFormat;

import com.beust.jcommander.Parameters;

@Parameters
public class TestCommand {

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
			dax = DaxploreFile.createFromExistingFile(file);
			System.out.println("File opened");
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
		MetaData metadata;
		try {
			DaxploreMemoryFile daxmem = dax.getInMemoryFile();
			System.out.println("In memory database created");
			
			metadata = daxmem.getMetaData();
			System.out.println("Metadata gotten");
			
			metadata.consolidateScales(new Locale("sv"));
			System.out.println("Scales consolidated");
			
			metadata.exportL10n(new OutputStreamWriter(System.out), L10nFormat.PROPERTIES, new Locale("sv"));
			
		} catch (DaxploreException e) {
			System.out.println("Could not get metadata");
			System.out.println(e.getMessage());
			Throwable e2 = e.getCause();
			if(e2 != null) {
				e2.printStackTrace();
			}
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			dax.close();
		} catch (DaxploreException e) {
			System.out.println("Closing error");
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
