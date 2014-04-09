package test.process;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import nsp.Patch;
import nsp.impl.RasterMosaic;
import nsp.impl.process.Process_Costing;
import nsp.impl.process.Process_Monitor;
import nsp.util.ManagementTypes;

import org.junit.Before;
import org.junit.Test;

public class Process_CostingTest {
	
	RasterMosaic rm = new RasterMosaic();
	Process_Monitor pm = new Process_Monitor();
	Process_Costing pc = new Process_Costing();
	String species = "Test_1";
	Map<String,String> dict = new HashMap<String,String>();

	@Before
	public void setUp() throws Exception {
		List<String> speciesList = new ArrayList<String>();
		speciesList.add("Test_1");
		rm.setSpeciesList(speciesList);
		
		try {
			rm.setPresenceMap("./resource files/patchtest.txt",species);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Map<String, double[]> p_discovery = new TreeMap<String, double[]>();
		p_discovery.put(species, new double[]{1,1,1});
		
		pm.setPDiscovery(p_discovery);
		pm.setCoreBufferSize(2);
		dict.put("Containment", "2");
		dict.put("Ground Control", "1");
		dict.put("Containment Core", "3");
		
		}

	@Test
	public void test() {
		Map<Integer, Patch> patches = rm.getPatches();
		
		for(Integer key: patches.keySet()){
			if(patches.get(key).hasNoData()){
				continue;
			}
			assertEquals(0,patches.get(key).getOccupant(species).getControls().size());
		}
		
		pm.process(rm);
		pc.process(rm);
		
		assertEquals(27870, pc.getCost(),1E-16);
		assertEquals(391, pc.getLabour(),1E-16);
			
	}
}
