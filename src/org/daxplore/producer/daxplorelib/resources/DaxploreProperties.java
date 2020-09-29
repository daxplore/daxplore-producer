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
	public static final int daxploreFileApplicationID = 474105423;

	/**
	 * File version for .daxplore save file, stored in sqlite's PRAGMA user_version.
	 * https://sqlite.org/pragma.html#pragma_user_version
	 **/
	public static final int daxploreFileVersion = 4;
	
	/** File version for export .zip data file, stored in manifest.xml **/
	public static final int exportDataPackageVersion = 6;
	
	public static final List<Locale> predefinedLocales =
			ImmutableList.copyOf(Arrays.asList(
					new Locale("sv"),
					new Locale("en")));
	
	public static final List<String> clientSettings;
	public static final Map<String, Object> clientSettingsDefaults;
	static {
		Map<String, Object> settingsMap = new LinkedHashMap<String, Object>(); 
		//TODO: rename things to more descriptive names
		settingsMap.put("csv", false);
		settingsMap.put("img", false);
		settingsMap.put("embed", true);
		settingsMap.put("timebuttons", false);
		settingsMap.put("typebuttons", false);
		settingsMap.put("respondents", true);
		settingsMap.put("defaultSelectedPerspectiveOptions", 4);
		settingsMap.put("showSelectTotal", true);
		settingsMap.put("defaultSelectTotal", true);
		settingsMap.put("perspectiveCheckboxesPerColumn", 8);
		settingsMap.put("questionDescriptionPosition", "BOTTOM"); //TODO enums
		settingsMap.put("perspectiveDescriptionPosition", "BOTTOM");
		settingsMap.put("structure.perspectivePosition", "BOTTOM");
		settingsMap.put("chart.mean.orientation", "HORIZONTAL");
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
					"explorer.freq.legend.missing_data",
					"explorer.freq.tooltip.single",
					"explorer.freq.tooltip.single_missing_data",
					"explorer.freq.tooltip.timepoints", 
					"explorer.freq.tooltip.timepoints_missing_data",
					"explorer.perspective.header",
					"explorer.perspective.header_secondary",
					"explorer.perspective.none_button",
					"hideWarningButton",
					"imageSaveButton",
					"listReferenceValue",
					"listReferenceBetter",
					"listReferenceWorse",
					"listReferenceComparable",
					"listXAxisDescription",
					"meanbars_legend_missingData",
					"meanbars_legend_referenceValue",
					"meanbars_tooltip_fewRespondents",
					"meanbars_tooltip_mean",
					"meanbars_tooltip_missingData",
					"meanbars_tooltip_referenceValue",
					"meanbars_tooltip_respondents",
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
					"profile.image.filename",
					"profile.image.watermark",
					"profile_user.chart_image.filename",
					"profile_user.grid_image.filename",
					"profile_user.image.watermark",
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
