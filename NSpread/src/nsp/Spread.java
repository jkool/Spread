package nsp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import postprocess.CalibrationAnalysis;

import nsp.impl.Disperser_Continuous2D;
import nsp.impl.RasterMosaic;
import nsp.impl.output.ExperimentWriter_Text;
import nsp.impl.output.MosaicWriter_Raster;
import nsp.impl.output.StatsWriter_Text;
import nsp.impl.process.Process_Containment;
import nsp.impl.process.Process_Costing;
import nsp.impl.process.Process_Dispersal;
import nsp.impl.process.Process_GroundControl;
import nsp.impl.process.Process_Growth;
import nsp.impl.process.Process_Infestation;
import nsp.impl.process.Process_Monitor;
import nsp.impl.random.RandomGenerator_Exponential;
import nsp.impl.random.RandomGenerator_Kernel;
import nsp.impl.random.RandomGenerator_Poisson;
import nsp.impl.random.RandomGenerator_Uniform;

/**
 * Principal class and entry point for running the SPREAD model. This class is
 * mainly used to interpret input from the Properties file, set of the requisite
 * objects, and generate multiple Experiments according to the input parameters.
 */

public class Spread {

	private Mosaic mosaic;
	private MosaicWriter mosaicWriter;
	private static Properties properties = new Properties();
	private boolean overwrite = false;
	private boolean savePropertiesFile = true;
	private boolean writeHeader = true;

	public static void main(String[] args) {

		// If no arguments are passed, then prompt the user with usage.

		if (args == null || args.length == 0) {
			System.out.println("Usage:  spread <path to parameters file>");
			return;
		}

		// We can add console queries here if desired. ************************

		// Ensure that the parameters file actually exists.

		File parameters = new File(args[0]);
		if (!parameters.exists()) {
			System.out
					.println("Parameter file: "
							+ args[0]
							+ " could not be found.  Please check the path and file name.");
			return;
		}

		// Attempt to load the parameters file.

		try {
			properties.load(new FileReader(new File(args[0])));
		} catch (java.io.IOException ex) {
			System.out
					.println("Parameter file: "
							+ args[0]
							+ " was found but could not be read.  Please check permissions values.");
			return;
		}

		// Begin the main routine

		Spread m = new Spread();
		m.start();
		m.shutdown();
	}

	/**
	 * Performs any necessary clean-up on the mosaic (e.g. closing files).
	 */

	public void shutdown() {
		mosaic.shutdown();
	}

	/**
	 * Initializes required objects - e.g. the Mosaic, OutputWriters etc.
	 */

