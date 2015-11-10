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
import org.daxplore.producer.daxplorelib.DaxploreWarning;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaScale;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort;
import org.daxplore.producer.daxplorelib.raw.VariableType;

import com.google.common.base.Joiner;

public class StatsCalculation {

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
	
	public StatsCalculation(Connection connection, About about) {
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
			rawColnames[col] = tempRawColnames.get(col).column;
		}
		if(about.getTimeSeriesType() == TimeSeriesType.SHORT) {
			rawTimePointIndex = Arrays.binarySearch(rawColnames, about.getTimeSeriesShortColumn());
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
	 * Calculate data for a question and perspective pair. Only returns results if total is over lowerlimit.
	 * @param question
	 * @param perspective
	 * @param lowerLimit
	 * @return
	 * @throws DaxploreException
	 * @throws DaxploreWarning 
	 */
	public BarStats calculateData(About about, MetaQuestion question, MetaQuestion perspective, int lowerLimit) throws DaxploreWarning, DaxploreException {
		try {
			BarStats stats = new BarStats(question, perspective);
			int[][] crosstabs;
			int[] frequencies;
			List<double[]> means; // <mean, allmean, count>
			
			List<String> warnings = new LinkedList<String>();
			
			switch (about.getTimeSeriesType()) {
			case NONE:
				if(question.useFrequencies()) {
					try {
						crosstabs = freqCalc(question, perspective, null, lowerLimit);
						//TODO add option to choose how to calculate the "all" group
						// A) As frequencies, as it currently is
						// B) As a sum of the crosstab data
						frequencies = frequencies(question, null, lowerLimit);
						stats.addFrequencyData(0, crosstabs, frequencies);
					} catch (DaxploreWarning e) {
						warnings.add(e.getMessage());
					}
				}
				if(question.useMean()) {
					try {
						means = meanCalc(question, perspective, null, lowerLimit);
						double[] counts = means.get(2);
						int[] intCounts = new int[counts.length];
						for(int i=0; i<counts.length; i++){
							intCounts[i] = (int)counts[i];
						}
						stats.addMeanData(0, means.get(0), means.get(1)[0], intCounts, (int)means.get(3)[0]);
					} catch (DaxploreWarning e) {
						warnings.add(e.getMessage());
					}
				}
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
					if(question.useFrequencies()) {
						try {
							crosstabs = freqCalc(question, perspective, timepoint, lowerLimit);
							frequencies = frequencies(question, timepoint, lowerLimit);
							stats.addFrequencyData(timepoint.getTimeindex(), crosstabs, frequencies);
						} catch (DaxploreWarning e) {
							warnings.add(e.getMessage());
						}
					}
					if(question.useMean()) {
						try {
							means = meanCalc(question, perspective, timepoint, lowerLimit);
							double[] counts = means.get(2);
							int[] intCounts = new int[counts.length];
							for(int i=0; i<counts.length; i++){
								intCounts[i] = (int)counts[i];
							}
							stats.addMeanData(0, means.get(0), means.get(1)[0], intCounts, (int)means.get(3)[0]);
						} catch (DaxploreWarning e) {
							warnings.add(e.getMessage());
						}
					}
				}
				break;
			}
			
			if(!warnings.isEmpty()) {
				Joiner joiner = Joiner.on("\n");
				throw new DaxploreWarning(joiner.join(warnings));
			}
			
			return stats;
		} catch (SQLException | NullPointerException | ArrayIndexOutOfBoundsException | DaxploreException e) {
			throw new DaxploreException("Failed to generate data for question: " + question.getColumn() + ", perspective: " + perspective.getColumn(), e);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<double[]> meanCalc(MetaQuestion question, MetaQuestion perspective, MetaTimepointShort timepoint, int lowerlimit) throws DaxploreException, SQLException, DaxploreWarning {
		if(perspective.getScale() == null) { 
			throw new DaxploreException("meanCalc: perspective is missing scale");
		}
		
		// return values in order
		double[] meandata = new double[perspective.getScale().getOptionCount()];
		double[] meanall = new double[1];
		double[] count = new double[perspective.getScale().getOptionCount()];
		double[] countall = new double[1];
		
		if(!hasLoadedRawdata) {
			loadRawToMem();
		}

		int questionColIndex = Arrays.binarySearch(rawColnames, question.getColumn());
		int perspectiveColIndex = Arrays.binarySearch(rawColnames, perspective.getColumn());
		
		MetaScale perspectiveScale = perspective.getScale();
		
		for(int row = 0; row < rawdataTable[0].length; row++) {
			if(timepoint == null || ((Double)rawdataTable[rawTimePointIndex][row]) == timepoint.getValue()) {
				int pindex = perspectiveScale.getOptionIndex(rawdataTable[perspectiveColIndex][row]);
				if(pindex == -1 || question.getMetaMean().isExcluded(rawdataTable[questionColIndex][row])) {
					continue;
				}
				meandata[pindex] += (Double)rawdataTable[questionColIndex][row];
				count[pindex]++;
			}
		}
		
		for(int i = 0; i < meandata.length; i++) {
			if(count[i] >= lowerlimit) {
				meanall[0] += meandata[i];
				countall[0] += count[i];
				meandata[i] = meandata[i] / count[i];
			} else {
				meandata[i] = -1;
				count[i] = 0;
			}
		}

		if(countall[0] >= lowerlimit) {
			meanall[0] = meanall[0]/countall[0];
		} else {
			throw new DaxploreWarning("Could not generate mean data for question: " + question.getColumn() + " and perspective: " + perspective.getColumn());
		}
		
		ArrayList<double[]> list = new ArrayList<double[]>(4);
		list.add(meandata);
		list.add(meanall);
		list.add(count);
		list.add(countall);
		
		return list;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int[][] freqCalc(MetaQuestion question, MetaQuestion perspective, MetaTimepointShort timepoint, int lowerlimit) throws SQLException, DaxploreWarning, DaxploreException {
		if(question.getScale() == null && perspective.getScale() == null) { 
			return null;
		}
		
		int[][] crosstabsdata = new int[perspective.getScale().getOptionCount()][question.getScale().getOptionCount()];
		
		int[] totals = new int[perspective.getScale().getOptionCount()];	
		
		if(!hasLoadedRawdata) {
			loadRawToMem();
		}

		int questionColIndex = Arrays.binarySearch(rawColnames, question.getColumn());
		int perspectiveColIndex = Arrays.binarySearch(rawColnames, perspective.getColumn());
		
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
		
		int emptyCount = 0;
		for(int ti = 0; ti < totals.length; ti++) {
			if(totals[ti] < lowerlimit) {
				crosstabsdata[ti] = new int[0];
				emptyCount++;
			}
		}
		
		if(emptyCount == totals.length) {
			throw new DaxploreWarning("Could not generate frequency data for question: " + question.getColumn() + " and perspective: " + perspective.getColumn());
		}
		
		return crosstabsdata;
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int[] frequencies(MetaQuestion question, MetaTimepointShort timepoint, int lowerlimit) throws SQLException, DaxploreWarning {
		int[] frequencies = new int[question.getScale().getOptionCount()];
		int total = 0;

		if(!hasLoadedRawdata) {
			loadRawToMem();
		}
		MetaScale scale = question.getScale();
		
		int questionColIndex = Arrays.binarySearch(rawColnames, question.getColumn());
		
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
			throw new DaxploreWarning("Could not generate total frequencies for question: " + question.getColumn());
		}
		
		return frequencies;
	}
	
	
	
}


