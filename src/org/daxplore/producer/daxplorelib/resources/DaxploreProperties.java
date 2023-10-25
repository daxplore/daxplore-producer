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
	public static final int daxploreFileVersion = 6;
	
	/** File version for export .zip data file, stored in manifest.json**/
	public static final int exportDataPackageVersion = 8;
	
	public static final List<Locale> predefinedLocales =
			ImmutableList.copyOf(Arrays.asList(
					new Locale("sv"),
					new Locale("en")));
	
	public static final List<String> clientSettings;
	public static final Map<String, Object> clientSettingsDefaults;
	static {
		Map<String, Object> settingsMap = new LinkedHashMap<String, Object>(); 
		settingsMap.put("chart.show_respondent_count", true);
		settingsMap.put("explorer.chart.mean.orientation", "HORIZONTAL"); // TODO enum {HORIZONTAL, VERTICAL}
		settingsMap.put("explorer.description.position", "BOTTOM"); // TODO enum {BOTTOM, LEFT, HEADER}
		settingsMap.put("explorer.perspective.checkboxes_per_column", 8);
		settingsMap.put("explorer.perspective.default_selected_option_count", 10);
		settingsMap.put("explorer.perspective.compare_to_all.default_select", false);
		settingsMap.put("explorer.perspective.compare_to_all.show", true);
		settingsMap.put("export.manifest.project_name", "New Daxplore Project");
		clientSettingsDefaults = ImmutableMap.copyOf(settingsMap);
		clientSettings = ImmutableList.copyOf(clientSettingsDefaults.keySet());
	}
	
	public static final List<String> presenterUITexts =
			ImmutableList.copyOf(Arrays.asList(
					"common.button.save_chart_as_image",
					
					"explorer.chart_tab.dichotomized_line",
					"explorer.chart_tab.frequency_bar",
					"explorer.chart_tab.mean_bar",
					
					"explorer.chart.dichotomized_line.image.filename",
					"explorer.chart.dichotomized_line.subtitle_end",
					"explorer.chart.dichotomized_line.subtitle_or",
					"explorer.chart.dichotomized_line.subtitle_separator",
					"explorer.chart.dichotomized_line.subtitle_start",
					
					"explorer.chart.frequency_bar.image.filename",
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
					
					"profile.image.filename",
					"profile.image.watermark",
					
					"profile.reference.better",
					"profile.reference.comparable",
					"profile.reference.value",
					"profile.reference.worse",
					
					"profile.x_axis_description",
					
					"radar.image.filename",
					"radar.image.watermark",
					
					"radar_model.image.filename",
					"radar_model.image.watermark",
					
					"user_profile.grid.image.filename",
					"user_profile.grid.image.watermark",
					
					"user_profile.profile.image.filename",
					"user_profile.profile.image.watermark",
					
					"user_profile.radar.image.filename",
					"user_profile.radar.image.watermark",
					
					"user_profile.radar_model.image.filename",
					"user_profile.radar_model.image.watermark",
					
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
