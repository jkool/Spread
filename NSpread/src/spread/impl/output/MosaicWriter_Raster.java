package spread.impl.output;

import java.io.IOException;

import spread.Mosaic;
import spread.MosaicWriter;

import spread.impl.RasterMosaic;
import spread.util.Raster;
import spread.util.RasterWriter;

/**
 * Writes a RasterMosaic object to an ASCII output file.
 * 
 */

public class MosaicWriter_Raster implements MosaicWriter {
	private String path = ".";
	private String name = "default.txt";
	private boolean writeHeader = false;
	private Raster current;
	protected int nodata = -9999;

	/**
	 * Writes the contents of the RasterMosaic to an ESRI format ASCII file
	 * using RasterWriter
	 */

	@Override
	public void write(Mosaic mosaic, String species) {
		RasterMosaic rm = (RasterMosaic) mosaic;
		double[][] data = new double[rm.getNrows()][rm.getNcols()];
		for (int i = 0; i < rm.getNrows(); i++) {
			for (int j = 0; j < rm.getNcols(); j++) {
				int key = i * rm.getNcols() + j;
				data[i][j] = getVal(rm, key, species);
			}
		}
		RasterWriter rw = new RasterWriter();
		try {
			rw.setWriteHeader(writeHeader);
			current = new Raster(data,rm.getCellsize(),rm.getLlx(),rm.getLly());
			rw.writeRaster(path + "/" + name + ".txt", data, rm.getLlx(),
					rm.getLly(), rm.getCellsize(), "-9999");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Retrieves the last data processed as a Raster object
	 * @return the last data processed as a Raster object
	 */
	
	public Raster getCurrent(){
		return current;
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

	protected double getVal(RasterMosaic rm, int key, String species){
		if(rm.getPatches().get(key).hasNoData()){return nodata;}
		else{return rm.getPatches().get(key).isInfestedBy(species) ? 1 : 0;}
	}
	
	/**
	 * Gets the output folder/directory.
	 */

	@Override
	public String getFolder(){
		return path;
	}
	
	/**
	 * Sets the path of the output file. The name of the file is set separately
	 * by setName
	 */

	@Override
	public void setFolder(String path) {
		this.path = path;
	}

	/**
	 * Sets the name of the output file. The path is set separately by setPath
	 */

	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets whether the ESRI ASCII header should be added to the file or not.
	 * Having a header is useful for viewing in GIS. No header is useful if
	 * importing files into R or Matlab.
	 */

	@Override
	public void setWriteHeader(boolean writeHeader) {
		this.writeHeader = writeHeader;
	}
	
	/**
	 * Gets whether header information is included in the written output.
	 */
	
	@Override
	public boolean getWriteHeader(){
		return writeHeader;
	}
	
}
