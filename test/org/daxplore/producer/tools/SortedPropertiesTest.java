package org.daxplore.producer.tools;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class SortedPropertiesTest {

	@Test
	public void testSortedStore() throws IOException {
		Random rnd = new Random(0xf290157a);

		SortedProperties sp = new SortedProperties();
		List<Pair<String, String>> props = new LinkedList<Pair<String, String>>();
		for(int i=1; i<200; i++) {
			String key = new BigInteger(i, rnd).toString(36);
			String value = new BigInteger(i, rnd).toString(36);
			props.add(new Pair<String, String>(key, value));
			sp.setProperty(key, value);
		}
		File tempFile = File.createTempFile("sorted-properties", ".props");
		FileOutputStream fos = new FileOutputStream(tempFile);
		sp.store(fos, "sorted properties");
		fos.close();
		
		Collections.sort(props, new Comparator<Pair<String, String>>() {
			@Override
			public int compare(Pair<String, String> o1, Pair<String, String> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		
		BufferedReader br = new BufferedReader(new FileReader(tempFile));
		assertEquals("#sorted properties", br.readLine());
		br.readLine(); // ignore date-comment line
		int i = 0;
		String line;
		while ((line=br.readLine())!=null) {
			Pair<String, String> pair = props.get(i);
			assertEquals(pair.getKey() + "=" + pair.getValue(), line);
			i++;
		}
		br.close();
	}
}
