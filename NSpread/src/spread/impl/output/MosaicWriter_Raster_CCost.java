package spread.impl.output;

import spread.Patch;
import spread.impl.RasterMosaic;
import spread.util.ControlType;

/**
 * Writes a RasterMosaic object to an ASCII output file.
 * 
 */

public class MosaicWriter_Raster_CCost extends MosaicWriter_Raster {

	double containment_cost = 90;

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
		if (rm.getPatches().get(key).hasNoData()) {
			return super.nodata;
		} else {
			Patch patch = rm.getPatches().get(key);
			if (patch.hasControl(ControlType.CONTAINMENT)) {
				return containment_cost;

			}
		}
		return 0;
	}

}
