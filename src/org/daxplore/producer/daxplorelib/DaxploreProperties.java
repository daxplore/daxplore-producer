/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class DaxploreProperties {

	static final int filetypeversionmajor = 0;
	static final int filetypeversionminor = 3;
	
	public static final String[] properties = {"page_title", "secondary_flag", "all_respondents"};
	
	//public static final String[] clientBoolSettings = {"freq", "mean", "extratext", "csv", "img", "embed", "timeseries"};
	public static final List<String> clientBoolSettings;
	public static final Map<String, Boolean> clientBoolSettingsDefaults;
	static {
		LinkedHashMap<String, Boolean> map = new LinkedHashMap<String, Boolean>();
		map.put("freq", true);
		map.put("mean", false);
		map.put("extratext", false);
		map.put("csv", true);
		map.put("img", false);
		map.put("embed", true);
		map.put("timeseries", false);
		clientBoolSettings = ImmutableList.copyOf(map.keySet());
		clientBoolSettingsDefaults = ImmutableMap.copyOf(map);
	}
}
