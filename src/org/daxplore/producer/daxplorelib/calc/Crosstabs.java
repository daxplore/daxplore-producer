/**
 * 
 */
package org.daxplore.producer.daxplorelib.calc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

public class Crosstabs {

	private Connection connection;
	private About about;
	
	private double[][] rawdataTable;
	private String[] rawColnames;
	private int rawTimePointIndex;
	
	private boolean hasLoadedRawdata = false;
	
	public Crosstabs(Connection connection, About about) {
		this.connection = connection;
		this.about = about;
	}
	
	public void loadRawToMem() throws SQLException {
		long time = System.nanoTime();
		ArrayList<Pair<String, Integer>> tempRawColnames = new ArrayList<>();
		
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery("PRAGMA TABLE_INFO(rawdata)")) {
			for(int colIndex = 0; rs.next(); colIndex++) {
				String type = rs.getString("type");
				if(type.equalsIgnoreCase("real")) {
					tempRawColnames.add(new Pair<>(rs.getString("name"), colIndex));
				}
			}
		}
		
		int colCount = tempRawColnames.size();
		Collections.sort(tempRawColnames, new Comparator<Pair<String, Integer>>() {
			@Override
			public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT count(*) as cnt from rawdata")) {
			rs.next();
			int rowCount = rs.getInt("cnt");
			rawdataTable = new double[colCount][rowCount];
		}
		
		try(Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM rawdata")) {
			for(int row = 0; rs.next(); row++) {
				for(int col = 0; col < tempRawColnames.size(); col++) {
					double val = rs.getDouble(tempRawColnames.get(col).getValue() + 1);
					if(rs.wasNull()) {
						val = Double.NaN;
					}
					rawdataTable[col][row] = val;
				}
			}
		}
		
		rawColnames = new String[tempRawColnames.size()];
		
		for(int col = 0; col < tempRawColnames.size(); col++) {
			rawColnames[col] = tempRawColnames.get(col).getKey().toUpperCase();
		}
		
		rawTimePointIndex = Arrays.binarySearch(rawColnames, about.getTimeSeriesShortColumn().toUpperCase());
		
		Logger.getGlobal().log(Level.INFO, "Loaded rawdata to memory: " + ((System.nanoTime() -time)/Math.pow(10,9)) + "s");
		hasLoadedRawdata = true;
	}
	
	public void dropRawFromMem() {
		hasLoadedRawdata = false;
		rawdataTable = null;
		rawColnames = null;
	}
	/**
	 * Generates crosstabs between cquestion and perspective. No result only if total results is over lowerlimit.
	 * @param question
	 * @param perspective
	 * @param lowerLimit
	 * @return
	 * @throws DaxploreException
	 */
	public BarStats crosstabs2(MetaQuestion question, MetaQuestion perspective, int lowerLimit) throws DaxploreException {
		try {
			List<MetaTimepointShort> questionTimes = question.getTimepoints();
			List<MetaTimepointShort> perspectiveTimes = perspective.getTimepoints();
			
			LinkedList<MetaTimepointShort> commonTimes = new LinkedList<>();
			
			for(MetaTimepointShort qTime: questionTimes) {
				if(perspectiveTimes.contains(qTime)) {
					commonTimes.add(qTime);
				}
			}
			
			BarStats stats = new BarStats(question, perspective);
			
			for(MetaTimepointShort timepoint: commonTimes) {
				int[][] crosstabs = crosstabs2(question, perspective, timepoint, lowerLimit);
				int[] frequencies = frequencies(question, timepoint, lowerLimit);
				stats.addTimePoint(timepoint, crosstabs, frequencies);
			}
			
			return stats;
		} catch (SQLException | NullPointerException | ArrayIndexOutOfBoundsException e) {
			throw new DaxploreException("Failed to generate crosstabs for question: " + question.getId() + ", perspective: " + perspective.getId(), e);
		}
	}
	
	private int[][] crosstabs2(MetaQuestion question, MetaQuestion perspective, MetaTimepointShort timepoint, int lowerlimit) throws SQLException {
		if(question.getScale() == null && perspective.getScale() == null) { 
			return null;
		}
		
		int[][] crosstabsdata = new int[perspective.getScale().getOptionCount()][question.getScale().getOptionCount()];
		
		int[] totals = new int[perspective.getScale().getOptionCount()];	
		
		if(hasLoadedRawdata) {
			
			int questionColIndex = Arrays.binarySearch(rawColnames, question.getId().toUpperCase());
			int perspectiveColIndex = Arrays.binarySearch(rawColnames, perspective.getId().toUpperCase());
			
			MetaScale questionScale = question.getScale();
			MetaScale perspectiveScale = perspective.getScale();
			
			for(int row = 0; row < rawdataTable[0].length; row++) {
				if(rawdataTable[rawTimePointIndex][row] == timepoint.getValue()) {
					int qindex = questionScale.matchIndex(rawdataTable[questionColIndex][row]);
					int pindex = perspectiveScale.matchIndex(rawdataTable[perspectiveColIndex][row]);
					if(qindex == -1 || pindex == -1) {
						continue;
					}
					totals[pindex]++;
					crosstabsdata[pindex][qindex]++;
				}
			}

		} else {
			//TODO: handle case where scale only has ignore
			
			try (Statement stmt = connection.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT " + question.getId() + " as q, " + perspective.getId() 
					+ " as p FROM rawdata WHERE " + about.getTimeSeriesShortColumn() + " = " + timepoint.getValue())) { //TODO: get query to work with prepared statement
				while(rs.next()) {
					Double qvalue = rs.getDouble("q");
					int qindex = question.getScale().matchIndex(qvalue);
					Double pvalue = rs.getDouble("p");
					int pindex = perspective.getScale().matchIndex(pvalue);
					//TODO handle ignore?
					if(qindex == -1 || pindex == -1) {
						continue;
					}
					totals[pindex]++;
					crosstabsdata[pindex][qindex]++;
				}
			}
		}
		
		for(int ti = 0; ti < totals.length; ti++) {
			if(totals[ti] < lowerlimit) {
				crosstabsdata[ti] = new int[crosstabsdata[ti].length];
			}
		}
		
		return crosstabsdata;
		
	}
	
	private int[] frequencies(MetaQuestion question, MetaTimepointShort timepoint, int lowerlimit) throws SQLException {
		int[] frequencies = new int[question.getScale().getOptionCount()];
		int total = 0;

		if(hasLoadedRawdata) {
			MetaScale scale = question.getScale();
			
			int questionColIndex = Arrays.binarySearch(rawColnames, question.getId().toUpperCase());
			
			for(int row = 0; row < rawdataTable[0].length; row++) {
				if(rawdataTable[rawTimePointIndex][row] == timepoint.getValue()) {
					int index = scale.matchIndex(rawdataTable[questionColIndex][row]);
					if(index != -1) {
						frequencies[index]++;
						total++;
					}
				}
			}

			
		} else {

			try (Statement stmt = connection.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT " + question.getId() + " FROM rawdata WHERE " 
			+ about.getTimeSeriesShortColumn() + " = " + timepoint.getValue())) { //TODO: get query to work with prepared statement
			
				while(rs.next()) {
					Double value = rs.getDouble(question.getId());
					int index = question.getScale().matchIndex(value);
					if(index != -1) {
						frequencies[index]++;
						total++;
					}
				}
			}
		}
		
		if(total < lowerlimit) {
			return new int[frequencies.length];
		}
		
		return frequencies;
	}
	
}


