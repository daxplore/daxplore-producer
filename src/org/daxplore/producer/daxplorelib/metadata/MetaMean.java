package org.daxplore.producer.daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
			"CREATE TABLE metamean (questionid TEXT NOT NULL, includedvalues TEXT NOT NULL, FOREIGN KEY(questionid) REFERENCES metaquestion(id))",
			"metamean");
	
	public static class MetaMeanManager {
		
		private Connection connection;
		
		private Map<String, MetaMean> metaMeanMap = new HashMap<>();
		private LinkedList<MetaMean> toBeAdded= new LinkedList<>();
		private Map<String, MetaMean> toBeRemoved = new HashMap<>();

		public MetaMeanManager(Connection connection) throws SQLException {
			this.connection = connection;
			
			if(!SQLTools.tableExists(table.name, connection)) {
				try(Statement stmt = connection.createStatement()) {
					stmt.executeUpdate(table.sql);
				}
			}
		}
		
		public MetaMean get(String id) throws DaxploreException, SQLException {
			if(metaMeanMap.containsKey(id)) {
				return metaMeanMap.get(id);
			} else if(toBeRemoved.containsKey(id)) {
				throw new DaxploreException("No metamean with id '"+id+"'");
			}
			
			Set<Double> includedValues = new HashSet<Double>();
			try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metamean WHERE questionid = ?")) {
				stmt.setString(1, id);
				
				try(ResultSet rs = stmt.executeQuery()) {
					if(!rs.next()) {
						throw new IllegalArgumentException("QuestionID does not exist?");
					}
					
					String includedValuesJson = rs.getString("includedvalues");
					Gson gson = new Gson();
					includedValues = Sets.newHashSet(gson.fromJson(includedValuesJson, Double[].class));
				}
			}
			
			MetaMean metaMean = new MetaMean(id, includedValues, false);
			metaMeanMap.put(id, metaMean);
			return metaMean;
		}
		
		public MetaMean create(String id, Set<Double> includedValues) {
			MetaMean metaMean = new MetaMean(id, includedValues, true);
			toBeAdded.add(metaMean);
			metaMeanMap.put(id, metaMean);
			return metaMean;
		}
		
		public void remove(String id) {
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
				PreparedStatement addMetaMeanStmt = connection.prepareStatement("INSERT INTO metamean (questionid, includedvalues) VALUES (?, ?)");
				PreparedStatement updateMetaMeanStmt = connection.prepareStatement("UPDATE metamean SET includedvalues = ? WHERE questionid = ?");
				PreparedStatement deleteMetaMeanStmt = connection.prepareStatement("DELETE FROM metamean WHERE questionid = ?");
			) {
				int nNew = 0, nModified = 0, nRemoved = 0;
				
				for(MetaMean metaMean : toBeAdded) {
					nNew++;
					addMetaMeanStmt.setString(1, metaMean.questionid);
					String valuesJson = gson.toJson(metaMean.includedValues.toArray(), Double[].class);
					addMetaMeanStmt.setString(2, valuesJson);
					addMetaMeanStmt.addBatch();
					metaMean.modified = false;
				}
				addMetaMeanStmt.executeBatch();
				
				for(MetaMean metaMean : metaMeanMap.values()) {
					if(metaMean.modified){
						String valuesJson = gson.toJson(metaMean.includedValues.toArray(), Double[].class);
						updateMetaMeanStmt.setString(1, valuesJson);
						updateMetaMeanStmt.setString(2, metaMean.questionid);
						updateMetaMeanStmt.addBatch();
					}
				}
				updateMetaMeanStmt.executeBatch();
				
				for(MetaMean metaMean : toBeRemoved.values()) {
					deleteMetaMeanStmt.setString(1, metaMean.questionid);
					deleteMetaMeanStmt.addBatch();
					// TODO Auto-generated method stub	}
					deleteMetaMeanStmt.executeBatch();
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
	
	private String questionid;
	private Set<Double> includedValues;
	private boolean modified;
	
	private MetaMean(String questionid, Set<Double> includedValues, boolean setNew) {
		this.questionid = questionid;
		this.includedValues = new TreeSet<>(includedValues); //TreeSet gives natural ordering
		modified = setNew;
	}
		
	public String getQuestionId() {
		return questionid;
	}
	
	public Set<Double> getIncludedValues() {
		return ImmutableSet.copyOf(includedValues);
	}
	
	public void setIncludedValues(Collection<Double> values) {
		includedValues.clear();
		includedValues.addAll(values);
	}
	
	public void addIncludedValue(double value) {
		includedValues.add(value);
	}
	
	public void removeIncludedValue(double value) {
		includedValues.remove(value);
	}
}
