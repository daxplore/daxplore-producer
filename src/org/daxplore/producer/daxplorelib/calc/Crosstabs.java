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
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daxplore.producer.daxplorelib.About;
import org.daxplore.producer.daxplorelib.About.TimeSeriesType;
import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaScale;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort;
import org.daxplore.producer.daxplorelib.raw.VariableType;

public class Crosstabs {

	private class RawColumnInfo implements Comparable<RawColumnInfo>{
		String column;
		int index;
		VariableType type;
		public RawColumnInfo(String column, int index, VariableType type) {
			this.column = column; this.index = index; this.type = type;
		}
		@Override
		public int compareTo(RawColumnInfo o) {
			return column.compareTo(o.column);
		}
	}
	
	private Connection connection;
	private About about;
	
	private Object[][] rawdataTable;
	private String[] rawColnames;
			
	private int rawTimePointIndex;
	
	private boolean hasLoadedRawdata = false;
	
	public Crosstabs(Connection connection, About about) {
		this.connection = connection;
		this.about = about;
	}
	
	public void loadRawToMem() throws SQLException {
		long time = System.nanoTime();
		List<RawColumnInfo> tempRawColnames = new ArrayList<>();
		
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery("PRAGMA TABLE_INFO(rawdata)")) {
			for(int colIndex = 0; rs.next(); colIndex++) {
				String rawtype = rs.getString("type");
				VariableType type = VariableType.fromSqltype(rawtype);
				tempRawColnames.add(new RawColumnInfo(rs.getString("name"), colIndex, type));
			}
		}
		
