package org.daxplore.producer.daxplorelib.raw;

public class VariableOptionInfo {
	private double value;
	private String rawText;
	private int count;
	
	public VariableOptionInfo(double value) {
		this.value = value;
	}
	
	public double getValue() {
		return value;
	}
	
	public String getRawText() {
		return rawText;
	}
	
	public void setRawText(String rawText) {
		this.rawText = rawText;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
}
