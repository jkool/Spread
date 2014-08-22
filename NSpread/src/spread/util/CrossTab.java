/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package spread.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class CrossTab {

	private Table<Double, Double, Long> table = HashBasedTable.create();
	private boolean writeLabels = true;
	private String sp = ",";
	
	public static void main(String[] args){
		if(args==null || args.length < 3){
			System.out.println("Usage: <zonal raster> <value raster> <output file>");
			System.exit(-1);
		}
		
		Raster zoneRaster = new Raster(new File(args[0]));
		Raster valRaster = new Raster(new File(args[1]));
		CrossTab ct = new CrossTab();
		ct.crossTab(zoneRaster, valRaster);
		ct.writeToFile(new File(args[2]));
	}

	public void crossTab(Raster zoneRaster, Raster valRaster) {

		for (int i = 0; i < zoneRaster.rows; i++) {
			for (int j = 0; j < zoneRaster.cols; j++) {
				double zone = zoneRaster.getValue(i, j);
				double val = valRaster.getValue(i, j);
				if (!table.contains(zone, val)) {
					table.put(zone, val, 1l);
				} else {
					table.put(zone, val, table.get(zone, val) + 1l);
				}
			}
		}
	}

	public boolean checkConsistency(Raster zoneRaster, Raster valRaster) {
		if (!zoneRaster.isConsistent(valRaster)) {
			System.out.println("Raster dimensions are inconsistent.");
			System.out.println("\tZone raster:" + "nrows:" + zoneRaster.rows
					+ " ncols:" + zoneRaster.cols + " llx:" + zoneRaster.xll
					+ " lly:" + zoneRaster.yll + " cellsize:"
					+ zoneRaster.cellsize);
			System.out.println("\tValue raster:" + "nrows:" + valRaster.rows
					+ " ncols:" + valRaster.cols + " llx:" + valRaster.xll
					+ " lly:" + valRaster.yll + " cellsize:"
					+ valRaster.cellsize);
			System.out.println("WARNING: Values were not tabulated.");
			return false;
		}
		return true;
	}

	public void clearTable() {
		table.clear();
	}

	public Set<Double> getRows() {
		return table.rowKeySet();
	}

	public Set<Double> getCols() {
		return table.columnKeySet();
	}

	public void writeToFile(File outputFile) {
		try (FileWriter fw = new FileWriter(outputFile)) {
			if (writeLabels) {
				StringBuilder sb = new StringBuilder();
				sb.append("ZONE" + sp);
				for (Double vals : table.columnKeySet()) {
					sb.append(vals + sp);
				}
				fw.write(sb.toString().substring(0, sb.length() - sp.length()));
			}

			for (Double zone : table.rowKeySet()) {
				StringBuilder sb = new StringBuilder();
				if (writeLabels) {
					sb.append(zone + "sp");
				}
				for (Double vals : table.columnKeySet()) {
					sb.append(vals + sp);
				}
				fw.write(sb.toString().substring(0, sb.length() - sp.length()));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeLabels(boolean writeLabels) {
		this.writeLabels = writeLabels;
	}

}
