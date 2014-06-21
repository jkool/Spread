package nsp.impl.process;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import nsp.Mosaic;
import nsp.Occupant;
import nsp.Patch;
import nsp.Process;

/**
 * Performs operations on a Mosaic pertaining to dispersing propagules. Chiefly,
 * calls the disperse() method if a patch is infested.
 * 
 */

public class Process_Dispersal implements Process, Cloneable {

	private Map<String,Long> waitTimes;

	/**
	 * Returns a clone/copy of the instance
	 */

	@Override
	public Process_Dispersal clone() {
		Process_Dispersal pd = new Process_Dispersal();
		Map<String,Long> c_waitTimes = new TreeMap<String,Long>();
			for(String s:waitTimes.keySet()){
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
		Iterator<String> it = patch.getOccupants().keySet().iterator();
		while (it.hasNext()) {
			String species = it.next();
			Occupant o = patch.getOccupant(species);
			if (o.isInfested() && o.getAgeOfInfestation() >= waitTimes.get(species)) {
				o.disperse();
			}
		}
	}

	/**
	 * Sets the amount of time before the onset of dispersion.
	 * 
	 * @param waitTime
	 *            - the amount of time to wait before dispersion (commence at
	 *            equals).
	 */

	public void setWaitTimes(Map<String,Long> waitTimes) {
		this.waitTimes = waitTimes;
	}
}
