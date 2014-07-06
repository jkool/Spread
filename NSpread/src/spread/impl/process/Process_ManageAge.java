package spread.impl.process;

import java.util.Iterator;

import spread.Mosaic;
import spread.Infestation;
import spread.Patch;
import spread.Process;


/**
 * Performs management operations on a Mosaic. Chiefly, detects if the age of
 * infestation is above a given threshold, and if so, clears the patch of
 * infestation.
 * 
 */

public class Process_ManageAge implements Process, Cloneable {

	private Number ageThreshold = 2;

	/**
	 * Returns a clone/copy of the instance
	 */

	@Override
	public Process_ManageAge clone() {
		Process_ManageAge pma = new Process_ManageAge();
		pma.ageThreshold = ageThreshold;
		return pma;
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
	 * Manages the provided patch on the basis of age. If the age of infestation
	 * exceeds the ageThreshold setting, the cell is cleared of its infestation.
	 * 
	 * @param patch
	 *            - The patch to be processed
	 */

	public void process(Patch patch) {

		Iterator<String> it = patch.getInfestation().keySet().iterator();
		while (it.hasNext()) {
			Infestation o = patch.getInfestation(it.next());

			if (o.getAgeOfInfestation() >= ageThreshold.longValue()) {
				o.setInfested(false);
				o.setAgeOfInfestation(0);
				o.setStageOfInfestation(0);
				return;
			}
		}
	}
	
	/**
	 * Resets the process
	 */
	
	public void reset(){}
}