		int colCount = tempRawColnames.size();
		Collections.sort(tempRawColnames);
		
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT count(*) as cnt from rawdata")) {
			rs.next();
			int rowCount = rs.getInt("cnt");
			rawdataTable = new Object[colCount][rowCount];
		}
		
		try(Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM rawdata")) {
			for(int row = 0; rs.next(); row++) {
				for(int col = 0; col < tempRawColnames.size(); col++) {
					switch (tempRawColnames.get(col).type) {
					case NUMERIC:
						double dval = rs.getDouble(tempRawColnames.get(col).index + 1);
						if(rs.wasNull()) {
							dval = Double.NaN;
						}
						rawdataTable[col][row] = dval;
						break;
					case TEXT:
						String sval = rs.getString(tempRawColnames.get(col).index + 1);
						rawdataTable[col][row] = sval;
						break;
					}
				}
			}
		}
		
		rawColnames = new String[tempRawColnames.size()];
		
		for(int col = 0; col < tempRawColnames.size(); col++) {
			rawColnames[col] = tempRawColnames.get(col).column.toUpperCase();
		}
		if(about.getTimeSeriesType() == TimeSeriesType.SHORT) {
			rawTimePointIndex = Arrays.binarySearch(rawColnames, about.getTimeSeriesShortColumn().toUpperCase());
		}
		
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
	public BarStats crosstabs2(About about, MetaQuestion question, MetaQuestion perspective, int lowerLimit) throws DaxploreException {
		try {
			BarStats stats = new BarStats(question, perspective);
			int[][] crosstabs;
			int[] frequencies;
			switch (about.getTimeSeriesType()) {
			case NONE:
				crosstabs = crosstabs2(question, perspective, null, lowerLimit);
				frequencies = frequencies(question, null, lowerLimit);
				stats.addTimePoint(0, crosstabs, frequencies);
				break;
			case SHORT:
				List<MetaTimepointShort> questionTimes = question.getTimepoints();
				List<MetaTimepointShort> perspectiveTimes = perspective.getTimepoints();
				
				LinkedList<MetaTimepointShort> commonTimes = new LinkedList<>();
				
				for(MetaTimepointShort qTime: questionTimes) {
					if(perspectiveTimes.contains(qTime)) {
						commonTimes.add(qTime);
					}
				}
				
				for(MetaTimepointShort timepoint: commonTimes) {
					crosstabs = crosstabs2(question, perspective, timepoint, lowerLimit);
					frequencies = frequencies(question, timepoint, lowerLimit);
					stats.addTimePoint(timepoint.getTimeindex(), crosstabs, frequencies);
				}
				break;
			}
			return stats;
		} catch (SQLException | NullPointerException | ArrayIndexOutOfBoundsException e) {
			throw new DaxploreException("Failed to generate crosstabs for question: " + question.getId() + ", perspective: " + perspective.getId(), e);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int[][] crosstabs2(MetaQuestion question, MetaQuestion perspective, MetaTimepointShort timepoint, int lowerlimit) throws SQLException {
		if(question.getScale() == null && perspective.getScale() == null) { 
			return null;
		}
		
		int[][] crosstabsdata = new int[perspective.getScale().getOptionCount()][question.getScale().getOptionCount()];
		
		int[] totals = new int[perspective.getScale().getOptionCount()];	
		
		if(!hasLoadedRawdata) {
			loadRawToMem();
		}

		int questionColIndex = Arrays.binarySearch(rawColnames, question.getColumn().toUpperCase());
		int perspectiveColIndex = Arrays.binarySearch(rawColnames, perspective.getColumn().toUpperCase());
		
		MetaScale questionScale = question.getScale();
		MetaScale perspectiveScale = perspective.getScale();
		
		for(int row = 0; row < rawdataTable[0].length; row++) {
			if(timepoint == null || ((Double)rawdataTable[rawTimePointIndex][row]) == timepoint.getValue()) { //TODO timepoint: support TEXT/REAL for value
				int qindex = questionScale.getOptionIndex(rawdataTable[questionColIndex][row]);
				int pindex = perspectiveScale.getOptionIndex(rawdataTable[perspectiveColIndex][row]);
				if(qindex == -1 || pindex == -1) {
					continue;
				}
				totals[pindex]++;
				crosstabsdata[pindex][qindex]++;
			}
		}

		/*} else { //TODO unused 2015-06-18
			//TODO: handle case where scale only has ignore
			
			try (Statement stmt = connection.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT " + question.getId() + " as q, " + perspective.getId() 
					+ " as p FROM rawdata WHERE " + about.getTimeSeriesShortColumn() + " = " + timepoint.getValue())) { //TODO: get query to work with prepared statement
				while(rs.next()) {
					Double qvalue = rs.getDouble("q");
					int qindex = question.getScale().getOptionIndex(qvalue);
					Double pvalue = rs.getDouble("p");
					int pindex = perspective.getScale().getOptionIndex(pvalue);
					//TODO handle ignore?
					if(qindex == -1 || pindex == -1) {
						continue;
					}
					totals[pindex]++;
					crosstabsdata[pindex][qindex]++;
				}
			}
		}*/
		
		for(int ti = 0; ti < totals.length; ti++) {
			if(totals[ti] < lowerlimit) {
				crosstabsdata[ti] = new int[crosstabsdata[ti].length];
			}
		}
		
		return crosstabsdata;
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int[] frequencies(MetaQuestion question, MetaTimepointShort timepoint, int lowerlimit) throws SQLException {
		int[] frequencies = new int[question.getScale().getOptionCount()];
		int total = 0;

		if(!hasLoadedRawdata) {
			loadRawToMem();
		}
		MetaScale scale = question.getScale();
		
		int questionColIndex = Arrays.binarySearch(rawColnames, question.getColumn().toUpperCase());
		
		for(int row = 0; row < rawdataTable[0].length; row++) {
			if(timepoint == null || (Double)rawdataTable[rawTimePointIndex][row] == timepoint.getValue()) { //TODO timepoint: support TEXT/REAL for value
				int index = scale.getOptionIndex(rawdataTable[questionColIndex][row]);
				if(index != -1) {
					frequencies[index]++;
					total++;
				}
			}
		}

			
		/*} else { //TODO unused 2015-06-18

			try (Statement stmt = connection.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT " + question.getId() + " FROM rawdata WHERE " 
			+ about.getTimeSeriesShortColumn() + " = " + timepoint.getValue())) { //TODO: get query to work with prepared statement
			
				while(rs.next()) {
					Double value = rs.getDouble(question.getId());
					int index = question.getScale().getOptionIndex(value);
					if(index != -1) {
						frequencies[index]++;
						total++;
					}
				}
			}
		}*/
		
		if(total < lowerlimit) {
			return new int[frequencies.length];
		}
		
		return frequencies;
	}
	
}


