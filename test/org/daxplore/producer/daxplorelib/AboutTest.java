/**
 * 
 */
package org.daxplore.producer.daxplorelib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AboutTest {
	DaxploreFile file;
	About about;
	long creationTime;
	
	@Before
	public void setUp() throws DaxploreException, IOException {
		File tempFile = File.createTempFile("daxplore-file", ".dax");
		file = DaxploreFile.createWithNewFile(tempFile);
		creationTime = new Date().getTime();
		about = file.getAbout();
	}

	@Test
	public void testFiletypeVersion() {
		assertEquals(DaxploreProperties.filetypeversionmajor, about.filetypeversionmajor);
		assertEquals(DaxploreProperties.filetypeversionminor, about.filetypeversionminor);
		
		//TODO test version for a loaded file
	}

	@Test
	public void testCreationDate() {
		long creation = about.getCreationDate().getTime();
		
		//Assume that file creation takes less that 10s:
		assertTrue(creationTime - creation >= 0);
		assertTrue(creationTime - creation <= 10*1000);
		
		//TODO test creation date for a loaded file
	}

	@Test
	public void testLastUpdate() {
		long now = new Date().getTime();
		long update = about.getLastUpdate().getTime();
		
		//Assume that file creation takes less that 10s:
		assertTrue(now - update >= 0);
		assertTrue(now - update <= 10*1000);
		
		//TODO make changes and check that the date has been updated
	}
	
	@After
	public void tearDown() throws DaxploreException {
		file.close();
	}

}
