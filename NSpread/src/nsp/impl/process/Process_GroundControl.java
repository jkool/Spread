package nsp.impl.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import nsp.Mosaic;
import nsp.Patch;
import nsp.Process;

/**
 * This is ground control to Major Tom. Commencing countdown, engines on...
 * Planet earth is blue and there's nothing I can do.
 * 
 * @author Johnathan Kool - (Lyrics by David Bowie though)
 * 
 */

public class Process_GroundControl implements Process, Cloneable {

	private long timeIncrement = 1;
	private Table<Integer, Long, Integer> table = HashBasedTable.create();

	public Process_GroundControl() {
		table.put(1, 0l, 1);
		table.put(1, 1l, 1);
		table.put(1, 2l, 1);
		table.put(1, 3l, 1);
		table.put(1, 4l, 1);
		table.put(1, 5l, 0);
		table.put(1, 6l, 0);
		table.put(2, 0l, 2);
		table.put(2, 1l, 2);
		table.put(2, 2l, 1);
		table.put(2, 3l, 1);
		table.put(2, 4l, 1);
		table.put(2, 5l, 0);
		table.put(2, 6l, 0);
		table.put(3, 0l, 3);
		table.put(3, 1l, 3);
		table.put(3, 2l, 2);
		table.put(3, 3l, 2);
		table.put(3, 4l, 1);
		table.put(3, 5l, 1);
		table.put(3, 6l, 1);
	}

	public void process(Mosaic mosaic) {
		for (Integer key : mosaic.getPatches().keySet()) {
			process(mosaic.getPatches().get(key));
		}
	}

	private void process(Patch patch) {

		for (String species : patch.getOccupants().keySet()) {

			// Can you hear me Major Tom? Can you hear me Major Tom?

			if (patch.getOccupant(species).hasControl("GROUND")) {
				Set<Long> s = table.columnKeySet();
				int stage = patch.getOccupant(species).getMaxInfestation();
				ArrayList<Long> times = new ArrayList<Long>(s);
				Collections.sort(times);
				int nearest_idx = Collections.binarySearch(times,
						patch.getOccupant(species).getControlTime("GROUND"));
				nearest_idx = nearest_idx < 0 ? -(nearest_idx + 1)
						: nearest_idx;
				nearest_idx = Math.min(times.size()-1,nearest_idx);
				long nearest = times.get(nearest_idx);
				patch.getOccupant(species).setStageOfInfestation(
						table.get(stage, nearest));
				patch.incrementControlTime("GROUND", timeIncrement);

				if (patch.getOccupant(species).getStageOfInfestation() == 0) {
					patch.getOccupant(species).clearInfestation();
				}
			}
		}
	}

	@Override
	public Process_Monitor clone() {
		return new Process_Monitor();
	}

	public void setTimeIncrement(long timeIncrement) {
		this.timeIncrement = timeIncrement;
	}

	public void setControlTable(Table<Integer, Long, Integer> table) {
		this.table = table;
	}

}
