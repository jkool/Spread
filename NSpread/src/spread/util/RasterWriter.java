package spread.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class RasterWriter {
	private NumberFormat cellFormat = null;
	private boolean writeHeader = true;
	public static final DecimalFormat INT_FORMAT = new DecimalFormat("0");

	public void setCellFormat(NumberFormat format) {
		cellFormat = format;
	}

	public void setWriteHeader(boolean writeHeader) {
		this.writeHeader = writeHeader;
	}

	public void writeRaster(String filename, double[][] data, double xll,
			double yll, double size, String ndata) throws IOException {
		writeRaster(filename, Raster.getTempRaster(data, xll, yll, size, ndata));
	}

	public void writeRaster(String filename, Raster r) throws IOException {
		File f = new File(filename);
		if (f.exists())
			f.delete();
		if (!f.createNewFile())
			System.err.println("Could not create file.");

		PrintStream o = new PrintStream(f);

		if (writeHeader) {
			o.println("ncols " + r.getCols());
			o.println("nrows " + r.getRows());
			o.println("xllcorner " + r.getXll());
			o.println("yllcorner " + r.getYll());
			o.println("cellsize " + r.getCellsize());
			o.println("NODATA_value " + r.getNDATA());
		}

		for (double[] row : r.getData()) {
			StringBuffer b = new StringBuffer();
			for (int i = 0; i < row.length; i++) {
				if (Double.isNaN(row[i]))
					b.append(r.getNDATA());
				else if (cellFormat != null)
					b.append(cellFormat.format(row[i]));
				else
					b.append(row[i]);
				if (i < row.length - 1)
					b.append(" ");
			}
			o.println(b);
		}
		o.close();
	}
}
