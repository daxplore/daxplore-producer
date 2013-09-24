package org.daxplore.producer.cli;

import java.io.File;
import java.io.IOException;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;

import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Create new project")
public class CreateCommand {
	public void run(File file) {
		if(file.exists()) {
			System.out.println("File already exists");
			return;
		}
		try (DaxploreFile created = DaxploreFile.createWithNewFile(file)) {
			
		} catch (DaxploreException | IOException e) {
			System.out.println("Couldn't create file");
			Throwable e2 = e.getCause();
			if(e2 != null) {
				e2.printStackTrace();
			}
			e.printStackTrace();
			return;
		}
	}
}
