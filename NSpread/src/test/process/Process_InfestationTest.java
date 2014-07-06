package test.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import spread.impl.Disperser_Continuous2D;
import spread.impl.RasterMosaic;
import spread.impl.process.Process_Dispersal;
import spread.impl.process.Process_Infestation;
import spread.impl.random.RandomGenerator_Determined;

import org.junit.Before;
import org.junit.Test;

import spread.Patch;
import spread.RandomGenerator;

public class Process_InfestationTest {

	Disperser_Continuous2D d2;
	RasterMosaic rm = new RasterMosaic();
	Process_Dispersal pd = new Process_Dispersal();
	Process_Infestation pi = new Process_Infestation();
	String species = "Test_1";
	
	@Before
	public void setup(){
		List<String> speciesList = new ArrayList<String>();
		speciesList.add("Test_1");
		speciesList.add("Test_2");
		rm.setSpeciesList(speciesList);
		
		Map<String,Long> waitTimes = new TreeMap<String,Long>();
		waitTimes.put("Test_1", 0l);
		waitTimes.put("Test_2", 3l);
		pd.setWaitTimes(waitTimes);
	}

	@Test
	public void testProcess() {
		d2 = new Disperser_Continuous2D();
		RandomGenerator east = new RandomGenerator_Determined(0);
		RandomGenerator one = new RandomGenerator_Determined(1);
		d2.setDistanceGenerator(one);
		d2.setAngleGenerator(east);
		d2.setNumberGenerator(one);

		try {
			rm.setPresenceMap("./resource files/test.txt",species);
			rm.setHabitatMap("ALL", species);
		} catch (IOException e) {
			e.printStackTrace();
		}

		rm.setDisperser(species, d2);
		Map<Integer, Patch> cells = rm.getPatches();

		assertFalse(cells.get(1).isInfestedBy(species));
		assertFalse(cells.get(22).isInfestedBy(species));
		assertFalse(cells.get(43).isInfestedBy(species));

		pd.process(rm);
		pi.process(rm);

		assertFalse(cells.get(1).isInfestedBy(species));
		assertTrue(cells.get(22).isInfestedBy(species));
		assertTrue(cells.get(43).isInfestedBy(species));
	}
}
