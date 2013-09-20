package org.daxplore.producer.tools;

import java.util.LinkedList;
import java.util.List;


/**
 * @author Axel Winkler, Daniel Dunér
 */
public class NumberlineCoverage {
	
	protected class Interval implements Cloneable{
		double low, high;
		boolean lowInclusive, highInclusive;
		
		public Interval(double low, boolean lowInclusive, double high, boolean highInclusive) throws NumberlineCoverageException {
			if(high == low & !lowInclusive & ! highInclusive) {
				throw new NumberlineCoverageException("Nonexistant interval");
			} else if(high >= low) {
				this.low = low; this.high = high; this.lowInclusive = lowInclusive; this.highInclusive = highInclusive;
			} else {
				this.low = high; this.high = low; this.lowInclusive = highInclusive; this.highInclusive = lowInclusive;				
			}
		}
		
		public Interval(String inter) throws NumberlineCoverageException {
			try { // really an if-statement: if(inter is double)
				Double value = Double.valueOf(inter);
				low = value;high = value; 
				lowInclusive = true; highInclusive = true;
			} catch (NumberFormatException e) {
				if(inter == null || inter.length() < 5) {
					throw new NumberlineCoverageException("Malformed interval string: " + inter);
				}
				switch(inter.charAt(0)) {
				case '[':
					lowInclusive = true;
					break;
				case '(':
					lowInclusive = false;
					break;
				default:
					throw new NumberlineCoverageException("Malformed interval string: " + inter);
				}
				
				switch(inter.charAt(inter.length()-1)) {
				case ']':
					highInclusive = true;
					break;
				case '[':
					highInclusive = false;
					break;
				default:
					throw new NumberlineCoverageException("Malformed interval string: " + inter);
				}
				
				String interTrimmed = inter.substring(1, inter.length()-1);
				String[] numbers = interTrimmed.split(",");
				if(numbers.length != 2) {
					throw new NumberlineCoverageException("Malformed interval string: " + inter);
				}
				
				try {
					low = Double.parseDouble(numbers[0]);
					high = Double.parseDouble(numbers[1]);
				} catch (NumberFormatException e2) {
					throw new NumberlineCoverageException("Malformed interval string: " + inter);
				}
			}
		}
		
		public boolean contains(double value) {
			return (value > low  | (lowInclusive  & value == low )) &
				   (value < high | (highInclusive & value == high)); 
		}
		
		public boolean joinable(Interval otherInterval) {
			return  (otherInterval.high > low | (otherInterval.high == low & (otherInterval.highInclusive | lowInclusive))) &
					(high > otherInterval.low | (high == otherInterval.low & (highInclusive | otherInterval.lowInclusive)));
		}
		
		@Override
		public Interval clone() {
			try {
				return (Interval)super.clone();
			} catch (CloneNotSupportedException e) {
				throw new AssertionError();
			}
		}
		
		@Override
		public String toString() {
			return low==high?
					low + "":
					(lowInclusive ? "[": "(") + 
					low + "," + high + 
					(highInclusive ? "]" : ")");
		}
	}
	
	@SuppressWarnings("serial")
	public class NumberlineCoverageException extends Exception {

		public NumberlineCoverageException(String string) {
			super(string);
		}
		
	}
	
	List<Interval> intervals = new LinkedList<>();
	
	public NumberlineCoverage(String intervalString) throws NumberlineCoverageException {
		if (intervalString == null || intervalString.length() == 0) {
			return;
		}
		String[] interStrings = intervalString.split("U");
		for(String inter: interStrings) {
			intervals.add(new Interval(inter));
		}
	}
	
	public NumberlineCoverage(double a, boolean aInclusive, double b, boolean bInclusive) throws NumberlineCoverageException {
		intervals.add(new Interval(a, aInclusive, b, bInclusive));
	}
	
	public NumberlineCoverage(double number) {
		addNumber(number);
	}
	
	public NumberlineCoverage() {}
	
	public void addNumber(double a) {
		try {
			addInterval(a, true, a, true);
		} catch (NumberlineCoverageException e) {
			throw new AssertionError();
		}
	}
	
	protected void addInterval(Interval interval) {
		try {
			addInterval(interval.low, interval.lowInclusive, interval.high, interval.highInclusive);
		} catch (NumberlineCoverageException e) {
			throw new AssertionError("Malformed intervals should not exist to begin with");
		}
	}
	
	public void addInterval(double a, boolean aInclusive, double b, boolean bInclusive) throws NumberlineCoverageException {
		Interval newInterval = new Interval(a, aInclusive, b, bInclusive);
		List<Interval> interlist = new LinkedList<>();
		
		for(int i = 0; i < intervals.size(); i++) {
			Interval oldInterval = intervals.get(i);
			if(newInterval.high < oldInterval.low | 
					(newInterval.high == oldInterval.low & 
					(newInterval.highInclusive | oldInterval.lowInclusive))) { // if newInterval is before oldInterval (with consideration to edges)
				interlist.add(newInterval);
				for(; i < intervals.size(); i++) {
					interlist.add(intervals.get(i));
				}
			} else if(newInterval.low > oldInterval.high | 
					(newInterval.low == oldInterval.high &
					(newInterval.lowInclusive | oldInterval.highInclusive))) {
				interlist.add(oldInterval);
				if(i == intervals.size() -1) {
					interlist.add(newInterval);
				}
			} else {
				newInterval = combine(oldInterval, newInterval);
				if(i == intervals.size() -1) {
					interlist.add(newInterval);
				}
			}
		}

		if(intervals.size() == 0) {
			interlist.add(newInterval);
		}
		
		intervals = interlist;
	}
	