	public void start() {
		// Set up the Mosaic (currently only implemented as a raster)
		mosaic = new RasterMosaic();
		Mosaic reference = new RasterMosaic();

		// Set the parameters of the mosaic (i.e. raster files)

		String species = properties.getProperty("Species");
		List<String> speciesList = parseStringArray(species);
		Set<String> unique = new HashSet<String>(speciesList);

		if (unique.size() != speciesList.size()) {
			System.out
					.println("Species names must be unique.  Please check the list for duplicates. ("
							+ (speciesList.size() - unique.size()) + " found)");
			System.exit(-1);
		}

		mosaic.setSpeciesList(speciesList);

		String presence = properties.getProperty("Presence_File");
		String age = properties.getProperty("Age_File");
		String habitat = properties.getProperty("Habitat_File");
		String ref = properties.getProperty("Reference_File");
		String mgt = properties.getProperty("Management_File");
		int mgt_frq = Integer.parseInt(properties.getProperty("Management_Frequency","1"));

		List<String> presenceList = parseStringArray(presence);
		List<String> ageList = parseStringArray(age);
		List<String> habitatList = parseStringArray(habitat);
		List<String> referenceList = parseStringArray(ref);
		List<String> mgtList = parseStringArray(mgt);

		if (speciesList.size() != presenceList.size()
				|| speciesList.size() != ageList.size()
				|| speciesList.size() != habitatList.size()
				|| speciesList.size() != referenceList.size()
			    || speciesList.size() != mgtList.size()
				) {
			System.out
					.println("Species, presence, age, habitat and reference file lists must all be the same size:");
			System.out.println("Species list size : " + speciesList.size()
					+ " " + Arrays.toString(speciesList.toArray()));
			System.out.println("Presence file list size : "
					+ presenceList.size() + " "
					+ Arrays.toString(presenceList.toArray()));
			System.out.println("Age file list size : " + ageList.size() + " "
					+ Arrays.toString(ageList.toArray()));
			System.out.println("Habitat file list size : " + habitatList.size()
					+ " " + Arrays.toString(habitatList.toArray()));
			System.out.println("Reference file list size : "
					+ referenceList.size() + " "
					+ Arrays.toString(referenceList.toArray()));
			System.out.println("Management file list size : "
					+ mgtList.size() + " "
					+ Arrays.toString(mgtList.toArray()));
			System.exit(-1);
		}

		for (int i = 0; i < speciesList.size(); i++) {

			try {
				mosaic.setPresenceMap(presenceList.get(i), speciesList.get(i));
				mosaic.setAgeMap(ageList.get(i), speciesList.get(i));
				mosaic.setHabitatMap(habitatList.get(i), speciesList.get(i));
				mosaic.setManagementMap(mgtList.get(i), speciesList.get(i));
			} catch (IOException e) {
				System.out.println(e);
				System.exit(-1);
			} catch (NullPointerException ne) {
				System.out
						.println("Missing properties in parameter file.  Ensure the file contains Presence_File, Age_File, and Habitat_File entries.");
				System.exit(-1);
			}

			reference.setSpeciesList(speciesList);

			try {
				reference.setPresenceMap(referenceList.get(i),
						speciesList.get(i));
				reference.setAgeMap(referenceList.get(i), speciesList.get(i));
				reference.setHabitatMap(habitatList.get(i), speciesList.get(i));
			} catch (IOException e) {
				System.out.println(e);
				System.exit(-1);
			} // catch (NullPointerException ne) {
				// System.out
				// .println("Missing reference file property in parameter file.  Ensure the file contains a Reference_file entry.");
				// System.exit(-1);
			// }
		}

		if (!properties.containsKey("Output_Folder")) {
			System.out
					.println("Missing output folder property in parameter file.  Ensure the file contains an Output_Folder entry.");
			System.exit(-1);
		}

		// Set the output root folder

		String outputFolder = properties.getProperty("Output_Folder");

		mosaicWriter = new MosaicWriter_Raster();
		mosaicWriter.setFolder(outputFolder);
		ExperimentWriter_Text ew = new ExperimentWriter_Text();
		StatsWriter_Text sw = new StatsWriter_Text();
		ew.setReferenceMosaic(reference);

		if (properties.containsKey("Overwrite_Output")) {
			overwrite = Boolean.parseBoolean(properties
					.getProperty("Overwrite_Output"));
		}

		if (overwrite) {
			File f = new File(outputFolder);
			if (!f.exists()) {
				f.mkdirs();
			}
		}

		if (properties.containsKey("Write_Raster_Header")) {
			writeHeader = Boolean.parseBoolean(properties
					.getProperty("Write_Raster_Header"));
			ew.setWriteRasterHeader(writeHeader);
		}

		if (properties.containsKey("Save_Properties_File")) {
			savePropertiesFile = Boolean.parseBoolean(properties
					.getProperty("Save_Properties_File"));

			// If we explicitly say to not save the properties file, but one
			// exists in the same location, then delete it to avoid confusion.
			if (!savePropertiesFile) {
				File f = new File(outputFolder + "/" + "properties.txt");
				if (f.exists()) {
					f.delete();
				}
			}
		}

		if (savePropertiesFile) {
			try {
				savePropertiesFile(outputFolder + "/" + "properties.txt");
			} catch (IOException e) {
				System.out
						.println("Unable to save properties file to "
								+ outputFolder
								+ "/"
								+ "properties.txt.  Please check the path exists, that the file is not in use, and that you have write permission.");
			}
		}

		String outputFile = properties.getProperty("Output_File");
		String outputPath = outputFolder + "/" + outputFile;

		ew.setOutputFolder(outputFolder);
		ew.setOutputFile(outputFile);

		if (!overwrite) {
			if (new File(outputPath).exists()) {
				System.out
						.println("Overwrite is currently disabled, but output file "
								+ outputPath
								+ " already exists. Please check the file, or set Overwrite_output as True in the properties file.");
				System.exit(-1);
			}
		}

		try {
			ew.open(new TreeSet<String>(speciesList));
		} catch (IOException e2) {
			System.out
					.println("Output file "
							+ ew.getOutputFile()
							+ " could not be accessed for writing.  Please check the path exists, that the file is not in use, and that you have write permission.");
			System.exit(-1);
		}

		int reps = Integer.parseInt(properties.getProperty("Replicates"));
		long startTime = Long.parseLong(properties.getProperty("Start_Time"));
		long timeIncrement = Long.parseLong(properties
				.getProperty("Step_Interval"));
		long endTime = Long.parseLong(properties.getProperty("End_Time"));

		if (endTime <= startTime) {
			System.out.println("ERROR:  End time (" + endTime
					+ ") must be greater than the start time(" + startTime
					+ ")");
			System.exit(-1);
		}

		if (timeIncrement <= 0) {
			System.out.println("ERROR:  Time increment (" + timeIncrement
					+ ") must be greater than zero.");
			System.exit(-1);
		}

		boolean printReplicates = properties.containsKey("Print_Replicates") ? Boolean
				.parseBoolean(properties.getProperty("Print_Replicates"))
				: false;

		boolean writeEachTimeStep = properties
				.containsKey("Write_Each_Time_Step") ? Boolean
				.parseBoolean(properties.getProperty("Write_Each_Time_Step"))
				: false;

		boolean writeFrequencyMap = properties
				.containsKey("Write_Frequency_Map") ? Boolean
				.parseBoolean(properties.getProperty("Write_Frequency_Map"))
				: false;

		ew.writeFrequencyMap(writeFrequencyMap);

		String distString = properties.getProperty("Distances");
		String rateString = properties.getProperty("Rates");
		
		List<double[]> kernels;
		
		if(properties.containsKey("Direction_kernel")){
			kernels = parseMultiNumericArray(properties.getProperty("Direction_Kernel"));
		}
		else{
			kernels = new ArrayList<double[]>();
			double[] k = new double[]{1,1,1,1,1,1,1,1};
			for(int i = 0; i < speciesList.size(); i++){
				kernels.add(k);
			}
		}
		
		List<double[]> distances = parseMultiNumericArray(distString);
		List<double[]> rates = parseMultiNumericArray(rateString);
		
		if (distances.size() != speciesList.size()
				|| rates.size() != speciesList.size()) {
			System.out
					.println("Dispersal parameter dimensions do not match the number of species:");
			System.out.println("Species " + speciesList.size() + " "
					+ Arrays.toString(speciesList.toArray()));
			System.out.println("Distances " + distances.size() + " "
					+ Arrays.toString(distances.toArray()));
			System.out.println("Rates " + rates.size() + " "
					+ Arrays.toString(rates.toArray()));
		}

		/*
		 * double[] scales = null;
		 * 
		 * if(properties.containsKey("Scales")){ scales =
		 * parseArrayString("Scales"); }
		 * 
		 * ew.setScales(scales);
		 */

		// Adding steps to the process chain. This makes it easy to
		// add additional steps interactively and switch order.

		List<Process> processes = new ArrayList<Process>();
		
		// Adding growth

		Process_Growth pg = new Process_Growth();

		if (!properties.containsKey("Age_Stage")) {
			System.out
					.println("Stage at Age (Age_Stage) threshold values must be provided in the properties file.  Exiting");
			System.exit(-1);
		}

		List<double[]> age_stage = parseMultiNumericArray(properties
				.getProperty("Age_Stage"));
		if (age_stage.size() != speciesList.size()) {
			System.out.println("Number of species (" + speciesList.size()
					+ ") and number of stage at age threshold arrays ("
					+ age_stage.size() + ") must match. Exiting");
			System.exit(-1);
		}

		Map<String, long[]> thresholds = new TreeMap<String, long[]>();
		for (int i = 0; i < speciesList.size(); i++) {
			long[] la = new long[age_stage.get(i).length];
			for (int j = 0; j < age_stage.get(i).length; j++) {
				la[j] = (long) age_stage.get(i)[j];
			}
			thresholds.put(speciesList.get(i), la);
		}

		pg.setThresholds(thresholds);

		// Adding dispersal
		
		Process_Dispersal pd = new Process_Dispersal();

		Map<String, Long> waitTimes = new TreeMap<String, Long>();

		for (int i = 0; i < speciesList.size(); i++) {
			waitTimes.put(speciesList.get(i), 0l);
		}

		if (properties.containsKey("Wait_Time")) {
			double[] wait_arr = parseNumericArray(properties
					.getProperty("Wait_Time"));
			if (wait_arr.length != speciesList.size()) {
				System.out
						.println("Number of wait times provided does not match the number of species.");
				System.exit(-1);
			}
			for (int i = 0; i < speciesList.size(); i++) {
				waitTimes.put(speciesList.get(i), (long) wait_arr[i]);
			}
		}

		pd.setWaitTimes(waitTimes);
		
		// Adding monitoring
		
		Process_Monitor pm = new Process_Monitor();
		pm.setContainmentCutoff(Double.parseDouble(properties.getProperty("Containment_Cutoff","500000")));
		pm.setCoreBufferSize(Double.parseDouble(properties.getProperty("Containment_Cutoff","750")));
		pm.setCheckFrequency(mgt_frq);
		List<double[]> p_discovery = parseMultiNumericArray(properties.getProperty("p_Detection"));
		Map<String, double[]> detectionMap = new TreeMap<String,double[]>();
		
		if(p_discovery.size()!=speciesList.size()){
			System.out.println("Detection probabilities array size ("+p_discovery.size()+") does not match the number of species ("+speciesList.size()+")");
			System.exit(-1);
		}
		
		for(int i =0; i < speciesList.size(); i++){
			if(age_stage.get(i).length!=p_discovery.get(i).length-1){
				System.out.println("Discovery probabilities must match the number of age thresholds plus 1.  Species " + i  + " stage thresholds :" + age_stage.get(i).length  + ", p_detection:" + p_discovery.get(i).length);
				System.exit(-1);
			}
			detectionMap.put(speciesList.get(i), p_discovery.get(i));
		}
		
		pm.setPDiscovery(detectionMap);
		
		// Adding ground control actions
		
		Process_GroundControl pgc = new Process_GroundControl();
		pgc.setCheckFrequency(mgt_frq);
		pgc.addToIgnoreList(parseStringArray(properties.getProperty("Ground_Control_Ignore")));
		
		// Adding containment actions
		
		Process_Containment pcc = new Process_Containment();
		pcc.setCheckFrequency(mgt_frq);
		pcc.addToIgnoreList(parseStringArray(properties.getProperty("Containment_Ignore")));
		
		// Adding cost accounting
		
		Process_Costing pcst = new Process_Costing();
		pcst.setCheckFrequency(mgt_frq);
		
		pcst.setContainmentCost(Double.parseDouble(properties.getProperty("Containment_Cost","7")));
		pcst.setContainmentLabour(Double.parseDouble(properties.getProperty("Containment_Labour","1")));
		pcst.setGroundControlCosts(parseNumericArray(properties.getProperty("Ground_Control_Cost","[1000,2000,4200]")));
		pcst.setGroundControlLabour(parseNumericArray(properties.getProperty("Ground_Control_Labour","[14,24,56]")));
		
		processes.add(pgc);
		processes.add(pcc);
		processes.add(pcst);
		processes.add(pg);
		processes.add(pd);
		processes.add(pm);
		
		// Adding infestation step
		
		processes.add(new Process_Infestation());
		
		boolean writeTrace = Boolean.parseBoolean(properties.getProperty("Write_Trace_Files","false"));

		// If the Run-type is Paired, then the arrays of distances and rates are
		// run as a paired set, therefore there is only one loop.

		if (properties.containsKey("Run_Type")
				&& properties.getProperty("Run_Type")
						.equalsIgnoreCase("Paired")) {

			for (int sp = 0; sp < speciesList.size(); sp++) {

				if (distances.get(sp).length != rates.get(sp).length) {
					System.out
							.println("ERROR:  Paired run type specified in properties file, but distance and rate arrays are not the same length ("
									+ distances.get(sp).length
									+ ","
									+ rates.get(sp).length
									+ " for species "
									+ sp + ").  Exiting.");
					System.exit(-1);
				}
			}

			for (int i = 0; i < distances.get(0).length; i++) {

				System.out.println("Processing pair set " + (i + 1) + " of "
						+ distances.get(0).length);

				for (int j = 0; j < speciesList.size(); j++) {

					// set up the Disperser

					Disperser_Continuous2D dc2 = new Disperser_Continuous2D();

					// instantiate the random number generators

					RandomGenerator_Exponential distanceGenerator = new RandomGenerator_Exponential();
					distanceGenerator.setLambda(1 / distances.get(j)[i]);
					RandomGenerator angleGenerator = new RandomGenerator_Uniform();

					if (properties.containsKey("Direction_Kernel")) {
						RandomGenerator_Kernel rk = new RandomGenerator_Kernel();
						rk.setRotate(true);
						
						rk.setWeights(kernels.get(j));
						angleGenerator = rk;
					}

					RandomGenerator_Poisson numberGenerator = new RandomGenerator_Poisson();
					numberGenerator.setLambda(rates.get(j)[i]);

					dc2.setAngleGenerator(angleGenerator);
					dc2.setDistanceGenerator(distanceGenerator);
					dc2.setNumberGenerator(numberGenerator);

					mosaic.setDisperser(speciesList.get(j), dc2);
				}
				// iterate through the required number of replicates

				for (int n = 0; n < reps; n++) {
					if (printReplicates) {
						System.out.println("\t\tReplicate " + (n + 1) + " of "
								+ reps);
					}
					
					Experiment e = new Experiment();
					
					double[] dist_vec = new double[speciesList.size()];
					double[] rate_vec = new double[speciesList.size()];

					for (int j = 0; j < speciesList.size(); j++) {
						dist_vec[j] = distances.get(j)[i];
						rate_vec[j] = rates.get(j)[i];
					}
					
					if(writeTrace){
						sw = new StatsWriter_Text();
						sw.setOutputFolder(outputFolder);
						sw.setDistances(dist_vec);
						sw.setRates(rate_vec);
						sw.setReplicate(n);
						String sw_output = properties.getProperty("Trace_Base_Name","TraceFile") + "_" + n;
						sw.setOutputFile(sw_output);
						try {
							sw.open(new HashSet<String>(speciesList));
							sw.setRunID(i*distances.get(0).length +n);
						} catch (IOException e1) {
							System.out.println("Could not write statistics to trace file " + outputFolder + "/" + sw_output + ".  Skipping.");
							continue;
						}
						e.setStatsWriter(sw);
						e.writeTraceFile(writeTrace);
					}
					
					e.setMosaic(mosaic.clone());
					e.setStartTime(startTime);
					e.setTimeIncrement(timeIncrement);
					e.setEndTime(endTime);
					mosaicWriter.setWriteHeader(writeHeader);
					e.setOutputWriter(mosaicWriter);
					e.setProcesses(processes);
					e.writeEachTimeStep(writeEachTimeStep);
					
					

					int id = (i * reps) + n;

					e.setIdentifier(id + "_" + n);

					ew.setDistances(dist_vec);
					ew.setRates(rate_vec);
					ew.setReplicate(n);
					ew.setID(id);

					e.setExperimentWriter(ew);

					e.run();
					
					pcst.resetCost();
					pcst.resetLabour();

				}
			}
			if(writeTrace){
				sw.close();
			}
			ew.close();
		}

		// Otherwise, we perform a calibration-type run where permutations of
		// the distance and rate arrays are used.
		// This is the default setting.

		else {
			for (int i = 0; i < distances.get(0).length; i++) {
				System.out.println("Processing distance class " + (i + 1)
						+ " of " + distances.get(0).length);
				for (int j = 0; j < rates.get(0).length; j++) {
					System.out.println("\tProcessing rate class " + (j + 1)
							+ " of " + rates.get(0).length);
					for (int k = 0; k < speciesList.size(); k++) {

						Disperser_Continuous2D dc2 = new Disperser_Continuous2D();
						RandomGenerator_Exponential distanceGenerator = new RandomGenerator_Exponential();
						distanceGenerator.setLambda(1 / distances.get(k)[i]);
						RandomGenerator angleGenerator = new RandomGenerator_Uniform();

						if (properties.containsKey("Direction_Kernel")) {
							RandomGenerator_Kernel rk = new RandomGenerator_Kernel();
							rk.setRotate(true);
							rk.setWeights(parseMultiNumericArray(properties
									.getProperty("Direction_Kernel")).get(k));
							angleGenerator = rk;
						}

						RandomGenerator_Poisson numberGenerator = new RandomGenerator_Poisson();
						numberGenerator.setLambda(rates.get(k)[j]);

						dc2.setAngleGenerator(angleGenerator);
						dc2.setDistanceGenerator(distanceGenerator);
						dc2.setNumberGenerator(numberGenerator);

						mosaic.setDisperser(speciesList.get(k), dc2);
					}

					for (int n = 0; n < reps; n++) {
						if (printReplicates) {
							System.out.println("\t\tReplicate " + (n + 1)
									+ " of " + reps);
						}
						
						Experiment e = new Experiment();
						
						double[] dist_vec = new double[speciesList.size()];
						double[] rate_vec = new double[speciesList.size()];

						for (int k = 0; k < speciesList.size(); k++) {
							dist_vec[k] = distances.get(k)[i];
							rate_vec[k] = rates.get(k)[i];
						}
						
						if(writeTrace){
							sw = new StatsWriter_Text();
							sw.setOutputFolder(outputFolder);
							sw.setDistances(dist_vec);
							sw.setRates(rate_vec);
							sw.setReplicate(n);
							String sw_output = properties.getProperty("Trace_Base_Name","TraceFile") + "_" + n;
							sw.setOutputFile(sw_output);
							try {
								sw.open(new HashSet<String>(speciesList));
								sw.setRunID(i*distances.get(0).length + j*(rates.get(0).length)+n);
							} catch (IOException e1) {
								System.out.println("Could not write statistics to trace file " + outputFolder + "/" + sw_output + ".  Skipping.");
								continue;
							}
							e.setStatsWriter(sw);
							e.writeTraceFile(writeTrace);
						}
						
						e.setMosaic(mosaic.clone());
						e.setStartTime(startTime);
						e.setTimeIncrement(timeIncrement);
						e.setEndTime(endTime);
						mosaicWriter.setWriteHeader(writeHeader);
						e.setOutputWriter(mosaicWriter);
						e.setProcesses(processes);
						e.writeEachTimeStep(writeEachTimeStep);

						int id = (i * reps) + n;

						e.setIdentifier(id + "_" + n);

						ew.setDistances(dist_vec);
						ew.setRates(rate_vec);
						ew.setReplicate(n);
						ew.setID(id);

						e.setExperimentWriter(ew);

						e.run();
						
						pcst.resetCost();
						pcst.resetLabour();
						
						if(writeTrace){
							sw.close();
						}
						
					}
				}
			}
			
			ew.close();

			// Post-process calibration results

			CalibrationAnalysis ca = new CalibrationAnalysis();
			ca.setOutputFolder(outputFolder);
			ca.setStatsTable(outputFile);
			ca.setSpeciesList(speciesList);
			ca.go();
		}

		System.out.println("\nComplete.");

	}

