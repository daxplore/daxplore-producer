/**
 * 
 */
package org.daxplore.producer.daxplorelib.calc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.daxplore.producer.daxplorelib.About;
import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreWarning;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaScale;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort;
import org.daxplore.producer.daxplorelib.raw.RawData.RawDataManager;
import org.daxplore.producer.daxplorelib.raw.RawMetaQuestion.RawMetaManager;

import com.google.common.base.Joiner;

public class StatsCalculation {

	private About about;
	private RawMetaManager rawMetaManager;
	private RawDataManager rawDataManager;
	
	public StatsCalculation(About about, RawMetaManager rawMetaManager, RawDataManager rawDataManager) {
		this.about = about;
		this.rawMetaManager = rawMetaManager;
		this.rawDataManager = rawDataManager;
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
	public BarStats calculateData(MetaQuestion question, MetaQuestion perspective, int lowerLimit) throws DaxploreWarning, DaxploreException {
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
						double globalMean = question.getMetaMean().useMeanReferenceValue() ? question.getMetaMean().getGlobalMean() : Double.NaN;
						stats.addMeanData(0, means.get(0), means.get(1)[0], globalMean, intCounts, (int)means.get(3)[0]);
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
							double globalMean = question.getMetaMean().useMeanReferenceValue() ? question.getMetaMean().getGlobalMean() : Double.NaN;
							stats.addMeanData(0, means.get(0), means.get(1)[0], globalMean, intCounts, (int)means.get(3)[0]);
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
		} catch (NullPointerException | ArrayIndexOutOfBoundsException | DaxploreException e) {
			throw new DaxploreException("Failed to generate data for question: " + question.getColumn() + ", perspective: " + perspective.getColumn(), e);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<double[]> meanCalc(MetaQuestion question, MetaQuestion perspective, MetaTimepointShort timepoint, int lowerlimit) throws DaxploreException, DaxploreWarning {
		if(perspective.getScale() == null) { 
			throw new DaxploreException("meanCalc: perspective is missing scale");
		}
		
		// return values in order
		double[] meandata = new double[perspective.getScale().getOptionCount()];
		double[] meanall = new double[1];
		double[] count = new double[perspective.getScale().getOptionCount()];
		double[] countall = new double[1];
		
		int questionColIndex = rawMetaManager.getIndexOfColumn(question.getColumn());
		int perspectiveColIndex = rawMetaManager.getIndexOfColumn(perspective.getColumn());
		int rawTimePointIndex = rawMetaManager.getIndexOfColumn(about.getTimeSeriesShortColumn());
		
		MetaScale perspectiveScale = perspective.getScale();
		Object[][] rawdataTable = rawDataManager.getDataTable();
		
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
	private int[][] freqCalc(MetaQuestion question, MetaQuestion perspective, MetaTimepointShort timepoint, int lowerlimit) throws DaxploreWarning, DaxploreException {
		if(question.getScale() == null && perspective.getScale() == null) { 
			return null;
		}
		
		int[][] crosstabsdata = new int[perspective.getScale().getOptionCount()][question.getScale().getOptionCount()];
		
		int[] totals = new int[perspective.getScale().getOptionCount()];	
		
		int questionColIndex = rawMetaManager.getIndexOfColumn(question.getColumn());
		int perspectiveColIndex = rawMetaManager.getIndexOfColumn(perspective.getColumn());
		int rawTimePointIndex = rawMetaManager.getIndexOfColumn(about.getTimeSeriesShortColumn());
		
		MetaScale questionScale = question.getScale();
		MetaScale perspectiveScale = perspective.getScale();
		Object[][] rawdataTable = rawDataManager.getDataTable();
		
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
	private int[] frequencies(MetaQuestion question, MetaTimepointShort timepoint, int lowerlimit) throws DaxploreWarning, DaxploreException {
		int[] frequencies = new int[question.getScale().getOptionCount()];
		int total = 0;

		MetaScale scale = question.getScale();
		
		int questionColIndex = rawMetaManager.getIndexOfColumn(question.getColumn());
		int rawTimePointIndex = rawMetaManager.getIndexOfColumn(about.getTimeSeriesShortColumn());
		
		Object[][] rawdataTable = rawDataManager.getDataTable();
		
		for(int row = 0; row < rawdataTable[0].length; row++) {
			if(timepoint == null || (Double)rawdataTable[rawTimePointIndex][row] == timepoint.getValue()) { //TODO timepoint: support TEXT/REAL for value
				int index = scale.getOptionIndex(rawdataTable[questionColIndex][row]);
				if(index != -1) {
					frequencies[index]++;
					total++;
				}
			}
		}

		if(total < lowerlimit) {
			throw new DaxploreWarning("Could not generate total frequencies for question: " + question.getColumn());
		}
		
		return frequencies;
	}
	
	
	
}


