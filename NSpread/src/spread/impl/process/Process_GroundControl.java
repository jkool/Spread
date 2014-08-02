package spread.impl.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import spread.Mosaic;
import spread.Infestation;
import spread.Patch;
import spread.Process;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import spread.util.ControlType;

/**
 * This is ground control to Major Tom. Commencing countdown, engines on...
 * Planet earth is blue and there's nothing I can do.
 * 
 * @author Johnathan Kool - (Lyrics by David Bowie though)
 * 
 */

public class Process_GroundControl implements Process, Cloneable {

	private long timeIncrement = 1;
	private long counter = 0;
	private long chkFrq = 1;
	private Set<String> ignore = new TreeSet<String>();
	private Set<String> coreControl = new TreeSet<String>();
	private Table<Integer, Long, Integer> table = HashBasedTable.create();

	/**
	 * Initializes this class. Currently the stage-reduction table is hard coded here.
	 */
	
	public Process_GroundControl() {
		table.put(1, 0l, 1);
		table.put(1, 1l, 1);
		table.put(1, 2l, 1);
		table.put(1, 3l, 1);
		table.put(1, 4l, 1);
		table.put(1, 5l, 0);
		table.put(1, 6l, 0);
		table.put(1, 7l, 0);
		table.put(2, 0l, 2);
		table.put(2, 1l, 2);
		table.put(2, 2l, 1);
		table.put(2, 3l, 1);
		table.put(2, 4l, 1);
		table.put(2, 5l, 0);
		table.put(2, 6l, 0);
		table.put(2, 7l, 0);
		table.put(3, 0l, 3);
		table.put(3, 1l, 3);
		table.put(3, 2l, 2);
		table.put(3, 3l, 2);
		table.put(3, 4l, 1);
		table.put(3, 5l, 1);
		table.put(3, 6l, 1);
		table.put(3, 7l, 0);
	}
	
	/**
	 * Adds a collection of species (indicated by Strings) to the list to be ignored by
	 * ground control actions.
	 * @param species - The species to be ignored.
	 */
	
	public void addToIgnoreList(Collection<String> species){
		this.ignore.addAll(species);
	}

	/**
	 * Adds a species (indicated by a String) to the list to be ignored by
	 * ground control actions.
	 * @param species - The species to be ignored.
	 */
	
	public void addToIgnoreList(String species){
		this.ignore.add(species);
	}
	
	/**
	 * Adds a collection of species (indicated by Strings) to the list to be added to
	 * core control activities
	 * @param species - The species to be ignored.
	 */
	
	public void addToCoreControlList(Collection<String> species){
		this.coreControl.addAll(species);
	}

	/**
	 * Adds a species (indicated by a String) to the list to be added to
	 * core control activities
	 * @param species - The species to be ignored.
	 */
	
	public void addToCoreControlList(String species){
		this.coreControl.add(species);
	}

	/**
	 * Returns a copy of the current instance of this class.
	 */
	
	@Override
	public Process_GroundControl clone() {
		Process_GroundControl pgc = new Process_GroundControl();
		pgc.timeIncrement=timeIncrement;
		pgc.counter=counter;
		return pgc;
	}

	/**
	 * Performs actions associated with ground control-based management for the entire Mosaic.
	 */

	@Override
	public void process(Mosaic mosaic) {
		
		counter+=timeIncrement;
		
		if(counter<chkFrq){
			return;
		}
		
		counter = 0;
		
		for (Integer key : mosaic.getPatches().keySet()) {
			process(mosaic.getPatches().get(key));
		}
	}

	/**
	 * Performs actions associated with ground control-based management for a single Patch.
	 */
	
	private void process(Patch patch) {
		
		for (String species : patch.getInfestation().keySet()) {
			
			if(ignore.contains(species)){
				continue;
			}
			
			if(!patch.isInfestedBy(species)){
				patch.getInfestation(species).removeControl(ControlType.GROUND_CONTROL);
				continue;
			}

			// Can you hear me Major Tom? Can you hear me Major Tom?

			if (patch.getInfestation(species).hasControl(ControlType.GROUND_CONTROL)) {
				
				// Retrieve and sort time indices
				
				Set<Long> s = table.columnKeySet();
				
				// We use max infestation here because it corresponds to the
				// initial stage upon discovery (because afterwards it won't
				// increase - at least in the present implementation)
				
				int stage = patch.getInfestation(species).getMaxInfestation();
				ArrayList<Long> times = new ArrayList<Long>(s);
				Collections.sort(times);
				
				// Find the time nearest to the amount of time spent in ground control

				long ctime = patch.getInfestation(species).getControlTime(ControlType.GROUND_CONTROL);
				int nearest_idx = Collections.binarySearch(times, ctime);
				
				// if index value is negative, get -value+1, otherwise it is an exact match
				
				nearest_idx = nearest_idx < 0 ? -(nearest_idx + 1)
						: nearest_idx;
				
				// If the value goes beyond the end of the array, use the last value
				
				nearest_idx = Math.min(times.size()-1,nearest_idx);
				
				// Get the conditioned value for the nearest time
				
				long nearest = times.get(nearest_idx);
				Infestation o = patch.getInfestation(species);
				
				// update stage of infestation
				
				o.setStageOfInfestation(
						table.get(stage, nearest));
				
				// if the stage has reached 0, clear the infestation
				
				if (o.getStageOfInfestation() == 0) {
					o.removeControl(ControlType.GROUND_CONTROL);
					o.clearInfestation();
					
					if(patch.getCurrentOccupants().size()==1&&patch.getCurrentOccupants().contains(species)){
						
					}
				}
				
				else{
					o.setControlTime(ControlType.GROUND_CONTROL, o.getControlTime(ControlType.GROUND_CONTROL)+chkFrq);
				}
			}
		}
	}
	
	/**
	 * Removes a species from the ignore list
	 * 
	 * @param species
	 */
	
	public void removeFromIgnoreList(String species){
		this.ignore.remove(species);
	}
	
	/**
	 * Sets the frequency with which ground control actions are taken.
	 * 
	 * @param checkFrequency
	 */
	
	public void setCheckFrequency(long checkFrequency){
		this.chkFrq=checkFrequency;
	}
	
	/**
	 * Sets the table of control values.  WARNING:  this is currently implemented in
	 * a brittle manner, and there are many assumptions throughout the code based
	 * on the built-in structure.  Caveat emptor.
	 * 
	 * @param table
	 */
	
	public void setControlTable(Table<Integer, Long, Integer> table) {
		this.table = table;
	}
	
	/**
	 * Sets the list of species to be ignored
	 * @param ignore
	 */
	
	public void setIgnoreList(Collection<String> ignore){
		this.ignore = new TreeSet<String>(ignore);
	}
	
	/**
	 * Sets the associated time interval for each time step made by this class.
	 * 
	 * @param timeIncrement
	 */
	
	public void setTimeIncrement(long timeIncrement) {
		this.timeIncrement = timeIncrement;
	}
	
	/**
	 * Resets the process
	 */
	
	@Override
	public void reset(){}
}