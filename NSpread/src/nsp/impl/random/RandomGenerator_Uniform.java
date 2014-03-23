package nsp.impl.random;

import nsp.RandomGenerator;
import cern.jet.random.Uniform;

/**
 * Number generator that implements the RandomGenerator interface, and returns
 * pseudo-random numbers drawn from a Uniform distribution. This class is
 * primarily a wrapper for the Colt package's Uniform class.
 * 
 */

public class RandomGenerator_Uniform implements RandomGenerator, Cloneable {

	/**
	 * Returns a clone/copy of the instance.
	 */

	@Override
	public RandomGenerator_Uniform clone() {
		return new RandomGenerator_Uniform();
	}

	/**
	 * Returns the next pseudo-random value from the generator.
	 */

	@Override
	public Number getNext() {
		return Uniform.staticNextDouble();
	}
}