	protected void removeInterval(Interval interval) {
		removeInterval(interval.low, interval.lowInclusive, interval.high, interval.highInclusive);
	}
	
	public void removeInterval(double a, boolean aInclusive, double b, boolean bInclusive) {
		Interval newInterval;
		try {
			newInterval = new Interval(a, aInclusive, b, bInclusive);
		} catch (NumberlineCoverageException e) {
			return;
		}
		List<Interval> interlist = new LinkedList<>();
		
		try {
			for(int i = 0; i < intervals.size(); i++) {
				Interval oldInterval = intervals.get(i);
				boolean lowContained = oldInterval.contains(newInterval.low);
				boolean lowEquals = oldInterval.low == newInterval.low & (newInterval.lowInclusive | !oldInterval.lowInclusive);
				boolean highContained = oldInterval.contains(newInterval.high);
				boolean highEquals = oldInterval.high == newInterval.high & (newInterval.highInclusive | !oldInterval.highInclusive);
				if(newInterval.high < oldInterval.low | 
						(newInterval.high == oldInterval.low & 
						!(newInterval.highInclusive & oldInterval.lowInclusive))) { // if newInterval is before oldInterval (with consideration to edges)
					for(; i < intervals.size(); i++) {
						interlist.add(intervals.get(i));
					}
				} else if(newInterval.low > oldInterval.high | 
						(newInterval.low == oldInterval.high &
						!(newInterval.lowInclusive & oldInterval.highInclusive))) {
					interlist.add(oldInterval);
				} else if(!lowContained & highContained) {
					if(!highEquals) {
						interlist.add(new Interval(newInterval.high, !newInterval.highInclusive, oldInterval.high, oldInterval.highInclusive));
					}
				} else if(lowContained & !highContained) { 
					if(!lowEquals) {
						interlist.add(new Interval(oldInterval.low, oldInterval.lowInclusive, newInterval.low, !newInterval.lowInclusive));
					}
				} else if(lowContained & highContained) {
					if(!lowEquals) {
						interlist.add(new Interval(oldInterval.low, oldInterval.lowInclusive, newInterval.low, !newInterval.lowInclusive));
					}
					if(!highEquals) {
						interlist.add(new Interval(newInterval.high, !newInterval.highInclusive, oldInterval.high, oldInterval.highInclusive));
					}
				} // else if oldInterval is contained in new, remove old.
			}
		}catch (NumberlineCoverageException e) {
			throw new AssertionError();
		}
		
		intervals = interlist;
	}
	
	protected Interval combine(Interval interval1, Interval interval2) {
		boolean lowIncluded = interval1.contains(interval2.low);
		boolean highIncluded = interval1.contains(interval2.high);
		if(lowIncluded & highIncluded) {
			return interval1;
		} else if(lowIncluded) {
			try {
				return new Interval(interval1.low, interval1.lowInclusive, interval2.high, interval2.highInclusive);
			} catch (NumberlineCoverageException e) {
				throw new AssertionError();
			}
		} else if(highIncluded) {
			try {
				return new Interval(interval2.low, interval2.lowInclusive, interval1.high, interval1.highInclusive);
			} catch (NumberlineCoverageException e) {
				throw new AssertionError();
			}
		} else return null;
	}
	
	public void unionWith(NumberlineCoverage otherInterval) {
		for(Interval interval: otherInterval.intervals) {
			addInterval(interval);
		}
	}
	
	public void differenceWith(NumberlineCoverage otherInterval) {
		for(Interval interval: otherInterval.intervals) {
			removeInterval(interval);
		}
	}
	
	public void intersectWith(NumberlineCoverage otherInterval) {
		List<Interval> interlist = new LinkedList<>();
		
		for(Interval newInterval: otherInterval.intervals) {
			for(int i = 0; i < intervals.size(); i++) {
				Interval oldInterval = intervals.get(i);
				boolean lowContained = oldInterval.contains(newInterval.low);
				boolean highContained = oldInterval.contains(newInterval.high);
				if(newInterval.high < oldInterval.low | 
						(newInterval.high == oldInterval.low & 
						!(newInterval.highInclusive & oldInterval.lowInclusive))) { // if newInterval is before oldInterval (with consideration to edges)
					break;
				} else if(newInterval.low > oldInterval.high | 
						(newInterval.low == oldInterval.high &
						!(newInterval.lowInclusive & oldInterval.highInclusive))) {
					break;
				} else if(!lowContained & highContained) {
					try {
						interlist.add(new Interval(oldInterval.low, oldInterval.lowInclusive, newInterval.high, newInterval.highInclusive));
					} catch (NumberlineCoverageException e) {
						throw new AssertionError();
					}
				} else if(lowContained & !highContained) { 
					try {
						interlist.add(new Interval(newInterval.low, newInterval.lowInclusive, oldInterval.high, oldInterval.highInclusive));
					} catch (NumberlineCoverageException e) {
						throw new AssertionError();
					}
				} else if(lowContained & highContained) {
					interlist.add(newInterval.clone());
				} else { // else if oldInterval is contained in new, keep old
					interlist.add(oldInterval);
				}
			}
		}
		
		intervals = interlist;
	}
	
	public boolean contains(double number) {
		for(Interval interval: intervals) {
			if(interval.contains(number)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return MyTools.join(intervals, "U");
	}
}
