/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib.resources;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class DaxploreProperties {

	public static final int filetypeversionmajor = 0;
	public static final int filetypeversionminor = 3;
	
	public static final List<Locale> predefinedLocales =
			ImmutableList.copyOf(Arrays.asList(
					new Locale("sv"),
					new Locale("en")));
	
	public static final List<String> clientSettings;
	public static final Map<String, Object> clientSettingsDefaults;
	static {
		Map<String, Object> settingsMap = new LinkedHashMap<String, Object>(); 
		//TODO: rename things to more descriptive names
		settingsMap.put("csv", new Boolean(false));
		settingsMap.put("img", new Boolean(false));
		settingsMap.put("embed", new Boolean(true));
		settingsMap.put("timebuttons", new Boolean(false));
		settingsMap.put("typebuttons", new Boolean(false));
		settingsMap.put("respondents", new Boolean(true));
		settingsMap.put("defaultSelectedPerspectiveOptions", new Integer(4));
		settingsMap.put("defaultSelectTotal", new Boolean(true));
		settingsMap.put("perspectiveCheckboxesPerColumn", new Integer(8));
		settingsMap.put("questionDescriptionPosition", new String("BOTTOM")); //TODO enums
		settingsMap.put("perspectiveDescriptionPosition", new String("BOTTOM"));
		clientSettingsDefaults = ImmutableMap.copyOf(settingsMap);
		clientSettings = ImmutableList.copyOf(clientSettingsDefaults.keySet());
	}
	
	public static final List<String> presenterUITexts =
			ImmutableList.copyOf(Arrays.asList(
					"allRespondents",
					"compareWithAll",
					"compareWithOld",
					"compareWithOldTitleDisabled",
					"compareWithOldTitleEnabled",
					"csvButtonTitle",
					"embedButtonNumbers",
					"embedButtonTexts",
					"embedButtonTitle",
					"embedPopupDescription",
					"embedPopupTitle",
					"embedPopupTitleSize",
					"embedSettingsHeader",
					"embedShowLegend",
					"embedTransparentBackground",
					"hideWarningButton",
					"onlyShowNewTitleDisabled",
					"onlyShowNewTitleEnabled",
					"onlyShowNew",
					"pageTitle",
					"pickAQuestionHeader",
					"pickSelectionAlternativesHeader",
					"pickSelectionGroupHeader",
					"printButtonTitle",
					"secondaryFlag",
					"showAverageTitleDisabled",
					"showAverageTitleEnabled",
					"showAverage",
					"showFrequencyTitleDisabled",
					"showFrequencyTitleEnabled",
					"showFrequency"));
}