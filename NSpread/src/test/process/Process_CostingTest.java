package test.process;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import nsp.Patch;
import nsp.impl.RasterMosaic;
import nsp.impl.process.Process_Costing;
import nsp.impl.process.Process_Monitor;

import org.junit.Before;
import org.junit.Test;

public class Process_CostingTest {

	RasterMosaic rm = new RasterMosaic();
	Process_Monitor pm = new Process_Monitor();
	Process_Costing pc = new Process_Costing();
	String species = "Test_1";

	@Before
	public void setUp() throws Exception {
		List<String> speciesList = new ArrayList<String>();
		speciesList.add("Test_1");
		rm.setSpeciesList(speciesList);

		try {
			rm.setPresenceMap("./resource files/patchtest.txt", species);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Map<String, double[]> p_discovery = new TreeMap<String, double[]>();
		p_discovery.put(species, new double[] { 1, 1, 1 });

		pm.setPDiscovery(p_discovery);
		pm.setCoreBufferSize(2);

	}

	@Test
	public void test() {
		Map<Integer, Patch> patches = rm.getPatches();

		for (Integer key : patches.keySet()) {
			if (patches.get(key).hasNoData()) {
				continue;
			}
			assertEquals(0, patches.get(key).getOccupant(species).getControls()
					.size());
		}

		pm.process(rm);
		pc.process(rm);

		/*Map<Integer, Patch> patchlist = rm.getPatches();

		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 20; j++) {
				
				Patch pp = patchlist.get((i * 20) + j);
				if(pp.hasNoData()){System.out.print("NaN "); continue;}
				ArrayList<ControlType> alist = new ArrayList<ControlType>(
						pp.getOccupant("Test_1")
								.getControls().keySet());
				if (alist.size() == 0) {
					System.out.print("0");
				} else {
					System.out.print(alist.get(0).ordinal() + 1);
				}
				System.out.print(" ");
			}
			System.out.println();
		}*/

	 assertEquals(26590, pc.getCostTotal(),1E-16);
	 assertEquals(373, pc.getLabourTotal(),1E-16);

	}
}
