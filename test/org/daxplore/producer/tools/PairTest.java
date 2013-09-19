package org.daxplore.producer.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class PairTest {
	Pair<String, Integer> pair1;
	Pair<String, Integer> pair2;
	Pair<String, Integer> pair3;
	Pair<String, Integer> pair4;

	Pair<Pair<String, Integer>, Pair<String, Integer>> pp1;
	Pair<Pair<String, Integer>, Pair<String, Integer>> pp2;
	Pair<Pair<String, Integer>, Pair<String, Integer>> pp3;
	Pair<Pair<String, Integer>, Pair<String, Integer>> pp4;

	@Before
	public void setUp() {
		pair1 = new Pair<String, Integer>("foo", 1);
		pair2 = new Pair<String, Integer>("foo", 1);
		pair3 = new Pair<String, Integer>("bar", 1);
		pair4 = new Pair<String, Integer>("bar", Integer.MIN_VALUE);

		pp1 = new Pair<Pair<String, Integer>, Pair<String, Integer>>(pair1, pair2);
		pp2 = new Pair<Pair<String, Integer>, Pair<String, Integer>>(pair1, pair2);
		pp3 = new Pair<Pair<String, Integer>, Pair<String, Integer>>(pair1, pair3);
		pp4 = new Pair<Pair<String, Integer>, Pair<String, Integer>>(pair4, pair1);
	}

	@Test
	public void testConstructor() {
		try {
			new Pair<Object, Object>(null, new Object());
			fail();
		} catch (NullPointerException e) {
			// Success
		}

		try {
			new Pair<Object, Object>(new Object(), null);
			fail();
		} catch (NullPointerException e) {
			// Success
		}
	}

	@Test
	public void testToString() {
		assertEquals("Key: foo\tValue: 1", pair1.toString());
		assertEquals("Key: foo\tValue: 1", pair2.toString());
		assertEquals("Key: bar\tValue: 1", pair3.toString());
		assertEquals("Key: bar\tValue: " + Integer.MIN_VALUE, pair4.toString());

		assertEquals("Key: " + pair1.toString() + "\tValue: " + pair2.toString(), pp1.toString());
		assertEquals("Key: " + pair4.toString() + "\tValue: " + pair1.toString(), pp4.toString());
	}
}
