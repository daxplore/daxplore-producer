package org.daxplore.producer.cli;

import java.io.File;
import java.util.Locale;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.metadata.MetaData;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters
public class ConsolidateCommand {

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
			MetaData metadata = dax.getMetaData();
			metadata.consolidateScales(locale);
		} catch (DaxploreException e) {
			System.out.println("Consolidate command failed");
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
