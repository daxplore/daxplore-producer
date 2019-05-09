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
	/**
	 * SQLite's application ID for .daxplore save file, stored in sqlite's PRAGMA application_id.
	 * https://sqlite.org/pragma.html#pragma_application_id
	 **/
	public static final int daxploreFileApplicationID = 474105420;

	/**
	 * File version for .daxplore save file, stored in sqlite's PRAGMA user_version.
	 * https://sqlite.org/pragma.html#pragma_user_version
	 **/
	public static final int daxploreFileVersion = 4;
	
	/** File version for export .zip data file, stored in manifest.xml **/
	public static final int exportFileVersion = 4;
	
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
		settingsMap.put("showSelectTotal", new Boolean(true));
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
					"chartTabDichotomized",
					"chartTabFrequencies",
					"chartTabMeans",
					"compareWithAll",
					"compareWithOld",
					"compareWithOldTitleDisabled",
					"compareWithOldTitleEnabled",
					"csvButtonTitle",
					"dichotomizedSubtitleStart",
					"dichotomizedSubtitleSeparator",
					"dichotomizedSubtitleOr",
					"dichotomizedSubtitleEnd",
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
					"imageSaveButton",
					"imageTitleProfileChart",
					"imageWaterStamp",
					"listReferenceValue",
					"listReferenceBetter",
					"listReferenceWorse",
					"listReferenceComparable",
					"listXAxisDescription",
					"onlyShowNewTitleDisabled",
					"onlyShowNewTitleEnabled",
					"onlyShowNew",
					"pageTitle",
					"perspectivesAllButton",
					"perspectivesHeader",
					"perspectivesLessButton",
					"perspectivesMoreButton",
					"perspectivesNoneButton",
					"printButtonTitle",
					"questionsHeader",
					"secondaryFlag",
					"showAverageTitleDisabled",
					"showAverageTitleEnabled",
					"showAverage",
					"showFrequencyTitleDisabled",
					"showFrequencyTitleEnabled",
					"showFrequency",
					"userProfileHeaderText",
					"userProfilePasteDataDescription",
					"userProfilePasteDataSubmitExplanation",
					"userPasteDataSubmitButton",
					"userProfilePasteDataErrorLogHeader",
					"userPasteDataErrorTextNumberBoundsErrors",
					"userPasteDataErrorTextNoNumberErrors",
					"userPasteDataErrorTextNoRowErrors"
				));
}
