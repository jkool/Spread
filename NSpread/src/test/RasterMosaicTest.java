package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Assert;

import nsp.Disperser;
import nsp.Patch;
import nsp.RandomGenerator;
import nsp.impl.Disperser_Continuous2D;
import nsp.impl.RasterMosaic;
import nsp.impl.random.RandomGenerator_Determined;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;

public class RasterMosaicTest {

	RasterMosaic re = new RasterMosaic();
	String species = "Test_1";
	
	@Before
	public void setup(){
		List<String> speciesList = new ArrayList<String>();
		speciesList.add("Test_1");
		speciesList.add("Test_2");
		re.setSpeciesList(speciesList);
	}
	
	@Test
	public void testGetBlock(){
		re.clear();
		try {
			re.setPresenceMap("C:/Temp/Rasters/Patchtest.txt",species);
			int[] bnds = new int[]{0,1,3,2};
			Set<Patch> filledRegion = re.getBlock(bnds);
			Set<Integer> keys = re.getKeys(filledRegion);
			Set<Integer> expected = new TreeSet<Integer>();

			expected.add(1);
			expected.add(2);
			expected.add(21);
			expected.add(22);
			expected.add(41);
			expected.add(42);
			expected.add(61);
			expected.add(62);
			assertEquals(expected,keys);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetBounds(){
		re.clear();
		try {
			re.setPresenceMap("C:/Temp/Rasters/Patchtest.txt",species);
			Set<Patch> region = re.getWeakRegion(re.getPatch(105),species);
			int[] bounds = re.getBounds(region);
			int[] expected = new int[]{5,5,7,9};
			assertArrayEquals(expected,bounds);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetFilledRegion(){
		re.clear();
		try {
			re.setPresenceMap("C:/Temp/Rasters/Patchtest.txt",species);
			Set<Patch> region = re.getWeakRegion(re.getPatch(7),species);
			Set<Patch> filledRegion = re.fill(region,species);
			Set<Integer> keys = re.getKeys(filledRegion);
			Set<Integer> expected = new TreeSet<Integer>();
			expected.add(7);
			expected.add(26);
			expected.add(27);
			expected.add(28);
			expected.add(46);
			expected.add(47);
			expected.add(48);
			expected.add(66);
			expected.add(67);
			expected.add(68);
			expected.add(87);
			assertEquals(expected,keys);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetStrongAdjacent(){
		re.clear();
		try {
			re.setPresenceMap("C:/Temp/Rasters/Patchtest.txt",species);
			Set<Patch> cells = re.getStrongAdjacent(re.getPatch(27));
			Set<Integer> keys = re.getKeys(cells);
			Set<Integer> expected = new TreeSet<Integer>();
			expected.clear();
			expected.add(7);
			expected.add(26);
			expected.add(28);
			expected.add(47);
			assertEquals(expected,keys);
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	
	@Test
	public void testGetStrongRegion(){
		
		re.clear();
		
		try {
			re.setPresenceMap("C:/Temp/Rasters/Patchtest.txt",species);
			Set<Patch> cells = re.getStrongRegion(re.getPatch(27),species);
			Set<Integer> keys = re.getKeys(cells);
			Set<Integer> expected = new TreeSet<Integer>();
			expected.clear();
			expected.add(27);
			expected.add(47);
			expected.add(67);
			assertEquals(expected,keys);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetWeakAdjacent(){
		re.clear();
		try {
			re.setPresenceMap("C:/Temp/Rasters/Patchtest.txt",species);
			Set<Patch> cells = re.getWeakAdjacent(re.getPatch(27));
			Set<Integer> keys = re.getKeys(cells);
			Set<Integer> expected = new TreeSet<Integer>();
			expected.add(6);
			expected.add(7);
			expected.add(8);
			expected.add(26);
			expected.add(28);
			expected.add(46);
			expected.add(47);
			expected.add(48);
			assertEquals(expected,keys);
			
			expected.clear();
			cells = re.getWeakAdjacent(re.getPatch(59));
			keys = re.getKeys(cells);
			expected.add(38);
			expected.add(39);
			expected.add(58);
			expected.add(78);
			expected.add(79);
			assertEquals(expected,keys);
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	@Test
	public void testGetWeakRegion(){
		// Clear the mosaic
		re.clear();

		try {
			// Test retrieval of a connected region
			re.setPresenceMap("C:/Temp/Rasters/Patchtest.txt",species);
			Set<Patch> cells = re.getWeakRegion(re.getPatch(7),species);
			Set<Integer> keys = re.getKeys(cells);
			Set<Integer> expected = new TreeSet<Integer>();
			expected.add(7);
			expected.add(26);
			expected.add(28);
			expected.add(46);
			expected.add(48);
			expected.add(66);
			expected.add(68);
			expected.add(87);
			assertEquals(expected,keys);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testHabitat(){
		try {
			// Clear the mosaic
			re.clear();

			// Set the parameters of the mosaic using a raster template
			re.setPresenceMap("./resource files/test.txt",species);

			// Set the environment such that no cells are infested
			re.setPresenceMap("NONE",species);
			
			//
			re.setHabitatMap("NONE",species);

			// Generate a List of propagules - note, these are coordinates, NOT
			// indices i.e. y is flipped.
			List<Coordinate> test_propagules = new ArrayList<Coordinate>();
			test_propagules.add(new Coordinate(0.5, 9.5));
			test_propagules.add(new Coordinate(1.5, 8.5));
			test_propagules.add(new Coordinate(2, 7.5));
			test_propagules.add(new Coordinate(4.5, 6));
			test_propagules.add(new Coordinate(7, 3));

			// Run the infestation
			re.infest(species, test_propagules);

			// Ensure cells are not infested
			assertFalse(re.getPatches().get(0).getOccupant(species).isInfested());
			assertFalse(re.getPatches().get(20).getOccupant(species).isInfested());
			assertFalse(re.getPatches().get(42).getOccupant(species).isInfested());
			assertFalse(re.getPatches().get(64).getOccupant(species).isInfested());
			assertFalse(re.getPatches().get(127).getOccupant(species).isInfested());
			
			re.setHabitatMap("ALL",species);
			
			// Run the infestation
			re.infest(species,test_propagules);

			// Ensure cells are now infested
			assertTrue(re.getPatches().get(0).getOccupant(species).isInfested());
			assertTrue(re.getPatches().get(21).getOccupant(species).isInfested());
			assertTrue(re.getPatches().get(42).getOccupant(species).isInfested());
			assertTrue(re.getPatches().get(64).getOccupant(species).isInfested());
			assertTrue(re.getPatches().get(127).getOccupant(species).isInfested());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testInfest() {
		try {
			// Clear the mosaic
			re.clear();

			// Set the parameters of the mosaic using a raster template
			re.setPresenceMap("C:/Temp/Rasters/Age.txt",species);

			// Set the environment such that no cells are infested
			re.setPresenceMap("NONE",species);

			// Generate a List of propagules - note, these are coordinates, NOT
			// indices i.e. y is flipped.
			List<Coordinate> test_propagules = new ArrayList<Coordinate>();
			test_propagules.add(new Coordinate(0.5, 9.5));
			test_propagules.add(new Coordinate(1.5, 8.5));
			test_propagules.add(new Coordinate(2, 7.5));
			test_propagules.add(new Coordinate(4.5, 6));
			test_propagules.add(new Coordinate(7, 3));

			// Ensure cells are not infested
			assertFalse(re.getPatches().get(0).getOccupant(species).isInfested());
			assertFalse(re.getPatches().get(20).getOccupant(species).isInfested());
			assertFalse(re.getPatches().get(42).getOccupant(species).isInfested());
			assertFalse(re.getPatches().get(64).getOccupant(species).isInfested());
			assertFalse(re.getPatches().get(127).getOccupant(species).isInfested());

			// Run the infestation
			re.infest(species, test_propagules);

			// Ensure cells are now infested
			assertTrue(re.getPatches().get(0).getOccupant(species).isInfested());
			assertTrue(re.getPatches().get(21).getOccupant(species).isInfested());
			assertTrue(re.getPatches().get(42).getOccupant(species).isInfested());
			assertTrue(re.getPatches().get(64).getOccupant(species).isInfested());
			assertTrue(re.getPatches().get(127).getOccupant(species).isInfested());

			// Ensure no other cells are infested
			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < 20; j++) {
					int key = i * 20 + j;
					if (key == 0 || key == 21 || key == 42 || key == 64
							|| key == 127 || re.getPatches().get(key).hasNoData()) {
						continue;
					}
					assertFalse(re.getPatches().get(key).getOccupant(species).isInfested());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	@Test
	public void testNibbleStrong(){
		re.clear();
		try {
			re.setPresenceMap("C:/Temp/Rasters/Patchtest.txt",species);
			Set<Patch> region = re.getWeakRegion(re.getPatch(21),species);
			region = re.nibbleStrong(region,species);
			Set<Integer> keys = re.getKeys(region);
			
			Set<Integer> expected = new TreeSet<Integer>();
			expected.add(40);
			expected.add(41);
			expected.add(42);
			expected.add(43);
			expected.add(60);
			expected.add(61);
			expected.add(62);
			expected.add(63);
			expected.add(80);
			expected.add(81);
			expected.add(82);
			assertEquals(expected, keys);	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//@Test
	public void testNibbleWeak(){
		re.clear();
		try {
			re.setPresenceMap("C:/Temp/Rasters/Patchtest.txt",species);
			Set<Patch> region = re.getWeakRegion(re.getPatch(21),species);
			region = re.nibbleWeak(region,species);
			Set<Integer> keys = re.getKeys(region);
			
			Set<Integer> expected = new TreeSet<Integer>();
			expected.add(40);
			expected.add(41);
			expected.add(42);
			expected.add(43);
			expected.add(60);
			expected.add(61);
			expected.add(62);
			expected.add(80);
			expected.add(81);
			expected.add(82);
			assertEquals(expected, keys);	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSearchInfestation(){
		re.clear();
		try {
			re.setPresenceMap("C:/Temp/Rasters/Patchtest.txt",species);
			Patch p = re.getPatch(0);
			Set<Patch> s = re.searchInfestation(p,species);
			assertTrue(s.size()==0);
			p = re.getPatch(22);
			s = re.searchInfestation(p,species);
			assertEquals(23,s.size());
			p = re.getPatch(105);
			s = re.searchInfestation(p,species);
			assertEquals(5,s.size());
			p = re.getPatch(50);
			s = re.searchInfestation(p,species);
			assertEquals(9,s.size());
			p = re.getPatch(18);
			s = re.searchInfestation(p,species);
			Assert.assertNull(s);
			p = re.getPatch(59);
			s = re.searchInfestation(p,species);
			assertEquals(3,s.size());
			p = re.getPatch(180);
			s = re.searchInfestation(p,species);
			assertEquals(6,s.size());
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	@Test
	public void testSetAgeMap() {
		try {
			// Clear the mosaic
			re.clear();

			// Set the infestation presence of the mosaic using a raster
			// template
			re.setPresenceMap("C:/Temp/Rasters/Age.txt",species);
			Map<Integer, Patch> cells = re.getPatches();

			// Set the age of infestation using the test raster
			re.setAgeMap("C:/Temp/Rasters/Age.txt",species);
			assertEquals(0, cells.get(0).getOccupant(species).getAgeOfInfestation());
			assertEquals(1, cells.get(21).getOccupant(species).getAgeOfInfestation());
			assertEquals(2, cells.get(42).getOccupant(species).getAgeOfInfestation());
			assertEquals(3, cells.get(63).getOccupant(species).getAgeOfInfestation());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSetDisperser() {
		try {
			Disperser_Continuous2D d2 = new Disperser_Continuous2D();
			RandomGenerator east = new RandomGenerator_Determined(0);
			RandomGenerator one = new RandomGenerator_Determined(1);
			d2.setDistanceGenerator(one);
			d2.setAngleGenerator(east);
			d2.setNumberGenerator(one);
			re.clear();
			re.setPresenceMap("C:/Temp/Rasters/Age.txt",species);
			re.setDisperser(species,d2);
			Map<Integer, Patch> cells = re.getPatches();
			assertEquals(new Coordinate(1.5, 8.5), cells.get(21).getOccupant(species).getDisperser()
					.getPosition());
			assertEquals(new Coordinate(2.5, 7.5), cells.get(42).getOccupant(species).getDisperser()
					.getPosition());
			assertEquals(new Coordinate(3.5, 6.5), cells.get(63).getOccupant(species).getDisperser()
					.getPosition());
			assertEquals(new Coordinate(10.5, 9.5), cells.get(10)
					.getOccupant(species).getDisperser().getPosition());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testSetDisperserWithKey() {
		try {
			Disperser_Continuous2D d2 = new Disperser_Continuous2D();
			RandomGenerator east = new RandomGenerator_Determined(0);
			RandomGenerator one = new RandomGenerator_Determined(1);
			d2.setDistanceGenerator(one);
			d2.setAngleGenerator(east);
			d2.setNumberGenerator(one);
			re.clear();
			re.setPresenceMap("C:/Temp/Rasters/Age.txt",species);

			// Set the disperser at 1,1
			re.setDisperser(species,d2, 21);
			Disperser d = re.getPatches().get(21).getOccupant(species).getDisperser();

			// Ensure we are retrieving the same object
			assertEquals(d, d2);

			// Check the position of the disperser. The expected position
			// is x=1.5, y=8.5, which is the center of cell 1,1 (i,j indexing)
			// where x=0.0, y=0.0 is the lower left corner.

			assertEquals(new Coordinate(1.5, 8.5), d.getPosition());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSetPresenceMap() {
		// Clear the mosaic
		re.clear();

		try {
			// Set the infestation presence of the mosaic using a raster
			// template
			re.setPresenceMap("C:/Temp/Rasters/Age.txt",species);
			Map<Integer, Patch> cells = re.getPatches();

			// Ensure that cells are infested in a manner consistent with
			// the structure of the test raster

			assertFalse(cells.get(0).getOccupant(species).isInfested());
			assertTrue(cells.get(21).getOccupant(species).isInfested());
			assertTrue(cells.get(42).getOccupant(species).isInfested());
			assertTrue(cells.get(63).getOccupant(species).isInfested());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
