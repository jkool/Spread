package spread;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import spread.impl.output.MosaicWriter_Raster;
import spread.impl.output.MosaicWriter_Raster_WasMonitored;
import spread.impl.output.MosaicWriter_Raster_Stage;
import spread.impl.output.StatsWriter_Text;
import spread.util.ControlType;

/**
 * This class is used to apply Processes to a Mosaic over time. Time is handled
 * using a long value, so values from Date objects can be applied. step()
 * implements a single time step (pass through the Process List) whereas run
 * iterates multiple time steps from start to end spaced by timeIncrement.
 */

public class Experiment implements Cloneable {

	private Mosaic mosaic;
	private MosaicWriter mw = new MosaicWriter_Raster();
	private MosaicWriter ms = new MosaicWriter_Raster_Stage();
	private MosaicWriter mm = new MosaicWriter_Raster_WasMonitored();
	private StatsWriter sw = new StatsWriter_Text();
	private ExperimentWriter ew;
	private List<Process> processes = new ArrayList<Process>();
	private List<Set<Integer>> record = new ArrayList<Set<Integer>>();
	private long startTime = 0;
	private long time = 0;
	private long endTime = 0;
	private long timeIncrement = 1;
	private String identifier = "";
	private boolean writeEachTimeStep = false;
	private boolean writeTraceFile = false;

	/**
	 * Returns a clone/copy of the instance
	 */

	@Override
	public Experiment clone() {
		Experiment ex = new Experiment();
		ex.setStartTime(startTime);
		ex.setEndTime(endTime);
		ex.setTime(time);
		ex.setOutputWriter(mw);// May need to consider cloning instead of
								// passing a reference.
		ex.setStageWriter(ms);// May need to consider cloning instead of
		// passing a reference.
		ex.setMonitoredWriter(mm);// May need to consider cloning instead of
		// passing a reference.
		ex.setStatsWriter(sw);
		return ex;
	}

	/**
	 * Retrieves the end time of the Experiment (long value).
	 * 
	 * @return - the end time of the Experiment (long value)
	 */

	public long getEndTime() {
		return endTime;
	}

	/**
	 * Retrieves the String identifier of the Experiment. TODO implement
	 * toString() method.
	 * 
	 * @return - the String identifier of the Experiment
	 */

	public String getIdentifier() {
		return identifier;
	}

	// Getters and Setters
	// ///////////////////////////////////////////////////////////////////

	/**
	 * Retrieves the Mosaic associated with the Experiment.
	 * 
	 * @return - the Mosaic associated with the Experiment.
	 */

	public Mosaic getMosaic() {
		return mosaic;
	}

	/**
	 * Returns the OutputWriter object being used by the Experiment
	 * 
	 * @return - the OutputWriter object being used by the Experiment
	 */

	public MosaicWriter getMosaicWriter() {
		return mw;
	}

	/**
	 * Iterates over the Mosaic to identify the number of infested Patches.
	 * 
	 * @return - the number of infested Patches
	 */

	public int getNInfested() {
		return mosaic.getNumberInfested();
	}

	/**
	 * Iterates over the Mosaic to identify the number of infested Patches.
	 * 
	 * @return - the number of infested Patches
	 */

	public int getNInfested(String species) {
		return mosaic.getNumberInfested(species);
	}

	/**
	 * Returns the List of Processes being used in the Experiment
	 * 
	 * @return - the List of Processes being used in the Experiment
	 */

	public List<Process> getProcesses() {
		return processes;
	}

	/**
	 * Retrieves the start time of the Experiment (long value).
	 * 
	 * @return - the start time of the Experiment (long value)
	 */

	public long getStartTime() {
		return startTime;
	}

	/**
	 * Retrieves the current time of the Experiment (long value).
	 * 
	 * @return - the current time of the Experiment (long value)
	 */

	public long getTime() {
		return time;
	}

	/**
	 * Retrieves the time increment being used by the Experiment (long value).
	 * 
	 * @return - the time increment being used by the Experiment (long value).
	 */

	public long getTimeIncrement() {
		return timeIncrement;
	}

