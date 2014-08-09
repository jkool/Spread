package spread.impl.output;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import spread.Patch;
import spread.impl.RasterMosaic;
import spread.util.ControlType;

/**
 * Writes a RasterMosaic object to an ASCII output file.
 * 
 */

public class MosaicWriter_Raster_TCost extends MosaicWriter_Raster {
	double containment_cost = 90;
	private Map<String, double[]> ground_control_costs = new TreeMap<String, double[]>();
	
	public MosaicWriter_Raster_TCost(){
		ground_control_costs.put("para", new double[]{1,10,100});
		ground_control_costs.put("OH", new double[]{1000,10000,100000});
	}

	/**
	 * Retrieves a value from the Raster Mosaic based on the key value provided.
	 * For a raster this is its row number * the total # of columns plus its
	 * column number. For this class, the value retrieved indicates whether the
	 * cell is infested or not (cover).
	 * 
	 * @param rm
	 * @param key
	 * @return
	 */

	@Override
	protected double getVal(RasterMosaic rm, int key, String species) {
		double patchCost=0;
		if (rm.getPatches().get(key).hasNoData()) {
			return super.nodata;
		} else {
			Patch patch = rm.getPatch(key);
			if (patch.hasNoData()) {
				return 0;
			}

			// Add patch-level cost

			if (patch.hasControl(ControlType.CONTAINMENT)) {
				patchCost += containment_cost;
			}

			// Add species-level cost

			double max_gc = 0;

			for (String sp2 : patch.getInfestation().keySet()) {
				Set<ControlType> sp_controls = patch.getControls(sp2);
				
				// Only add species cost if ground-controlled or core controlled

				if (sp_controls.contains(ControlType.GROUND_CONTROL)
						|| sp_controls
								.contains(ControlType.CONTAINMENT_CORE_CONTROL)) {

					int stage = patch.getInfestation(sp2)
							.getStageOfInfestation();

					// Take the maximum cost of the species-level controls
					
					max_gc = Math.max(max_gc,
							ground_control_costs.get(sp2)[stage - 1]);
				}
			}

			patchCost += max_gc;

			return patchCost;
		}
	}
}
