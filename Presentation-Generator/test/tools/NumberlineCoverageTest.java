package tools;

import static org.junit.Assert.*;

import org.junit.Test;

import tools.NumberlineCoverage.NumberlineCoverageException;

public class NumberlineCoverageTest {

	@Test
	public void testToAndFromString() throws NumberlineCoverageException {
		NumberlineCoverage c1 = new NumberlineCoverage(-4, true, 19.5, false);
		c1.addInterval(100, true, Double.POSITIVE_INFINITY, false);
		NumberlineCoverage c2 = new NumberlineCoverage(c1.toString());
		
		assertTrue(c2.contains(-4));
		assertTrue(c2.contains(10));
		assertTrue(c2.contains(100));
		assertTrue(c2.contains(Double.MAX_VALUE));
		assertTrue(c2.contains(0));
		
		assertFalse(c2.contains(Math.nextAfter(100, Double.NEGATIVE_INFINITY)));
		assertFalse(c2.contains(Double.POSITIVE_INFINITY));
		assertFalse(c2.contains(19.5));
		assertFalse(c2.contains(-10));
		
	}
	
	/*
	 	//System.out.println(Double.valueOf("-inf"));
		//System.out.println(Double.valueOf("-Inf"));
		//System.out.println(Double.valueOf("-infinity"));
		/*System.out.println(Double.valueOf("-Infinity"));
		
		double d = Double.parseDouble("-Infinity");
		String a ="look " + d;
		System.out.println(a);
		
		NumberlineCoverage mi = new NumberlineCoverage("[-Infinity, -1)U[0,5]");
		System.out.println(mi);
		NumberlineCoverage mi2 = new NumberlineCoverage("[-3, -1]U[2,7]");
		System.out.println(mi2 + "\n");
		mi.unionWith(mi2);
		System.out.println(mi+ "\n");
		mi.addInterval(-2, true, 0.23145, false);
		System.out.println(mi + "\n");
		
		mi.removeInterval(Double.NEGATIVE_INFINITY, true, -8.123, true);
		System.out.println(mi + "\n");
		
		mi.removeInterval(6.0, true, 7.0, true);
		
		System.out.println(mi+ "\n");
		
		mi.addInterval(30, true, 35, true);
		
		mi.removeInterval(29, true, 35, true);
		
		System.out.println(mi + "\n ");
	 */

}
