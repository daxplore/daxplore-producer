/**
 * 
 */
package org.daxplore.producer.daxplorelib;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.daxplore.producer.gui.resources.GuiTexts;
import org.junit.Test;

public class DaxploreFileTest {

	@Test
	public void testCreateFile() throws IOException, DaxploreException {
		File tempFile = File.createTempFile("daxplore-file", ".dax");
		GuiTexts texts = new GuiTexts(new Locale("en"));
		try (DaxploreFile file = DaxploreFile.createWithNewFile(tempFile, texts)) {
			assertEquals(tempFile, file.getFile());
		}
	}

}
