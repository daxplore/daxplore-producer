package daxplorelib.metadata;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class MetaScaleTransformation {

	SortedSet<Integer> numbers = new TreeSet<Integer>();
	
	public MetaScaleTransformation(String transformString) throws NumberFormatException {
		String[] transformParts = transformString.split(",");
		for(String part: transformParts) {
			String[] values = part.split("-");
			switch(values.length) {
			case 1:
				numbers.add(Integer.parseInt(values[0]));
				break;
			case 2:
				Integer begin = Integer.parseInt(values[0]);
				Integer end = Integer.parseInt(values[1]);
				if(begin < end) {
					for(Integer i = begin; i <= end; i++) {
						numbers.add(i);
					}
				} else {
					throw new NumberFormatException("Beginning of inteval can't be after end: " + begin + ">=" + end);
				}
				break;
			default:
				throw new NumberFormatException("Not a valid number or interval: " + part);
			}
		}
	}
	
	public void add(int value) {
		numbers.add(value);
	}
	
	public void remove(int value) {
		numbers.remove(value);
	}
	
	/**
	 * Add numbers in the interval (inclusive) to the transform.
	 * @param begin
	 * @param end
	 */
	public void addInterval(int begin, int end) {
		for(Integer i = begin; i <= end; i++) {
			numbers.add(i);
		}
	}
	
	/**
	 * Remove numbers in the interval (inclusive) from the transform.
	 * @param begin
	 * @param end
	 */
	public void removeInteval(int begin, int end) {
		for(Integer i = begin; i <= end; i++) {
			numbers.remove(i);
		}
	}
	
	public boolean contains(int value) {
		return numbers.contains(value);
	}
	
	public String transformString() {
		StringBuilder ts = new StringBuilder();
		Iterator<Integer> iter = numbers.iterator();
		int begin = iter.next();
		int end = begin;
		while(iter.hasNext()) {
			int next = iter.next();
			if (end+1==next) {
				end++;
			} else {
				switch(end-begin) {
				case 1:
					ts.append(","+begin);
					break;
				case 2:
					ts.append(","+begin+","+end);
					break;
				default:
					ts.append(","+begin+"-"+end);
				}
				begin = next;
				end = next;
			}
		}
		return ts.toString();
	}
	
}
