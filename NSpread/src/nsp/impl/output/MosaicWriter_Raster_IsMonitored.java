package nsp.impl.output;

import nsp.Patch;
import nsp.impl.RasterMosaic;

/**
 * Writes a RasterMosaic object to an ASCII output file.
 * 
 */

public class MosaicWriter_Raster_IsMonitored extends MosaicWriter_Raster {

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
		if(!p.hasOccupant(species) || p.getOccupant(species).hasNoData()){return super.nodata;}
		else{return rm.getPatches().get(key).isMonitored()?1:0;}
	}
}
