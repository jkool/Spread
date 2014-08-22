/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package spread.impl.output;

import spread.Patch;
import spread.impl.RasterMosaic;

/**
 * Writes a RasterMosaic object to an ASCII output file.
 * 
 */

public class MosaicWriter_Raster_WasMonitored extends MosaicWriter_Raster {

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
		Patch p = rm.getPatches().get(key);
		if (p.hasNoData()) {
			return super.nodata;
		} else {
			return p.getMaxControl().ordinal();
		}
	}
}
