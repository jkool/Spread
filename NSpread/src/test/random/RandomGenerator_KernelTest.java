package test.random;

//import static org.junit.Assert.*;

import spread.impl.random.RandomGenerator_Kernel;

import org.junit.Before;
import org.junit.Test;

import cern.colt.Arrays;

public class RandomGenerator_KernelTest {

	RandomGenerator_Kernel rk = new RandomGenerator_Kernel();
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		rk.setWeights(new double[]{0,1});
		rk.setRotate(false);
		double[] da = new double[10];
		for(int i = 0; i < da.length; i++){
			da[i] = rk.getNext().doubleValue();
		}
		
		System.out.println(Arrays.toString(da));
		
		rk.setWeights(new double[]{1,0});
		for(int i = 0; i < da.length; i++){
			da[i] = rk.getNext().doubleValue();
		}
		System.out.println(Arrays.toString(da));
		
		rk.setRotate(true);
		
		rk.setWeights(new double[]{0,1});
		da = new double[10];
		for(int i = 0; i < da.length; i++){
			da[i] = rk.getNext().doubleValue();
		}
		
		System.out.println(Arrays.toString(da));
		
		rk.setWeights(new double[]{1,0});
		for(int i = 0; i < da.length; i++){
			da[i] = rk.getNext().doubleValue();
		}
		System.out.println(Arrays.toString(da));
	}

}
