package test.process;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import nsp.Patch;
import nsp.RandomGenerator;
import nsp.impl.Disperser_Continuous2D;
import nsp.impl.RasterMosaic;
import nsp.impl.process.Process_Dispersal;
import nsp.impl.random.RandomGenerator_Determined;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;

public class Process_DispersalTest {

	RasterMosaic rm = new RasterMosaic();
	Process_Dispersal pd = new Process_Dispersal();
	Disperser_Continuous2D d2;
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
		} catch (IOException e) {
			e.printStackTrace();
		}

		rm.setDisperser(species, d2);

		Map<Integer, Patch> cells = rm.getPatches();

		List<Coordinate> empty = new ArrayList<Coordinate>();
		assertEquals(cells.get(0).getOccupant(species).getPropagules(), empty);
		assertEquals(cells.get(21).getOccupant(species).getPropagules(), empty);
		assertEquals(cells.get(42).getOccupant(species).getPropagules(), empty);
		assertEquals(cells.get(94).getOccupant(species).getPropagules(), empty);

		pd.process(rm);

		assertEquals(empty, cells.get(0).getOccupant(species).getPropagules());
		assertEquals(new Coordinate(2.5, 8.5), cells.get(21).getOccupant(species).getPropagules()
				.get(0));
		assertEquals(new Coordinate(3.5, 7.5), cells.get(42).getOccupant(species).getPropagules()
				.get(0));
		assertEquals(new Coordinate(11.5, 9.5), cells.get(10).getOccupant(species).getPropagules()
				.get(0));
		assertEquals(new Coordinate(15.5, 5.5), cells.get(94).getOccupant(species).getPropagules()
				.get(0));
	}
}