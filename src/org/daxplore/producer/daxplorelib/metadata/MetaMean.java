package org.daxplore.producer.daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreTable;
import org.daxplore.producer.daxplorelib.SQLTools;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

public class MetaMean {

	private static final DaxploreTable table = new DaxploreTable(
			"CREATE TABLE metamean ("
			+ "questionid INTEGER NOT NULL,"
			+ "excludedvalues TEXT NOT NULL,"
			+ "useglobalmean INTEGER,"
			+ "globalmean REAL,"
			+ "FOREIGN KEY(questionid) REFERENCES metaquestion(id))",
			"metamean");
	
	public static class MetaMeanManager {
		private Connection connection;
		private Map<Integer, MetaMean> metaMeanMap = new HashMap<>();
		private LinkedList<MetaMean> toBeAdded= new LinkedList<>();
		private Map<Integer, MetaMean> toBeRemoved = new HashMap<>();

		public MetaMeanManager(Connection connection) throws SQLException {
			this.connection = connection;
			
			if(!SQLTools.tableExists(table.name, connection)) {
				try(Statement stmt = connection.createStatement()) {
					stmt.executeUpdate(table.sql);
				}
			}
		}
		
		public MetaMean get(int id) throws DaxploreException, SQLException {
			if(metaMeanMap.containsKey(id)) {
				return metaMeanMap.get(id);
			} else if(toBeRemoved.containsKey(id)) {
				throw new DaxploreException("No metamean with id '"+id+"'");
			}
			
			Set<Double> excludedValues = new HashSet<Double>();
			boolean useGlobalMean;
			double globalMean;
			try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metamean WHERE questionid = ?")) {
				stmt.setInt(1, id);
				
				try(ResultSet rs = stmt.executeQuery()) {
					if(!rs.next()) {
						throw new IllegalArgumentException("QuestionID does not exist?");
					}
					
					String excludedValuesJson = rs.getString("excludedvalues");
					Gson gson = new Gson();
					excludedValues = Sets.newHashSet(gson.fromJson(excludedValuesJson, Double[].class));
					
					if(rs.getInt("useglobalmean") != 0) {
						useGlobalMean = true;
					} else {
						useGlobalMean = false;
					}
					
					
					globalMean = rs.getDouble("globalMean");
					if(rs.wasNull()) {
						globalMean = Double.NaN;
					}
					
				}
			}
			
			MetaMean metaMean = new MetaMean(id, excludedValues, useGlobalMean, globalMean, false);
			metaMeanMap.put(id, metaMean);
			return metaMean;
		}
		
		public MetaMean create(int id, Set<Double> excludedValues, boolean useGlobalMean, double globalMean) {
			MetaMean metaMean = new MetaMean(id, excludedValues, useGlobalMean, globalMean, true);
			toBeAdded.add(metaMean);
			metaMeanMap.put(id, metaMean);
			return metaMean;
		}
		
		public void remove(int id) {
			MetaMean metaMean = metaMeanMap.remove(id);
			toBeAdded.remove(metaMean);
			toBeRemoved.put(id, metaMean);
		}
		
		public int getUnsavedChangesCount() {
			int nModified = 0;
			for(MetaMean metaMean : metaMeanMap.values()) {
				if(metaMean.modified) {
					nModified++;
				}
			}			
			return toBeRemoved.size() + nModified;
		}
		