	private List<String> parseStringArray(String string) {

		List<String> list = new ArrayList<String>();
		
		if(string==null){
			return list;
		}

		if (string.startsWith("file:")) {
			String filestring = string.substring(string.indexOf("file:") + 5);
			if (filestring.length() == 5) {
				System.out
						.println("File tag used (file:) but no path was provided after the tag (empty string).  Please check the properties file and referenced files");
			}
			File f = new File(filestring);
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				String ln = br.readLine();
				while (ln != null) {
					list.add(ln);
					br.readLine();
				}
			} catch (FileNotFoundException e) {
				System.out.println("File entry "
						+ string.substring(string.indexOf("file:") + 5)
						+ " could not be found.");
				System.exit(-1);
			} catch (IOException e) {
				System.out.println("File entry "
						+ string.substring(string.indexOf("file:") + 5)
						+ " could not be read or accessed.");
				System.exit(-1);
			}
		}

		StringTokenizer stk = new StringTokenizer(string, "[,]");
		while (stk.hasMoreTokens()) {
			list.add(stk.nextToken());
		}
		return list;
	}

	private List<double[]> parseMultiNumericArray(String string) {
		List<double[]> ma = new ArrayList<double[]>();

		if (string.startsWith("file:")) {
			String filestring = string.substring(string.indexOf("file:") + 5);
			if (filestring.length() == 5) {
				System.out
						.println("File tag used (file:) but no path was provided after the tag (empty string).  Please check the properties file and referenced files");
			}
			File f = new File(filestring);
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				String ln = br.readLine();
				while (ln != null) {
					ma.add(parseNumericArray(ln));
					br.readLine();
				}
			} catch (FileNotFoundException e) {
				System.out.println("File entry "
						+ string.substring(string.indexOf("file:") + 5)
						+ " could not be found.");
				System.exit(-1);
			} catch (IOException e) {
				System.out.println("File entry "
						+ string.substring(string.indexOf("file:") + 5)
						+ " could not be read or accessed.");
				System.exit(-1);
			}
		}

		StringTokenizer stk = new StringTokenizer(string, ";");
		while (stk.hasMoreTokens()) {
			ma.add(parseNumericArray(stk.nextToken()));
		}
		return ma;
	}

	/**
	 * Parses a String to generate arrays. Brace brackets indicate {start, stop,
	 * number of items}. Round brackets indicate (start,interval,number of
	 * items). Square brackets are used to directly specify the array.
	 * 
	 * @param string
	 * @return
	 */

	private double[] parseNumericArray(String string) {

		double[] values;

		if (string.startsWith("file:")) {
			ArrayList<Double> da = new ArrayList<Double>();
			String filestring = string.substring(string.indexOf("file:") + 5);
			if (filestring.length() == 5) {
				System.out
						.println("File tag used (file:) but no path was provided after the tag (empty string).  Please check the properties file and referenced files");
			}
			File f = new File(filestring);
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				String ln = br.readLine();
				while (ln != null) {
					da.addAll(arr2list(parseNumericArray(ln)));
					ln = br.readLine();
				}
			} catch (FileNotFoundException e) {
				System.out.println("File entry "
						+ string.substring(string.indexOf("file:") + 5)
						+ " could not be found.");
				System.exit(-1);
			} catch (IOException e) {
				System.out.println("File entry "
						+ string.substring(string.indexOf("file:") + 5)
						+ " could not be read or accessed.");
				System.exit(-1);
			}
			values = new double[da.size()];
			for (int i = 0; i < da.size(); i++) {
				values[i] = da.get(i).doubleValue();
			}
			return values;
		}

		// parse brace brackets for {start,stop, number of items}

		if (string.startsWith("{") && string.endsWith("}")) {
			StringTokenizer stk = new StringTokenizer(string, "{,}");
			if (stk.countTokens() != 3) {
				throw new IllegalArgumentException(
						"Incorrect parameter values "
								+ string
								+ ".  Brace bracket notation {} indicates a range using min,max and number of values and takes only 3 parameters.");
			}
			double min = Double.parseDouble(stk.nextToken());
			double max = Double.parseDouble(stk.nextToken());

			if (max <= min) {
				throw new IllegalArgumentException(
						"Maximum (2nd) value must be greater than the minimum (1st) value.");
			}

			int quantity = Integer.parseInt(stk.nextToken());
			double interval = (max - min) / (quantity - 1);
			values = new double[quantity];
			values[0] = min;
			values[quantity - 1] = max;
			for (int i = 1; i < quantity - 1; i++) {
				values[i] = min + i * interval;
			}
			return values;
		}

		// parse square brackets for directly specifying the array

		else if (string.startsWith("[") && string.endsWith("]")) {
			StringTokenizer stk = new StringTokenizer(string, "[,]");
			values = new double[stk.countTokens()];
			int ct = 0;
			while (stk.hasMoreTokens()) {
				values[ct] = Double.parseDouble(stk.nextToken());
				ct++;
			}
			return values;
		}

		// parse round brackets for (start,interval,number of items)

		else if (string.startsWith("(") && string.endsWith(")")) {
			StringTokenizer stk = new StringTokenizer(string, "(,)");
			if (stk.countTokens() != 3) {
				throw new IllegalArgumentException(
						"Incorrect parameter values "
								+ string
								+ ".  Round bracket notation () indicates a range using min,interval and number of values and takes only 3 parameters.");
			}

			double min = Double.parseDouble(stk.nextToken());
			double interval = Double.parseDouble(stk.nextToken());
			int quantity = Integer.parseInt(stk.nextToken());

			values = new double[quantity];
			for (int i = 0; i < quantity; i++) {
				values[i] = i * interval + min;
			}

			return values;
		}

		// if a single value, wrap as an array

		else if (isNumeric(string)) {
			return new double[] { Double.parseDouble(string) };
		}

		else {
			throw new IllegalArgumentException(
					"Parameter array values "
							+ string
							+ " could not be parsed.  Please use a single number; a range: {min,max,number of values}; a range: (min,interval,number of values), or a comma separated list surrounded by square brackets []");
		}
	}

	/**
	 * Checks whether a String is a number
	 * 
	 * @param str
	 * @return
	 */

	private boolean isNumeric(String str) {
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition pos = new ParsePosition(0);
		formatter.parse(str, pos);
		return str.length() == pos.getIndex();
	}

	/**
	 * Saves the current Properties to a tab-delimited file
	 * 
	 * @param outputPath
	 *            - THe path location where the file is to be written
	 * @throws IOException
	 */

	private void savePropertiesFile(String outputPath) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath));

		String[] keys = properties.keySet().toArray(new String[0]);
		Arrays.sort(keys);

		for (String key : keys) {
			bw.write(key + "\t" + properties.getProperty(key) + "\n");
		}

		bw.flush();
		bw.close();
	}
	
	private List<Double> arr2list(double[] da){
		ArrayList<Double> list = new ArrayList<Double>();
		for(double d:da){
			list.add(d);
		}
		return list;
	}
}