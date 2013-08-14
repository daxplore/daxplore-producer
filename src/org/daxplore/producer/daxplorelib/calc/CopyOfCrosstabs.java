/**
 * 
 */
package org.daxplore.producer.daxplorelib.calc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daxplore.producer.daxplorelib.About;
import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaScale;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort;
import org.daxplore.producer.tools.Pair;

import com.google.appengine.repackaged.org.apache.commons.logging.Log;

public class CopyOfCrosstabs {

	private Connection connection;
	private About about;
	
	private double[][] rawdataTable;
	private String[] rawColnames;
	private int rawTimePointIndex;
	
	private boolean hasLoadedRawdata = false;
	
	public CopyOfCrosstabs(Connection connection, About about) {
		this.connection = connection;
		this.about = about;
	}
	
	public <T> void loadRawToMem() throws SQLException {
		long time = System.nanoTime();
		ResultSet rs = connection.createStatement().executeQuery("PRAGMA TABLE_INFO(rawdata)");
		ArrayList<Pair<String, Integer>> tempRawColnames = new ArrayList<Pair<String, Integer>>();
		
		for(int colIndex = 0; rs.next(); colIndex++) {
			String type = rs.getString("type");
			if(type.equalsIgnoreCase("real")) {
				tempRawColnames.add(new Pair<String, Integer>(rs.getString("name"), colIndex));
			}
		}
		
		int colCount = tempRawColnames.size();
		Collections.sort(tempRawColnames, new Comparator<Pair<String, Integer>>() {
			@Override
			public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		
		
		
		rs = connection.createStatement().executeQuery("SELECT count(*) as cnt from rawdata");
		rs.next();
		int rowCount = rs.getInt("cnt");
		
		rawdataTable = new double[rowCount][colCount];
		
		rs = connection.createStatement().executeQuery("SELECT * FROM rawdata");
		
		for(int row = 0; rs.next(); row++) {
			for(int col = 0; col < tempRawColnames.size(); col++) {
				double val = rs.getDouble(tempRawColnames.get(col).getValue() + 1);
				if(rs.wasNull()) {
					val = Double.NaN;
				}
				rawdataTable[row][col] = val;
			}
		}
		
		rawColnames = new String[tempRawColnames.size()];
		
		for(int col = 0; col < tempRawColnames.size(); col++) {
			rawColnames[col] = tempRawColnames.get(col).getKey();
		}
		
		rawTimePointIndex = Arrays.binarySearch(rawColnames, about.getTimeSeriesShortColumn().toUpperCase());
		
		//Logger.getGlobal().log(Level.INFO, "Loaded rawdata to memory: " + ((System.nanoTime() -time)/Math.pow(10,9)) + "s");
		hasLoadedRawdata = true;
	}
	
	public void dropRawFromMem() {
		hasLoadedRawdata = false;
		rawdataTable = null;
		rawColnames = null;
	}
	
	public BarStats crosstabs2(MetaQuestion question, MetaQuestion perspective) throws DaxploreException {
		try {
			List<MetaTimepointShort> questionTimes = question.getTimepoints();
			List<MetaTimepointShort> perspectiveTimes = perspective.getTimepoints();
			
			LinkedList<MetaTimepointShort> commonTimes = new LinkedList<MetaTimepointShort>();
			
			for(MetaTimepointShort qTime: questionTimes) {
				if(perspectiveTimes.contains(qTime)) {
					commonTimes.add(qTime);
				}
			}
			
			BarStats stats = new BarStats(question, perspective);
			
			for(MetaTimepointShort timepoint: commonTimes) {
				int[][] crosstabs = crosstabs2(question, perspective, timepoint);
				int[] frequencies = frequencies(question, timepoint);
				stats.addTimePoint(timepoint, crosstabs, frequencies);
			}
			
			return stats;
		} catch (SQLException | NullPointerException e) {
			throw new DaxploreException("Failed to generate crosstabs for question: " + question.getId() + ", perspective: " + perspective.getId(), e);
		}
	}
	
	public int[][] crosstabs2(MetaQuestion question, MetaQuestion perspective, MetaTimepointShort timepoint) throws SQLException {
		if(question.getScale() != null && perspective.getScale() != null) {
			int[][] crosstabsdata = new int[perspective.getScale().getOptionCount()][question.getScale().getOptionCount()];

			if(hasLoadedRawdata) {
				
				int questionColIndex = Arrays.binarySearch(rawColnames, question.getId());
				int perspectiveColIndex = Arrays.binarySearch(rawColnames, perspective.getId());
				
				MetaScale questionScale = question.getScale();
				MetaScale perspectiveScale = perspective.getScale();
				
				for(int row = 0; row < rawdataTable.length; row++) {
					if(rawdataTable[row][rawTimePointIndex] == timepoint.getValue()) {
						int qindex = questionScale.matchIndex(rawdataTable[row][questionColIndex]);
						int pindex = perspectiveScale.matchIndex(rawdataTable[row][perspectiveColIndex]);
						if(qindex == -1 || pindex == -1) {
							continue;
						}
						crosstabsdata[pindex][qindex]++;
					}
				}
				return crosstabsdata;
				
			} else {
				//TODO: handle case where scale only has ignore
				
	//			PreparedStatement stmt1 = connection.prepareStatement("SELECT ?, ? FROM rawdata WHERE ? = ?");
	//			stmt1.setString(1, question.getId());
	//			stmt1.setString(2, perspective.getId());
	//			stmt1.setString(3, about.getTimeSeriesShortColumn());
	//			stmt1.setDouble(4, timepoint.getValue());
	//			ResultSet rs = stmt1.executeQuery();
				
				ResultSet rs = connection.createStatement().executeQuery("SELECT " + question.getId() + " as q, " + perspective.getId() 
						+ " as p FROM rawdata WHERE " + about.getTimeSeriesShortColumn() + " = " + timepoint.getValue());
				
				while(rs.next()) {
					Double qvalue = rs.getDouble("q");
					int qindex = question.getScale().matchIndex(qvalue);
					Double pvalue = rs.getDouble("p");
					int pindex = perspective.getScale().matchIndex(pvalue);
					//TODO handle ignore?
					if(qindex == -1 || pindex == -1) {
						continue;
					}
					crosstabsdata[pindex][qindex]++;
				}
				return crosstabsdata;
			}
		} else {
			//TODO handle cases where there is no scale
			return null;
		}
	}
	
	public int[] frequencies(MetaQuestion question, MetaTimepointShort timepoint) throws SQLException {
		int[] frequencies = new int[question.getScale().getOptionCount()];

		if(hasLoadedRawdata) {
			MetaScale scale = question.getScale();
			
			int questionColIndex = Arrays.binarySearch(rawColnames, question.getId());
			
			for(int row = 0; row < rawdataTable.length; row++) {
				if(rawdataTable[row][rawTimePointIndex] == timepoint.getValue()) {
					int index = scale.matchIndex(rawdataTable[row][questionColIndex]);
					if(index != -1) {
						frequencies[index]++;;
					}
				}
			}
			return frequencies;
			
		} else {
	//		PreparedStatement stmt = connection.prepareStatement("SELECT ? FROM rawdata WHERE ? = ?");
	//		stmt.setString(1, question.getId());
	//		stmt.setString(2, about.getTimeSeriesShortColumn());
	//		stmt.setDouble(3, timepoint.getValue());
	//		ResultSet rs = stmt.executeQuery();
			ResultSet rs = connection.createStatement().executeQuery("SELECT " + question.getId() + " FROM rawdata WHERE " + about.getTimeSeriesShortColumn() + " = " + timepoint.getValue());
			
			
			while(rs.next()) {
				Double value = rs.getDouble(question.getId());
				int index = question.getScale().matchIndex(value);
				if(index != -1) {
					frequencies[index]++;
				}
			}
			return frequencies;
		}
	}
	
}


