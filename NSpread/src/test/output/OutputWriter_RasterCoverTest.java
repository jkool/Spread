/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package test.output;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import spread.impl.RasterMosaic;
import spread.impl.output.MosaicWriter_Raster;
import spread.util.Raster;
import spread.util.RasterReader;

import org.junit.Before;
import org.junit.Test;

public class OutputWriter_RasterCoverTest {

	MosaicWriter_Raster ow = new MosaicWriter_Raster();
	RasterMosaic rm = new RasterMosaic();
	RasterReader rr = new RasterReader();
	String species = "Test_1";
	
	@Before
	public void setup(){
		List<String> speciesList = new ArrayList<String>();
		speciesList.add("Test_1");
		speciesList.add("Test_2");
		rm.setSpeciesList(speciesList);
	}
	
	@Test
	public void testWrite() {
		try {
			rm.setPresenceMap("C:/Temp/Rasters/age.txt",species);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ow.setFolder("C:/Temp/Rasters");
		ow.setName("presence_test.txt");
		try {
			ow.write(rm,species);
			Raster a = rr.readRaster("C:/Temp/Rasters/age.txt");
			Raster b = rr.readRaster("C:/Temp/Rasters/presence_test.txt");
			assertEquals(a.getCellsize(), b.getCellsize(), 1E-12);
			assertEquals(a.getRows(), b.getRows());
			assertEquals(a.getCols(), b.getCols());
			assertEquals(a.getXll(), b.getXll(), 1E-12);
			assertEquals(a.getYll(), b.getYll(), 1E-12);
			assertEquals(b.getValue(1, 1), 1.0, 1E-12);
			assertEquals(b.getValue(2, 2), 1.0, 1E-12);
			assertEquals(b.getValue(3, 3), 1.0, 1E-12);
			assertEquals(b.getValue(0, 10), 1.0, 1E-12);
			assertEquals(b.getValue(1, 11), 1.0, 1E-12);
			assertEquals(b.getValue(2, 12), 1.0, 1E-12);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
