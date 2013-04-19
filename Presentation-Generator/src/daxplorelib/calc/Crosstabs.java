/**
 * 
 */
package daxplorelib.calc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import daxplorelib.About;
import daxplorelib.metadata.MetaQuestion;
import daxplorelib.metadata.MetaTimepointShort;

public class Crosstabs {

	private Connection connection;
	private About about;
	
	public Crosstabs(Connection connection, About about) {
		this.connection = connection;
		this.about = about;
	}
	
	public BarStats crosstabs2(MetaQuestion question, MetaQuestion perspective) throws SQLException {
		List<MetaTimepointShort> questionTimes = question.getTimepoints();
		List<MetaTimepointShort> perspectiveTimes = perspective.getTimepoints();
		
		LinkedList<MetaTimepointShort> commonTimes = new LinkedList<MetaTimepointShort>();
		
		for(MetaTimepointShort qTime: questionTimes) {
			if(perspectiveTimes.contains(qTime)) {
				commonTimes.add(qTime);
			}
		}
		
		BarStats stats = new BarStats();
		
		for(MetaTimepointShort timepoint: commonTimes) {
			int[][] crosstabs = crosstabs2(question, perspective, timepoint);
			int[] frequencies = frequencies(question, timepoint);
			stats.addTimePoint(timepoint, crosstabs, frequencies);
		}
		
		return stats;
	}
	
	public int[][] crosstabs2(MetaQuestion question, MetaQuestion perspective, MetaTimepointShort timepoint) throws SQLException {
		if(question.getScale() != null && perspective.getScale() != null) {
			//TODO: handle case where scale only has ignore
			
//			PreparedStatement stmt1 = connection.prepareStatement("SELECT ?, ? FROM rawdata WHERE ? = ?");
//			stmt1.setString(1, question.getId());
//			stmt1.setString(2, perspective.getId());
//			stmt1.setString(3, about.getTimeSeriesShortColumn());
//			stmt1.setDouble(4, timepoint.getValue());
//			ResultSet rs = stmt1.executeQuery();
			
			ResultSet rs = connection.createStatement().executeQuery("SELECT " + question.getId() + " as q, " + perspective.getId() 
					+ " as p FROM rawdata WHERE " + about.getTimeSeriesShortColumn() + " = " + timepoint.getValue());
			
			int[][] crosstabsdata = new int[perspective.getScale().getOptionCount()][question.getScale().getOptionCount()]; //TODO: switch indexing??
			
			int ignore = 0;
			while(rs.next()) {
				try {
					Double qvalue = rs.getDouble("q");
					int qindex = question.getScale().matchIndex(qvalue);
					Double pvalue = rs.getDouble("p");
					int pindex = perspective.getScale().matchIndex(pvalue);
					
					crosstabsdata[pindex][qindex]++;
				} catch (Exception e) {
					if(e.getMessage().equalsIgnoreCase("Ignore Exception")) {
						//TODO handle ignore?
						ignore++;
					} else {
						e.printStackTrace();
					}
				}
			}
			System.out.print(" <crossig:" + ignore + "> ");
			return crosstabsdata;
		
		} else {
			//TODO handle cases where there is no scale
			return null;
		}
		
	}
	
	public int[] frequencies(MetaQuestion question, MetaTimepointShort timepoint) throws SQLException {
//		PreparedStatement stmt = connection.prepareStatement("SELECT ? FROM rawdata WHERE ? = ?");
//		stmt.setString(1, question.getId());
//		stmt.setString(2, about.getTimeSeriesShortColumn());
//		stmt.setDouble(3, timepoint.getValue());
//		ResultSet rs = stmt.executeQuery();
		ResultSet rs = connection.createStatement().executeQuery("SELECT " + question.getId() + " FROM rawdata WHERE " + about.getTimeSeriesShortColumn() + " = " + timepoint.getValue());
		
		int[] frequencies = new int[question.getScale().getOptionCount()];
		
		int ignore = 0;
		while(rs.next()) {
			Double value = rs.getDouble(question.getId());
			try {
				frequencies[question.getScale().matchIndex(value)]++;
			} catch (Exception e) {
				if(e.getMessage().equalsIgnoreCase("Ignore Exception")) {
					//TODO handle ignore?
					ignore++;
				} else {
					e.printStackTrace();
				}
			}
		}
		System.out.print(" <freqig:" + ignore + "> ");
		return frequencies;
	}
	
}


