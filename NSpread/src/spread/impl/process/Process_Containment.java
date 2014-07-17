package spread.impl.process;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import spread.Mosaic;
import spread.Patch;
import spread.Process;

import spread.util.ControlType;

/**
 * Performs actions relating to containment of infested areas
 *
 */

public class Process_Containment implements Process, Cloneable{
	
	private long timeIncrement = 1;
	private long chkFrq = 1;
	private long counter = 0;
	private Set<String> ignore = new TreeSet<String>();
	
	/**
	 * Adds a collection of species to the ignore list.
	 * @param species
	 */
	
	public void addToIgnoreList(Collection<String> species){
		this.ignore.addAll(species);
	}
	
	/**
	 * Adds a species to the ignore list.
	 * @param species
	 */
	
	public void addToIgnoreList(String species){
		this.ignore.add(species);
	}
	
	/**
	 * Creates a cloned instance of this class.
	 */
	
	@Override
	public Process_Containment clone(){
		Process_Containment clone = new Process_Containment();
		return clone;
	}
	
	/**
	 * Processes the entire mosaic.
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
	 * Processes an individual patch.
	 * @param patch
	 */

	private void process(Patch patch) {
		
		if (patch.hasControl(ControlType.CONTAINMENT_CORE)) {
			return;
		}
		
		for (String species : patch.getInfestation().keySet()) {
			
			if(ignore.contains(species)){
				continue;
			}

			// I'm gonna break my rusty cage... and run!
			
			if (patch.hasControl(ControlType.CONTAINMENT)) {
				patch.getInfestation(species).setStageOfInfestation(0);
				patch.getInfestation(species).clearInfestation();
			}
		}
	}
	
	/**
	 * Removes a species from the ignore list.
	 * @param species - the species to be removed from the ignore list.
	 */
	
	public void removeFromIgnoreList(String species){
		this.ignore.remove(species);
	}
	
	/**
	 * Sets the frequency with which containment actions should occur.
	 * @param checkFrequency - The frequency of checking for containment.
	 */
	
	public void setCheckFrequency(long checkFrequency){
		this.chkFrq=checkFrequency;
	}
	
	/**
	 * Sets the list of species to be ignored.
	 * @param ignore - the species to be ignored.
	 */
	
	public void setIgnoreList(Collection<String> ignore){
		this.ignore = new TreeSet<String>(ignore);
	}
	
	/**
	 * Sets the time increment over which containment actions will take place.
	 * @param timeIncrement - the time increment over which actions will occur.
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
