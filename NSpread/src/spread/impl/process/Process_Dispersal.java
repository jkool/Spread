/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package spread.impl.process;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import spread.util.ControlType;

import spread.Mosaic;
import spread.Infestation;
import spread.Patch;
import spread.Process;

/**
 * Performs operations on a Mosaic pertaining to dispersing propagules. Chiefly,
 * calls the disperse() method if a patch is infested.
 * 
 */

public class Process_Dispersal implements Process, Cloneable {

	private Map<String, Long> waitTimes;
	private Set<String> coreControl = new TreeSet<String>();

	/**
	 * Returns a clone/copy of the instance
	 */

	@Override
	public Process_Dispersal clone() {
		Process_Dispersal pd = new Process_Dispersal();
		Map<String, Long> c_waitTimes = new TreeMap<String, Long>();
		for (String s : waitTimes.keySet()) {
			c_waitTimes.put(s, waitTimes.get(s));
		}
		pd.setWaitTimes(waitTimes);
		return pd;
	}

	/**
	 * Processes all patches in the Mosaic
	 */

	@Override
	public void process(Mosaic mosaic) {
		for (Integer key : mosaic.getPatches().keySet()) {
			process(mosaic.getPatches().get(key));
		}
	}

	/**
	 * Calls the disperse() method on the provided Patch object
	 * 
	 * @param patch
	 *            - The patch to be processed
	 */

	private void process(Patch patch) {
		Iterator<String> it = patch.getInfestation().keySet().iterator();
		while (it.hasNext()) {
			String species = it.next();
			Infestation o = patch.getInfestation(species);
			if (o.isInfested()
					&& o.getAgeOfInfestation() >= waitTimes.get(species)
					&& !o.hasControl(ControlType.GROUND_CONTROL)
					&& !patch.hasControl(ControlType.CONTAINMENT)
					&& !patch.hasControl(ControlType.CONTAINMENT_CORE_CONTROL,species)
					&& !patch.hasControl(ControlType.GROUND_CONTROL,species)){
				
					o.disperse();
			}
		}
	}

	/**
	 * Sets the amount of time before the onset of dispersion.
	 * 
	 * @param waitTimes
	 *            - the amount of time to wait before dispersion (commence at
	 *            equals).
	 */

	public void setWaitTimes(Map<String, Long> waitTimes) {
		this.waitTimes = waitTimes;
	}
	
	/**
	 * Adds a collection of species (indicated by Strings) to the list
	 * of species that will be ground-controlled in core areas.
	 * @param species - the species to be ground controlled
	 */
	
	public void addToCoreControlList(Collection<String> species){
		this.coreControl.addAll(species);
	}

	/**
	 * Adds a species (indicated by a String) to the list
	 * of species that will be ground-controlled in core areas.
	 * @param species - the species to be ground controlled
	 */
	
	public void addToCoreControlList(String species){
		this.coreControl.add(species);
	}
	
	/**
	 * Resets the process
	 */
	
	@Override
	public void reset(){}
}
