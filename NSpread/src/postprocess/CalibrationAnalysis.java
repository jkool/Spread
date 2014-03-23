package postprocess;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Map;
import java.util.TreeMap;

public class CalibrationAnalysis {

	private String outputFolder = "C:/Temp/Spread_Output";
	private String table = "stats";
	private String resampleField = "Replicate";
	private String criterionField = "K_standard";
	private String errorType = "Quantity_disagreement";
	private String spreadRateField = "Rate";
	private String spreadDistanceField = "Distance";
	private double percentile = 0.1d;
	private double errorCutoff = 0.03d;
	private double performanceCriterion = .8d;
	private String outputFile = "calibration.csv";
	private List<String> speciesList;

	public void go() {

		System.out.println("\nRunning calibration analysis...\t");
		
		Iterator<String> it = speciesList.iterator();
		while(it.hasNext()){
			
		String species = it.next();
		
		String trimtable = species + "_" + removeExtension(table);
		
		try {

			Properties props = new Properties();
			props.put(
					"columnTypes",
					"Int,Double,Double,Int,Int,Double,Double,Double,Double,Double,Double,Double,Double,Double,Double,Double");

			Class.forName("org.relique.jdbc.csv.CsvDriver");

			// Identifying the data types for the fields

			// Connect to the csv file

			Connection conn = DriverManager.getConnection("jdbc:relique:csv:"
					+ outputFolder, props);

			// Create a statement so we can read the unique resampleIDs

			Statement stmt = conn.createStatement();

			ResultSet numset = stmt.executeQuery("SELECT COUNT(*) FROM "
					+ trimtable);
			numset.next();
			int nrows = numset.getInt(1);
			numset.close();

			int trimrows = (int) ((double) nrows * percentile);

			ResultSet repset = stmt.executeQuery("SELECT DISTINCT "
					+ resampleField + " FROM " + trimtable);
			int repct = 0;
			while (repset.next()) {
				repct++;
			}
			repset.close();

			// Ideally, here we'd retrieve a nested table, and run a GROUP
			// BY on it, but csvjdbc doesn't support nested/temporary
			// tables.
			// One option might be to write an in-memory adaptor to treat a
			// ResultSet like a table, but the easier thing for now is to
			// just iterate over the table and use Maps to do the grouping.

			String sql = "SELECT " + spreadRateField + ","
					+ spreadDistanceField + "," + errorType + " FROM " + trimtable
					+ " ORDER BY " + criterionField + " DESC LIMIT " + trimrows;

			ResultSet resampleSet = stmt.executeQuery(sql);

			Map<Double, Map<Double, Integer>> map = process(resampleSet, repct);

			writeToFile(map, outputFolder + "/" + species + "_" + removeExtension(outputFile) + ".csv");

			// Close resources and notify that replicate is complete.

			resampleSet.close();

			stmt.close();
			conn.close();

			// Basic exception handling

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		}
	}

	private Map<Double, Map<Double, Integer>> process(ResultSet resampleSet,
			int repct) throws SQLException {
		Map<Double, Map<Double, Integer>> map = new TreeMap<Double, Map<Double, Integer>>();

		while (resampleSet.next()) {

			double err = resampleSet.getDouble(errorType);
			if (err >= errorCutoff) {
				continue;
			}

			double spread = resampleSet.getDouble(spreadRateField);
			double distance = resampleSet.getDouble(spreadDistanceField);

			if (!map.containsKey(spread)) {
				Map<Double, Integer> insert = new TreeMap<Double, Integer>();
				insert.put(distance, 1);
				map.put(spread, insert);
			} else {
				Map<Double, Integer> retrieve = map.get(spread);
				if (!retrieve.containsKey(distance)) {
					retrieve.put(distance, 1);
				} else {
					retrieve.put(distance, retrieve.get(distance) + 1);
				}
			}
		}

		// Trim map down using requiredReplicates

		int requiredReplicates = (int) (performanceCriterion * (double) repct);

		// First iterate over resampleIDs

		Iterator<Double> it = map.keySet().iterator();
		while (it.hasNext()) {
			Double dbl = it.next();

			// Then iterate over the pair values

			Map<Double, Integer> submap = map.get(dbl);
			Iterator<Double> it2 = submap.keySet().iterator();
			while (it2.hasNext()) {
				Double val = it2.next();
				if (submap.get(val) < requiredReplicates) {
					it2.remove();
				}
			}
		}

		// Eliminate empty tables

		it = map.keySet().iterator();

		while (it.hasNext()) {
			Double dbl = it.next();
			if (map.get(dbl).isEmpty()) {
				it.remove();
			}
		}
		return map;
	}

	/**
	 * Writes a two field map to csv
	 * 
	 * @param map
	 *            - The Map containing the data values to be written.
	 * @param outputFile
	 *            - String containing the path where the output file should be
	 *            written
	 */

	private void writeToFile(Map<Double, Map<Double, Integer>> map,
			String outputFile) {

		if (map.size() == 0) {
			System.out
					.println("\n\nWARNING:  No Experiments match the required calibration criteria.  Calibration file was not written.\n");
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {

			// Do we want to add a header?
			Iterator<Double> it1 = map.keySet().iterator();
			while (it1.hasNext()) {
				double spreadRate = it1.next();
				Map<Double, Integer> retrieve = map.get(spreadRate);
				Iterator<Double> it2 = retrieve.keySet().iterator();
				while (it2.hasNext()) {
					double spreadDistance = it2.next();
					bw.write(spreadRate + "," + spreadDistance + ","
							+ retrieve.get(spreadDistance) + "\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Getters and setters
	// /////////////////////////////////////////////////////////////////////////////////

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public String getTable() {
		return table;
	}

	public void setStatsTable(String table) {
		this.table = table;
	}

	public String getResampleField() {
		return resampleField;
	}

	public void setResampleField(String resampleField) {
		this.resampleField = resampleField;
	}

	public String getCriterionField() {
		return criterionField;
	}

	public void setCriterionField(String criterionField) {
		this.criterionField = criterionField;
	}

	public String getErrorType() {
		return errorType;
	}

	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

	public String getSpreadRateField() {
		return spreadRateField;
	}

	public void setSpreadRateField(String spreadRateField) {
		this.spreadRateField = spreadRateField;
	}

	public String getSpreadDistanceField() {
		return spreadDistanceField;
	}

	public void setSpreadDistanceField(String spreadDistanceField) {
		this.spreadDistanceField = spreadDistanceField;
	}

	public double getPercentile() {
		return percentile;
	}

	public void setPercentile(double percentile) {
		this.percentile = percentile;
	}

	public double getErrorCutoff() {
		return errorCutoff;
	}

	public void setErrorCutoff(double errorCutoff) {
		this.errorCutoff = errorCutoff;
	}

	public double getPerformanceCriterion() {
		return performanceCriterion;
	}

	public void setPerformanceCriterion(double performanceCriterion) {
		this.performanceCriterion = performanceCriterion;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
	
	private static String removeExtension(String s) {

	    String separator = System.getProperty("file.separator");
	    String filename;

	    // Remove the path upto the filename.
	    int lastSeparatorIndex = s.lastIndexOf(separator);
	    if (lastSeparatorIndex == -1) {
	        filename = s;
	    } else {
	        filename = s.substring(lastSeparatorIndex + 1);
	    }

	    // Remove the extension.
	    int extensionIndex = filename.lastIndexOf(".");
	    if (extensionIndex == -1)
	        return filename;

	    return filename.substring(0, extensionIndex);
	}
	
	public void setSpeciesList(List<String> speciesList){
		this.speciesList = speciesList;
	}
}