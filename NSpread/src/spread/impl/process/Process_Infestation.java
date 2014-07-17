package spread.impl.process;

import java.util.Iterator;

import spread.Mosaic;
import spread.Infestation;
import spread.Process;


/**
 * Performs operations on a Mosaic pertaining to infesting Patches. Chiefly,
 * calls the infest(List<Coordinate> propagules) method to assign propagules to
 * destination cells.
 * 
 */

public class Process_Infestation implements Process, Cloneable {

	/**
	 * Returns a clone/copy of the instance
	 */

	@Override
	public Process_Infestation clone() {
		return new Process_Infestation();
	}

	/**
	 * Calls the calls the infest(List<Coordinate> propagules) method for all
	 * cells in the Mosaic.
	 * 
	 * @param mosaic
	 *            - The mosaic to be processed
	 */

	@Override
	public void process(Mosaic mosaic) {
		for (Integer key : mosaic.getPatches().keySet()) {

			Iterator<String> it = mosaic.getPatch(key).getInfestation().keySet()
					.iterator();
			while (it.hasNext()) {
				Infestation o = mosaic.getPatch(key).getInfestation(it.next());

				if (o.isInfested()) {
					mosaic.infest(o.getName(), o.getPropagules());
				}
				// mosaic.getPatch(key).getPropagules().clear();
			}
		}
	}
	
	/**
	 * Resets the process
	 */
	
	@Override
	public void reset(){}
}