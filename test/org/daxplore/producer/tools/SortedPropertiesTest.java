package org.daxplore.producer.tools;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.google.common.base.Charsets;

public class SortedPropertiesTest {

	@Test
	public void testSortedStore() throws IOException {
		Random rnd = new Random(0xf290157a);

		SortedProperties sp = new SortedProperties();
		List<Pair<String, String>> props = new LinkedList<>();
		for(int i=1; i<200; i++) {
			String key = new BigInteger(i, rnd).toString(36);
			String value = new BigInteger(i, rnd).toString(36);
			props.add(new Pair<>(key, value));
			sp.setProperty(key, value);
		}
		File tempFile = File.createTempFile("sorted-properties", ".props");
		try (FileOutputStream fos = new FileOutputStream(tempFile)) {
			sp.store(fos, "sorted properties");
		}
		
		Collections.sort(props, new Comparator<Pair<String, String>>() {
			@Override
			public int compare(Pair<String, String> o1, Pair<String, String> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		
		try (FileInputStream fis = new FileInputStream(tempFile);
				InputStreamReader isr = new InputStreamReader(fis, Charsets.UTF_8);
    			BufferedReader br = new BufferedReader(isr)) {
			assertEquals("#sorted properties", br.readLine());
			br.readLine(); // ignore date-comment line
			int i = 0;
			String line;
			while ((line=br.readLine())!=null) {
				Pair<String, String> pair = props.get(i);
				assertEquals(pair.getKey() + "=" + pair.getValue(), line);
				i++;
			}
		}
	}
}
