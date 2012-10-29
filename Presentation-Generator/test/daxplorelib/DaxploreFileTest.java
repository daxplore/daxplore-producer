/**
 * 
 */
package daxplorelib;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class DaxploreFileTest {

	@Test
	public void testCreateFile() throws IOException, DaxploreException {
		File tempFile = File.createTempFile("daxplore-file", ".dax");
		DaxploreFile file = DaxploreFile.createWithNewFile(tempFile);
		assertEquals(tempFile, file.getFile());
		file.close();
	}

}