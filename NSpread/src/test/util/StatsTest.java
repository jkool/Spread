package test.util;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.TreeMap;

import nsp.util.Stats;
import nsp.Occupant;

import org.junit.Before;
import org.junit.Test;

public class StatsTest {

	Stats stats = new Stats();
	String species = "Test";
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testConfusionMatrix(){
		Map<Integer,Occupant> truth = new TreeMap<Integer,Occupant>();
		Map<Integer,Occupant> estimate = new TreeMap<Integer,Occupant>();
		Occupant infested = new Occupant("Test");
		infested.setInfested(true);
		
		Occupant not_infested = new Occupant("Test");
		not_infested.setInfested(false);
		
		estimate.put(0, not_infested);
		estimate.put(1, not_infested);
		estimate.put(2, not_infested);
		estimate.put(3, not_infested);
		estimate.put(4, not_infested);
		estimate.put(5, not_infested);
		estimate.put(6, not_infested);
		estimate.put(7, infested);
		estimate.put(8, infested);
		estimate.put(9, infested);
		
		truth.put(0, not_infested);
		truth.put(1, not_infested);
		truth.put(2, not_infested);
		truth.put(3, not_infested);
		truth.put(4, infested);
		truth.put(5, infested);
		truth.put(6, infested);
		truth.put(7, not_infested);
		truth.put(8, not_infested);
		truth.put(9, infested);
		

		
		int[][] cmatrix = stats.makeConfusionMatrix(truth, estimate);
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