		public void saveAll() throws SQLException {
			Gson gson = new Gson();
			try (
				PreparedStatement addMetaMeanStmt = connection.prepareStatement("INSERT INTO metamean (questionid, excludedvalues, useglobalmean, globalmean) VALUES (?, ?, ?, ?)");
				PreparedStatement updateMetaMeanStmt = connection.prepareStatement("UPDATE metamean SET excludedvalues = ?, useglobalmean = ?, globalmean = ? WHERE questionid = ?");
				PreparedStatement deleteMetaMeanStmt = connection.prepareStatement("DELETE FROM metamean WHERE questionid = ?");
			) {
				int nNew = 0, nModified = 0, nRemoved = 0;
				
				for(MetaMean metaMean : toBeAdded) {
					nNew++;
					addMetaMeanStmt.setInt(1, metaMean.questionid);
					String valuesJson = gson.toJson(metaMean.excludedValues.toArray(), Double[].class);
					addMetaMeanStmt.setString(2, valuesJson);
					addMetaMeanStmt.setInt(3, metaMean.useGlobalMean ? 1 : 0);
					if(Double.isNaN(metaMean.globalMean)) {
						addMetaMeanStmt.setNull(4, Types.DOUBLE);
					} else {
						addMetaMeanStmt.setDouble(4, metaMean.globalMean);
					}
					addMetaMeanStmt.addBatch();
					nNew++;
				}
				addMetaMeanStmt.executeBatch();
				
				for(MetaMean metaMean : metaMeanMap.values()) {
					if(metaMean.modified){
						String valuesJson = gson.toJson(metaMean.excludedValues.toArray(), Double[].class);
						updateMetaMeanStmt.setString(1, valuesJson);
						updateMetaMeanStmt.setInt(2, metaMean.useGlobalMean ? 1 : 0);
						if(Double.isNaN(metaMean.globalMean)) {
							updateMetaMeanStmt.setNull(3, Types.DOUBLE);
						} else {
							updateMetaMeanStmt.setDouble(3, metaMean.globalMean);
						}
						updateMetaMeanStmt.setInt(4, metaMean.questionid);
						updateMetaMeanStmt.addBatch();
						nModified++;
					}
				}
				updateMetaMeanStmt.executeBatch();
				
				for(MetaMean metaMean : toBeRemoved.values()) {
					deleteMetaMeanStmt.setInt(1, metaMean.questionid);
					deleteMetaMeanStmt.addBatch();
					// TODO Auto-generated method stub	}
					deleteMetaMeanStmt.executeBatch();
					nRemoved++;
				}
				
				if(nModified != 0 || nNew != 0 || nRemoved != 0) {
					String logString = String.format("MetaMean: Saved %d (%d new), %d removed", nModified, nNew, nRemoved);
					Logger.getGlobal().log(Level.INFO, logString);
				}
			}
			
			// after the batches are executed successfully
			toBeAdded.clear();
			toBeRemoved.clear();
			for(MetaMean metaMean : metaMeanMap.values()) {
				metaMean.modified = false;
			}
		}

		public void discardChanges() {
			metaMeanMap.clear();
			toBeAdded.clear();
			toBeRemoved.clear();
		}
	}
	
	private int questionid;
	private TreeSet<Double> excludedValues = new TreeSet<Double>(); //TreeSet gives natural ordering
	private boolean modified;
	private boolean useGlobalMean;
	private double globalMean;
	
	private MetaMean(int questionid, Set<Double> excludedValues, boolean useGlobalMean, double globalMean, boolean setNew) {
		this.questionid = questionid;
		if(excludedValues != null) {
			this.excludedValues.addAll(excludedValues);
		}
		this.useGlobalMean = useGlobalMean;
		this.globalMean = globalMean;
		modified = setNew;
	}
		
	public int getQuestionId() {
		return questionid;
	}
		
	public boolean isExcluded(Object value) {
		if(value != null && value instanceof Double && !Double.isNaN((Double)value)) {
			return excludedValues.contains(value);
		}
		return true;
	}
	
	public Set<Double> getExcludedValues() {
		return ImmutableSet.copyOf(excludedValues);
	}
	
	public void setExcludedValues(Collection<Double> values) {
		excludedValues.clear();
		excludedValues.addAll(values);
		modified = true;
	}
	
	public void addExcludedValue(double value) {
		modified |= excludedValues.add(value);
	}
	
	public void removeExcludedValue(double value) {
		modified |= excludedValues.remove(value);
	}
	
	public void setUseGlobalMean(boolean useGlobalMean) {
		this.useGlobalMean = useGlobalMean;
		modified = true;
	}
	
	public boolean useGlobalMean() {
		return useGlobalMean;
	}
	
	public double getGlobalMean() {
		return globalMean;
	}

	public void setGlobalMean(double globalMean) {
		this.globalMean = globalMean;
		modified = true;
	}
}
