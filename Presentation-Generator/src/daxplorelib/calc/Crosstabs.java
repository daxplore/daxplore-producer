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
	
	public void Crosstabs2(MetaQuestion question, MetaQuestion perspective) throws SQLException {
		List<MetaTimepointShort> questionTimes = question.getTimepoints();
		List<MetaTimepointShort> perspectiveTimes = perspective.getTimepoints();
		
		LinkedList<MetaTimepointShort> commonTimes = new LinkedList<MetaTimepointShort>();
		
		for(MetaTimepointShort qTime: questionTimes) {
			if(perspectiveTimes.contains(qTime)) {
				commonTimes.add(qTime);
			}
		}
		
		for(MetaTimepointShort timepoint: commonTimes) {
			Crosstabs2(question, perspective, timepoint);
		}
		
	}
	
	public void Crosstabs2(MetaQuestion question, MetaQuestion perspective, MetaTimepointShort timepoint) throws SQLException {
		if(question.getScale() != null && perspective.getScale() != null) {
			//TODO: handle case where scale only has ignore
			
			PreparedStatement stmt1 = connection.prepareStatement("SELECT ?, ? FROM rawdata WHERE ? = ?");
			stmt1.setString(1, question.getId());
			stmt1.setString(2, perspective.getId());
			stmt1.setString(3, about.getTimeSeriesShortColumn()); //TODO get timepointshortcolumn
			stmt1.setDouble(4, timepoint.getValue());
			ResultSet rs = stmt1.executeQuery();
			
			int[][] crosstabsdata = new int[question.getScale().getOptions().size()][perspective.getScale().getOptions().size()];
			
			while(rs.next()) {
				try {
					Double qvalue = rs.getDouble(question.getId());
					int qindex = question.getScale().matchIndex(qvalue);
					Double pvalue = rs.getDouble(perspective.getId());
					int pindex = perspective.getScale().matchIndex(pvalue);
					
					crosstabsdata[qindex][pindex]++;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		
		} else {
			//TODO handle cases where there is no scale
		}
		
	}
	
}