	/**
	 * Runs step() multiple times from startTime to endTime spaced by
	 * timeIncrement.
	 */

	public void run() {

		if (endTime < startTime) {
			System.out
					.println("WARNING:  Specified end time is before start time.");
			return;
		}

		for (long t = startTime; t < endTime; t += timeIncrement) {
			time = t;
			///////////////////////////////////////////////////////////////////////
			step();
		}

		ew.write(this);

		System.gc();
	}

	/**
	 * Sets the Disperser object of the Experiment
	 * 
	 * @param d
	 */

	public void setDisperser(String species, Disperser d) {
		if (mosaic == null) {
			System.out
					.println("WARNING: mosaic has not been initialized.  Cannot set disperser.");
			return;
		}
		mosaic.setDisperser(species, d);
	}

	/**
	 * Sets the Disperser object to a Collection of Patches identified by their
	 * key.
	 * 
	 * @param d
	 */

	public void setDisperser(String species, Disperser d,
			Collection<Integer> keys) {
		for (Integer key : keys) {
			mosaic.setDisperser(species, d.clone(), key);
		}
	}

	/**
	 * Sets the end time of the Experiment (using a long value)
	 * 
	 * @param endTime
	 */

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public void setExperimentWriter(ExperimentWriter ew) {
		this.ew = ew;
	}

	/**
	 * Sets the identifier of the Experiment
	 * 
	 * @param identifier
	 */

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Sets the OutputWriter associated with the Experiment
	 * 
	 * @param ow
	 */

	public void setMonitoredWriter(MosaicWriter om) {
		this.mm = om;
	}

	/**
	 * Sets the Mosaic associated with the Experiment.
	 * 
	 * @param mosaic
	 */

	public void setMosaic(Mosaic mosaic) {
		this.mosaic = mosaic;
	}

	/**
	 * Sets the OutputWriter associated with the Experiment
	 * 
	 * @param ow
	 */

	public void setOutputWriter(MosaicWriter ow) {
		this.mw = ow;
	}

	/**
	 * Sets the List of Processes to be used by the Experiment
	 * 
	 * @param processes
	 */

	public void setProcesses(List<Process> processes) {
		this.processes = processes;
	}
	
	/**
	 * Sets the OutputWriter associated with the Experiment
	 * 
	 * @param ow
	 */

	public void setStageWriter(MosaicWriter os) {
		this.ms = os;
	}

	/**
	 * Sets the start time of the Experiment (as a long value)
	 * 
	 * @param startTime
	 */

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * Sets the OutputWrier associated with the Experiment
	 * 
	 * @param ow
	 */

	public void setStatsWriter(StatsWriter sw) {
		this.sw = sw;
	}

	/**
	 * Sets the current time of the Experiment (as a long value)
	 * 
	 * @param time
	 */

	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * Sets the time increment of the Experiment
	 * 
	 * @param timeIncrement
	 */

	public void setTimeIncrement(long timeIncrement) {
		this.timeIncrement = timeIncrement;
	}

	/**
	 * Performs a single pass through the Process List, applying them to the
	 * Mosaic.
	 */

	public void step() {
		for (Process proc : processes) {
			proc.process(mosaic);
		}

		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(Long.toString(endTime).length());

		for (String species : mosaic.getSpeciesList()) {

			if (writeEachTimeStep) {
				mw.setName("cover" + "_" + identifier + "_" + species + "_t"
						+ nf.format(time));
				mw.write(mosaic, species);
			}
		}
		
		if(writeTraceFile){
			sw.write(this);
		}

		record.add(mosaic.getPatches().keySet());
	}

	/**
	 * Sets whether output should be written at each time step
	 */

	public void writeEachTimeStep(boolean writeEachTimeStep) {
		this.writeEachTimeStep = writeEachTimeStep;
	}
	
	/**
	 * Sets whether trace files should be written for this Experiment.
	 * @param writeTraceFile
	 */
	
	public void writeTraceFile(boolean writeTraceFile){
		this.writeTraceFile = writeTraceFile;
	}
}
