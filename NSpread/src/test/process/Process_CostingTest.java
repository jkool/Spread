package test.process;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import spread.impl.RasterMosaic;
import spread.impl.process.Process_Costing;
import spread.impl.process.Process_Monitor;
import spread.util.ControlType;

import org.junit.Before;
import org.junit.Test;

import spread.Patch;

public class Process_CostingTest {

	RasterMosaic rm = new RasterMosaic();
	Process_Monitor pm = new Process_Monitor();
	Process_Costing pc = new Process_Costing();
	String species = "Test_1";
	String species2 = "Test_2";
	String species3 = "Test_3";

	@Before
	public void setUp() throws Exception {
		List<String> speciesList = new ArrayList<String>();
		speciesList.add(species);
		speciesList.add(species2);
		speciesList.add(species3);
		rm.setSpeciesList(speciesList);

		try {
			rm.setPresenceMap("./resource files/monitor_1.txt", species);
			rm.setPresenceMap("./resource files/monitor_2.txt", species2);
			rm.setPresenceMap("./resource files/monitor_3.txt", species3);
			rm.setHabitatMap("ALL", species);
			rm.setHabitatMap("ALL", species2);
			rm.setHabitatMap("ALL", species3);

		} catch (IOException e) {
			e.printStackTrace();
		}

		Map<String, double[]> p_discovery = new TreeMap<String, double[]>();
		p_discovery.put(species, new double[] { 1, 1, 1 });
		p_discovery.put(species2, new double[] { 1, 1, 1 });
		p_discovery.put(species3, new double[] { 1, 1, 1 });

		pc.setContainmentCost(1);
		pc.setContainmentLabour(1);

		Map<String, double[]> gcCost = new TreeMap<String, double[]>();
		Map<String, double[]> gcLabour = new TreeMap<String, double[]>();
		Set<String> coreControl = new TreeSet<String>();

		gcCost.put(species, new double[] { 1E3, 2E3, 3E3 });
		gcCost.put(species2, new double[] { 1E6, 2E6, 3E6 });
		gcCost.put(species3, new double[] { 1E9, 2E9, 3E9 });

		gcLabour.put(species, new double[] { 1E-3, 2E-3, 3E-3 });
		gcLabour.put(species2, new double[] { 1E-6, 2E-6, 3E-6 });
		gcLabour.put(species3, new double[] { 1E-9, 2E-9, 3E-9 });
		
		coreControl.add(species2);

		pc.setGroundControlCosts(gcCost);
		pc.setGroundControlLabour(gcLabour);

		pm.setPDiscovery(p_discovery);
		pm.setContainmentCutoff(6);
		pm.setCoreBufferSize(2);
		pm.ignoreFirst(false);
		pm.addToContainmentIgnore(species3);
		pm.addToCoreControl(species2);

	}

	@Test
	public void testCostingProcess() {
		Map<Integer, Patch> patches = rm.getPatches();

		for (Integer key : patches.keySet()) {
			if (patches.get(key).hasNoData()) {
				continue;
			}
			assertEquals(0, patches.get(key).getControls(species).size());
		}

		pm.process(rm);
		pc.process(rm);
		
		assertEquals(30,rm.getControlled(ControlType.CONTAINMENT).size()); // 35 - 5 core
		assertEquals(5,rm.getControlled(ControlType.CONTAINMENT_CORE).size()); // 5 core (2 cells in)
		assertEquals(6,rm.getControlled(species,ControlType.GROUND_CONTROL).size());
		assertEquals(2,rm.getControlled(species2,ControlType.CONTAINMENT_CORE_CONTROL).size());
		assertEquals(7,rm.getControlled(species2,ControlType.GROUND_CONTROL).size());
		assertEquals(31,rm.getControlled(species3,ControlType.GROUND_CONTROL).size());

		//31 sp3gc + 6 sp2gc + 2 sp2cc (=8) +6 sp1gc + 30 containment
		assertEquals(31008006030d, pc.getCostTotal(), 1E-8);
		
		// Numbers are slightly different since ground control for species 2 now exceeds
		// species 3. 30 containment + 6sp1gc + 7sp2gc + 2 sp2cc + 30 sp3gc
		assertEquals(30.006009030d, pc.getLabourTotal(), 1E-8);
	}
}
