package tools;

import java.util.LinkedList;
import java.util.List;


/**
 * @author Axel Winkler, Daniel Dunér
 */
public class MathInterval{
	
	protected class Interval implements Cloneable{
		double low, high;
		boolean lowInclusive, highInclusive;
		
		public Interval(double low, boolean lowInclusive, double high, boolean highInclusive) {
			if(high >= low) {
				this.low = low; this.high = high; this.lowInclusive = lowInclusive; this.highInclusive = highInclusive;
			} else {
				this.low = high; this.high = low; this.lowInclusive = highInclusive; this.highInclusive = lowInclusive;				
			}
		}
		
		public Interval(String inter) {
			lowInclusive = inter.charAt(0) == '[';
			highInclusive = inter.charAt(inter.length() -1) == ']';
			inter = inter.substring(1, inter.length()-1);
			String[] numbers = inter.split(",");
			low = Double.parseDouble(numbers[0]);
			high = Double.parseDouble(numbers[1]);
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
		
		public String toString() {
			return (lowInclusive ? "[": "(") + 
					low + "," + high + 
					(highInclusive ? "]" : ")");
		}
	}
	
	List<Interval> intervals = new LinkedList<Interval>();
	
	public static void main(String[] args){
		//System.out.println(Double.valueOf("-inf"));
		//System.out.println(Double.valueOf("-Inf"));
		//System.out.println(Double.valueOf("-infinity"));
		System.out.println(Double.valueOf("-Infinity"));
		
		double d = Double.parseDouble("-Infinity");
		String a ="look " + d;
		System.out.println(a);
		
		MathInterval mi = new MathInterval("[-Infinity, -1)U[0,5]");
		System.out.println(mi);
		MathInterval mi2 = new MathInterval("[-3, -1]U[2,7]");
		System.out.println(mi2);
		mi.unionWith(mi2);
		System.out.println(mi);
		
	}
	
	public MathInterval(String intervalString) {
		String[] interStrings = intervalString.split("U");
		for(String inter: interStrings) {
			intervals.add(new Interval(inter));
		}
	}
	
	public MathInterval(double a, boolean aInclusive, double b, boolean bInclusive) {
		intervals.add(new Interval(a, aInclusive, b, bInclusive));
	}
	
	protected void addInterval(Interval interval) {
		addInterval(interval.low, interval.lowInclusive, interval.high, interval.highInclusive);
	}
	
	public void addInterval(double a, boolean aInclusive, double b, boolean bInclusive) {
		Interval newInterval = new Interval(a, aInclusive, b, bInclusive);
		List<Interval> interlist = new LinkedList<Interval>();
		
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
			} else {
				newInterval = combine(oldInterval, newInterval);
			}
		}
		intervals = interlist;
	}
	
	protected void removeInterval(Interval interval) {
		removeInterval(interval.low, interval.lowInclusive, interval.high, interval.highInclusive);
	}
	
	public void removeInterval(double a, boolean aInclusive, double b, boolean bInclusive) {
		Interval newInterval = new Interval(a, aInclusive, b, bInclusive);
		List<Interval> interlist = new LinkedList<Interval>();
		
		for(int i = 0; i < intervals.size(); i++) {
			Interval oldInterval = intervals.get(i);
			boolean lowContained = oldInterval.contains(newInterval.low);
			boolean highContained = oldInterval.contains(newInterval.high);
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
				interlist.add(new Interval(newInterval.high, !newInterval.highInclusive, oldInterval.high, oldInterval.highInclusive));
			} else if(lowContained & !highContained) { 
				interlist.add(new Interval(oldInterval.low, oldInterval.lowInclusive, newInterval.low, !newInterval.lowInclusive));
			} else if(lowContained & highContained) {
				interlist.add(new Interval(oldInterval.low, oldInterval.lowInclusive, newInterval.low, !newInterval.lowInclusive));
				interlist.add(new Interval(newInterval.high, !newInterval.highInclusive, oldInterval.high, oldInterval.highInclusive));
			} // else if oldInterval is contained in new, remove old.
		}
		
		intervals = interlist;
	}
	
	protected Interval combine(Interval interval1, Interval interval2) {
		boolean lowIncluded = interval1.contains(interval2.low);
		boolean highIncluded = interval1.contains(interval2.high);
		if(lowIncluded & highIncluded) {
			return interval1;
		} else if(lowIncluded) {
			return new Interval(interval1.low, interval1.lowInclusive, interval2.high, interval2.highInclusive);
		} else if(highIncluded) {
			return new Interval(interval2.low, interval2.lowInclusive, interval1.high, interval1.highInclusive);
		} else return null;
	}
	
	public void addValue(double value) {
		addInterval(value, true, value, true);
	}
	
	public void unionWith(MathInterval otherInterval) {
		for(Interval interval: otherInterval.intervals) {
			addInterval(interval);
		}
	}
	
	public void differenceWith(MathInterval otherInterval) {
		for(Interval interval: otherInterval.intervals) {
			removeInterval(interval);
		}
	}
	
	public void intersectWith(MathInterval otherInterval) {
		List<Interval> interlist = new LinkedList<Interval>();
		
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
					interlist.add(new Interval(oldInterval.low, oldInterval.lowInclusive, newInterval.high, newInterval.highInclusive));
				} else if(lowContained & !highContained) { 
					interlist.add(new Interval(newInterval.low, newInterval.lowInclusive, oldInterval.high, oldInterval.highInclusive));
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
