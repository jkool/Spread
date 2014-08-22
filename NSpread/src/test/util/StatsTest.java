/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package test.util;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.TreeMap;

import spread.util.Stats;

import org.junit.Before;
import org.junit.Test;

import spread.Infestation;
import spread.Patch;

public class StatsTest {

	Stats stats = new Stats();
	String species = "Test";
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testConfusionMatrix(){
		Map<Integer,Patch> truth = new TreeMap<Integer,Patch>();
		Map<Integer,Patch> estimate = new TreeMap<Integer,Patch>();
		Infestation infested = new Infestation(species);
		infested.setInfested(true);
		
		Infestation not_infested = new Infestation(species);
		not_infested.setInfested(false);
		
		Patch pi = new Patch();
		pi.addInfestation(infested);
		
		Patch pni = new Patch();
		
		estimate.put(0, pni);
		estimate.put(1, pni);
		estimate.put(2, pni);
		estimate.put(3, pni);
		estimate.put(4, pni);
		estimate.put(5, pni);
		estimate.put(6, pni);
		estimate.put(7, pi);
		estimate.put(8, pi);
		estimate.put(9, pi);
		
		truth.put(0, pni);
		truth.put(1, pni);
		truth.put(2, pni);
		truth.put(3, pni);
		truth.put(4, pi);
		truth.put(5, pi);
		truth.put(6, pi);
		truth.put(7, pni);
		truth.put(8, pni);
		truth.put(9, pi);
		
		int[][] cmatrix = stats.makeConfusionMatrix(truth, estimate, species);
		assertEquals(cmatrix[0][0],4);
		assertEquals(cmatrix[0][1],3);
		assertEquals(cmatrix[1][0],2);
		assertEquals(cmatrix[1][1],1);
	}
	
	@Test
	public void testPontiusness() {
		stats.pontiusStats(new int[][]{{710,8},{19,16}});
		assertEquals(stats.getKno(),.93,5E-3);
		assertEquals(stats.getKallocation(),.65,5E-3);
		assertEquals(stats.getKquantity(),.97,5E-3);
		assertEquals(stats.getKhisto(),.81,5E-3);
		assertEquals(stats.getKstandard(),.52,5E-3);
		assertEquals(stats.getChanceAgreement(),.5,5E-3);
		assertEquals(stats.getQuantityAgreement(),.42,5E-3);
		assertEquals(stats.getAllocationAgreement(),.04,5E-3);
		assertEquals(stats.getAllocationDisagreement(),.02,5E-3);
		assertEquals(stats.getQuantityDisagreement(),.01,5E-3);
		assertEquals(stats.getPierceSkill(),.64,5E-3);
		assertEquals(stats.getFigureOfMerit(),.96,5E-3);
		
		stats.pontiusStats(new int[][]{{685,17},{42,48}});

		assertEquals(stats.getKno(),.85,5E-3);
		assertEquals(stats.getKallocation(),.70,5E-3);
		assertEquals(stats.getKquantity(),.92,5E-3);
		assertEquals(stats.getKhisto(),.82,5E-3);
		assertEquals(stats.getKstandard(),.58,5E-3);
		assertEquals(stats.getChanceAgreement(),.5,5E-3);
		assertEquals(stats.getQuantityAgreement(),.32,5E-3);
		assertEquals(stats.getAllocationAgreement(),.10,5E-3);
		assertEquals(stats.getAllocationDisagreement(),.04,5E-3);
		assertEquals(stats.getQuantityDisagreement(),.03,5E-3);
		assertEquals(stats.getPierceSkill(),.68,5E-3);
		assertEquals(stats.getFigureOfMerit(),.92,5E-3);		
	}
}
