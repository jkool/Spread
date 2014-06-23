package test.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import spread.impl.RasterMosaic;
import spread.impl.process.Process_Growth;

import org.junit.Before;
import org.junit.Test;

import spread.Patch;

public class Process_GrowthTest {

	RasterMosaic rm = new RasterMosaic();
	Process_Growth pg = new Process_Growth();
	String species = "Test_1";

	@Before
	public void setup() {
		List<String> speciesList = new ArrayList<String>();
		speciesList.add("Test_1");
		speciesList.add("Test_2");
		rm.setSpeciesList(speciesList);
		
		Map<String,long[]> thresholds = new TreeMap<String,long[]>();
		thresholds.put("Test_1", new long[]{5,8});
		thresholds.put("Test_2", new long[]{4,9});
		
		pg.setThresholds(thresholds);
		
		try {
			rm.setPresenceMap("./resource files/test.txt",species);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testProcess() {
		Map<Integer, Patch> cells = rm.getPatches();
		assertFalse(cells.get(0).getOccupant(species).isInfested());
		assertTrue(cells.get(21).getOccupant(species).isInfested());
		assertTrue(cells.get(42).getOccupant(species).isInfested());
		assertEquals(0, cells.get(0).getOccupant(species).getAgeOfInfestation());
		assertEquals(0, cells.get(21).getOccupant(species).getAgeOfInfestation());
		assertEquals(0, cells.get(42).getOccupant(species).getAgeOfInfestation());
		pg.setTimeIncrement(1);
		pg.process(rm);
		assertEquals(0, cells.get(0).getOccupant(species).getAgeOfInfestation());
		assertEquals(1, cells.get(21).getOccupant(species).getAgeOfInfestation());
		assertEquals(1, cells.get(42).getOccupant(species).getAgeOfInfestation());
		assertEquals(1, cells.get(21).getOccupant(species).getStageOfInfestation());
		assertEquals(1, cells.get(21).getOccupant(species).getMaxInfestation());
		pg.process(rm);
		assertEquals(0, cells.get(0).getOccupant(species).getAgeOfInfestation());
		assertEquals(2, cells.get(21).getOccupant(species).getAgeOfInfestation());
		assertEquals(2, cells.get(42).getOccupant(species).getAgeOfInfestation());
		pg.setTimeIncrement(4);
		pg.process(rm);
		assertEquals(0, cells.get(0).getOccupant(species).getAgeOfInfestation());
		assertEquals(6, cells.get(21).getOccupant(species).getAgeOfInfestation());
		assertEquals(6, cells.get(42).getOccupant(species).getAgeOfInfestation());
		assertEquals(2, cells.get(21).getOccupant(species).getStageOfInfestation());
		assertEquals(2, cells.get(21).getOccupant(species).getMaxInfestation());
	}
}
