/**
 * 
 */
package org.daxplore.producer.daxplorelib;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class DaxploreFileTest {

	@Test
	public void testCreateFile() throws IOException, DaxploreException {
		File tempFile = File.createTempFile("daxplore-file", ".dax");
		try (DaxploreFile file = DaxploreFile.createWithNewFile(tempFile)) {
			assertEquals(tempFile, file.getFile());
		}
	}

}
