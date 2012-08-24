package cli;

import java.io.File;

import com.beust.jcommander.Parameters;

import daxplorelib.DaxploreException;
import daxplorelib.DaxploreFile;

@Parameters(commandDescription = "Create new project")
public class CreateCommand {
	public void run(File file) {
		if(file.exists()) {
			System.out.println("File already exists");
			return;
		} else {
			try {
				DaxploreFile.createWithNewFile(file);
			} catch (DaxploreException e) {
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
}
