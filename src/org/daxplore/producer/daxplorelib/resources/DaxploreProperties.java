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
	public static final int daxploreFileVersion = 5;
	
	/** File version for export .zip data file, stored in manifest.xml **/
	public static final int exportDataPackageVersion = 7;
	
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
		settingsMap.put("export.manifest.project_name", "New Daxplore Project");
		settingsMap.put("structure.perspectivePosition", "BOTTOM");
		settingsMap.put("chart.mean.orientation", "HORIZONTAL");
		clientSettingsDefaults = ImmutableMap.copyOf(settingsMap);
		clientSettings = ImmutableList.copyOf(clientSettingsDefaults.keySet());
	}
	
	public static final List<String> presenterUITexts =
			ImmutableList.copyOf(Arrays.asList(
					"common.button.save_chart_as_image",
					"explorer.chart_tab.dichotomized_line",
					"explorer.chart_tab.frequency_bar",
					"explorer.chart_tab.mean_bar",
					"explorer.chart.dichotomized_line.subtitle_end",
					"explorer.chart.dichotomized_line.subtitle_or",
					"explorer.chart.dichotomized_line.subtitle_separator",
					"explorer.chart.dichotomized_line.subtitle_start",
					"explorer.chart.frequency_bar.legend.missing_data",
					"explorer.chart.frequency_bar.tooltip.multiple_timepoints_missing",
					"explorer.chart.frequency_bar.tooltip.multiple_timepoints", 
					"explorer.chart.frequency_bar.tooltip.single_timepoint_missing",
					"explorer.chart.frequency_bar.tooltip.single_timepoint",
					"explorer.chart.mean_bar_horizontal.legend.missing_data",
					"explorer.chart.mean_bar_horizontal.legend.reference_value",
					"explorer.chart.mean_bar_horizontal.tooltip.few_respondents",
					"explorer.chart.mean_bar_horizontal.tooltip.mean",
					"explorer.chart.mean_bar_horizontal.tooltip.missing_data",
					"explorer.chart.mean_bar_horizontal.tooltip.reference_value",
					"explorer.chart.mean_bar_horizontal.tooltip.respondents",
					"explorer.chart.mean_bar_vertical.image.filename",
					"explorer.chart.mean_bar_vertical.reference.better",
					"explorer.chart.mean_bar_vertical.reference.comparable",
					"explorer.chart.mean_bar_vertical.reference.value",
					"explorer.chart.mean_bar_vertical.reference.worse",
					"explorer.chart.mean_bar_vertical.x_axis_description",
					"explorer.image.watermark",
					"explorer.perspective_picker.all_respondents",
					"explorer.perspective_picker.button.select_all",
					"explorer.perspective_picker.button.select_none",
					"explorer.perspective_picker.button.show_less",
					"explorer.perspective_picker.button.show_more",
					"explorer.perspective_picker.header",
					"explorer.perspective_picker.secondary_perspective.button.none",
					"explorer.perspective_picker.secondary_perspective.header",
					"explorer.question_picker.section_header",
					"profile.chart.mean_bar_vertical.x_axis_description",
					"profile.chart.mean_bar_vertical.image.watermark",
					"profile.chart.mean_bar_vertical.reference.better",
					"profile.chart.mean_bar_vertical.reference.comparable",
					"profile.chart.mean_bar_vertical.reference.value",
					"profile.chart.mean_bar_vertical.reference.worse",
					"profile.image.filename",
					"profile.image.watermark",
					"user_profile.chart.mean_bar_vertical.image.filename",
					"user_profile.grid.image.filename",
					"user_profile.image.watermark",
					"user_profile.paste_data.error_log.header.no_number",
					"user_profile.paste_data.error_log.header.no_row",
					"user_profile.paste_data.error_log.header.number_bounds",
					"user_profile.paste_data.error_log.header",
					"user_profile.paste_data.header",
					"user_profile.paste_data.instruction",
					"user_profile.paste_data.submit.button",
					"user_profile.paste_data.submit.explanation"
				));
}
