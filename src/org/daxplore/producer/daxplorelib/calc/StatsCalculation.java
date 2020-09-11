/**
 * 
 */
package org.daxplore.producer.daxplorelib.calc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
	public BarStats calculateData(MetaQuestion question, List<MetaQuestion> perspectives, int lowerLimit) throws DaxploreWarning, DaxploreException {
		try {
			BarStats stats = new BarStats(question, perspectives);
			int[][] crosstabs;
			int[] frequencies;
			List<double[]> means; // <mean, allmean, count>
			
			List<String> warnings = new LinkedList<String>();
			
			switch (about.getTimeSeriesType()) {
			case NONE:
				if(question.useFrequencies()) {
					try {
						crosstabs = freqCalc(question, perspectives, null, lowerLimit);
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
						means = meanCalc(question, perspectives.get(0), null, lowerLimit); //TODO !!!
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
				List<MetaTimepointShort> perspectiveTimes = perspectives.get(0).getTimepoints(); // TODO !!!
				
				LinkedList<MetaTimepointShort> commonTimes = new LinkedList<>();
				
				for(MetaTimepointShort qTime: questionTimes) {
					if(perspectiveTimes.contains(qTime)) {
						commonTimes.add(qTime);
					}
				}
				
				for(MetaTimepointShort timepoint : commonTimes) {
					if (question.useFrequencies() || question.useDichotomizedLine()) {
						try {
							crosstabs = freqCalc(question, perspectives, timepoint, lowerLimit);
							frequencies = frequencies(question, timepoint, lowerLimit);
							stats.addFrequencyData(timepoint.getTimeindex(), crosstabs, frequencies);
						} catch (DaxploreWarning e) {
							warnings.add(e.getMessage());
						}
					}
					if(question.useMean()) {
						try {
							means = meanCalc(question, perspectives.get(0), timepoint, lowerLimit); // TODO !!!
							double[] counts = means.get(2);
							int[] intCounts = new int[counts.length];
							for(int i=0; i<counts.length; i++){
								intCounts[i] = (int)counts[i];
							}
							stats.addMeanData(timepoint.getTimeindex(), means.get(0), means.get(1)[0], intCounts, (int)means.get(3)[0]);
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
			if (perspectives.size() == 1) {
				throw new DaxploreException("Failed to generate data for question: " + question.getColumn() + ", perspective: " + perspectives.get(0).getColumn(), e);
			} else {
				throw new DaxploreException("Failed to generate data for question: " + question.getColumn() + ", perspectives: " + perspectives.get(0).getColumn() + ", " + perspectives.get(1).getColumn(), e);
			}
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
		int rawTimePointIndex = timepoint == null ? -1 : rawMetaManager.getIndexOfColumn(about.getTimeSeriesShortColumn());
		
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
	
	/**
	 * 
	 * @param question
	 * @param perspectives should have 1 or 2 perspectives
	 * @param timepoint
	 * @param lowerlimit
	 * @return
	 * @throws DaxploreWarning
	 * @throws DaxploreException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int[][] freqCalc(MetaQuestion question, List<MetaQuestion> perspectives, MetaTimepointShort timepoint, int lowerlimit) throws DaxploreWarning, DaxploreException {
		if(question.getScale() == null) { 
			return null;
		}
		for (MetaQuestion p : perspectives) {
			 if(p.getScale() == null) {
				 return null;
			 }
		}
		
		if (perspectives.size() == 0 || perspectives.size() > 2) {
			throw new DaxploreException("Invalid number of perspectives: " + perspectives.size());
		}
		
		boolean hasSecondaryPerspective = perspectives.size() == 2;
		
		int perspectiveOptionCount = perspectives
			.stream()
			.mapToInt(mq -> mq.getScale().getOptionCount())
			.reduce(1, (q1, q2) -> q1*q2);
		
		int[][] crosstabsdata = new int[perspectiveOptionCount][question.getScale().getOptionCount()];
		
		int[] totals = new int[perspectiveOptionCount];	
		
		int questionColIndex = rawMetaManager.getIndexOfColumn(question.getColumn());
	
		int[] perspectiveColIndices = new int[perspectives.size()];
		perspectiveColIndices[0] = rawMetaManager.getIndexOfColumn(perspectives.get(0).getColumn());
		if (hasSecondaryPerspective) {
			perspectiveColIndices[1] = rawMetaManager.getIndexOfColumn(perspectives.get(1).getColumn());
		}

		int rawTimePointIndex = timepoint == null ? -1 : rawMetaManager.getIndexOfColumn(about.getTimeSeriesShortColumn());
		
		MetaScale questionScale = question.getScale();
		List<MetaScale> perspectiveScales = perspectives
				.stream()
				.map(MetaQuestion::getScale)
				.collect(Collectors.toList());
		Object[][] rawdataTable = rawDataManager.getDataTable();
		
		for(int row = 0; row < rawdataTable[0].length; row++) {
			if(timepoint == null || ((Double)rawdataTable[rawTimePointIndex][row]) == timepoint.getValue()) { //TODO timepoint: support TEXT/REAL for value
				int qindex = questionScale.getOptionIndex(rawdataTable[questionColIndex][row]);
				Object p1Option = rawdataTable[perspectiveColIndices[0]][row];
				int pindex1 = perspectiveScales.get(0).getOptionIndex(p1Option);
				
				if(qindex == -1 || pindex1 == -1) {
					continue;
				}
				
				int combinedPerspectiveIndex = pindex1;
				if (hasSecondaryPerspective) {
					Object p2Option = rawdataTable[perspectiveColIndices[1]][row];
					int pindex2 = perspectiveScales.get(1).getOptionIndex(p2Option);
					if (pindex2 == -1) {
						continue;
					}
					combinedPerspectiveIndex = pindex1 * perspectiveScales.get(1).getOptionCount() + pindex2;
				}
				totals[combinedPerspectiveIndex]++;
				crosstabsdata[combinedPerspectiveIndex][qindex]++;
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
			if (perspectives.size() == 1) {
				throw new DaxploreWarning("Could not generate frequency data for question: " + question.getColumn() + " and perspective: " + perspectives.get(0).getColumn());
			} else {
				throw new DaxploreWarning("Could not generate frequency data for question: " + question.getColumn() + " and perspectives: " + perspectives.get(0).getColumn() + ", " + perspectives.get(1).getColumn());
			}
		}
		
		return crosstabsdata;
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int[] frequencies(MetaQuestion question, MetaTimepointShort timepoint, int lowerlimit) throws DaxploreWarning, DaxploreException {
		int[] frequencies = new int[question.getScale().getOptionCount()];
		int total = 0;

		MetaScale scale = question.getScale();
		
		int questionColIndex = rawMetaManager.getIndexOfColumn(question.getColumn());
		int rawTimePointIndex = timepoint == null ? -1 : rawMetaManager.getIndexOfColumn(about.getTimeSeriesShortColumn());
		
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


