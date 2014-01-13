/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib.raw;

public class VariableOptionInfo {
	private Double value;
	private String rawText;
	private int count;
	
	public VariableOptionInfo(Double value) {
		this.value = value;
	}
	
	public Double getValue() {
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
