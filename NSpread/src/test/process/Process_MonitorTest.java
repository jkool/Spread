/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package test.process;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import spread.impl.RasterMosaic;
import spread.impl.process.Process_Monitor;
import spread.util.ControlType;

import org.junit.Before;
import org.junit.Test;

import spread.Patch;

public class Process_MonitorTest {

	RasterMosaic rm = new RasterMosaic();
	Process_Monitor pm = new Process_Monitor();
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
		pm.setContainmentCutoff(7);
	}

	@Test
	public void test() {
		Map<Integer, Patch> patches = rm.getPatches();

		for (Integer key : patches.keySet()) {
			if (patches.get(key).hasNoData()) {
				continue;
			}
			assertEquals(0, patches.get(key).getControls(species).size());
		}

		pm.process(rm);

		assertTrue(rm.getPatch(0).getControls(species).isEmpty());
		assertTrue(rm.getPatch(7).getControls().contains(ControlType.CONTAINMENT));
		assertTrue(!rm.getPatch(7).getControls().contains(ControlType.CONTAINMENT_CORE));
		assertTrue(!rm.getPatch(7).getControls(species)
				.contains(ControlType.GROUND_CONTROL));
		
		assertTrue(rm.getPatch(10).getControls()
				.contains(ControlType.CONTAINMENT));
		assertTrue(!rm.getPatch(10).getControls(species)
				.contains(ControlType.GROUND_CONTROL));
		assertTrue(!rm.getPatch(7).getControls()
				.contains(ControlType.CONTAINMENT_CORE));
		assertTrue(rm.getPatch(105).getControls(species)
				.contains(ControlType.GROUND_CONTROL));
		assertTrue(!rm.getPatch(105).getControls()
				.contains(ControlType.CONTAINMENT));
		assertTrue(!rm.getPatch(7).getControls()
				.contains(ControlType.CONTAINMENT_CORE));
		assertTrue(!rm.getPatch(60).getControls(species)
				.contains(ControlType.GROUND_CONTROL));
		assertTrue(!rm.getPatch(60).getControls()
				.contains(ControlType.CONTAINMENT));
		assertTrue(rm.getPatch(60).getControls()
				.contains(ControlType.CONTAINMENT_CORE));
	}
}
