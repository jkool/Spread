package nsp;

/**
 * Generic interface for reading over Moaic objects and persisting them to
 * output.
 * 
 */

public interface MosaicWriter {

	/**
	 * Sets the path of the output file. The name of the file is set separately
	 * by setName
	 */

	public void setFolder(String path);

	/**
	 * Sets the name of the output file. The path is set separately by setPath
	 */

	public void setName(String name);

	/**
	 * Sets whether the ESRI ASCII header should be added to the file or not.
	 * Having a header is useful for viewing in GIS. No header is useful if
	 * importing files into R or Matlab.
	 */

	public void setWriteHeader(boolean writeHeader);

	/**
	 * Writes the contents of the RasterMosaic to an ESRI format ASCII file
	 * using RasterWriter
	 */

	public void write(Mosaic mosaic, String species);
}
