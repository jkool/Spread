/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package spread.impl.random;

import spread.RandomGenerator;
import cern.jet.random.Poisson;

/**
 * Number generator that implements the RandomGenerator interface, and returns
 * pseudo-random numbers drawn from a Poisson distribution. This class is
 * primarily a wrapper for the Colt package's Poisson class.
 * 
 */

public class RandomGenerator_Poisson implements RandomGenerator, Cloneable {
	private double lambda = 1.0d;

	/**
	 * No-argument constructor
	 */

	public RandomGenerator_Poisson() {
	}

	/**
	 * Constructor that accepts a (lambda) value to be used as a parameter to
	 * the Poisson function.
	 * 
	 * @param lambda
	 *            - the (lambda) parameter to be used by the Poisson function.
	 */

	public RandomGenerator_Poisson(double lambda) {
		this.lambda = lambda;
	}

	/**
	 * Returns a clone/copy of the instance.
	 */

	@Override
	public RandomGenerator_Poisson clone() {
		RandomGenerator_Poisson clone = new RandomGenerator_Poisson();
		clone.setLambda(lambda);
		return clone;
	}

	/**
	 * Returns the lambda value of the distribution.
	 * 
	 * @return - the lambda value of the distribution
	 */

	public double getLambda() {
		return lambda;
	}

	/**
	 * Returns the next pseudo-random value from the generator.
	 */

	@Override
	public Number getNext() {
		return Poisson.staticNextInt(lambda);
	}

	/**
	 * Sets the lambda value (rate) of the distribution.
	 * 
	 * @param lambda - the lambda value of the distribution.
	 */

	public void setLambda(double lambda) {
		if (lambda <= 0) {
			throw new IllegalArgumentException(
					"Poisson lambda parameter cannot be equal to or less than zero. Attempting to set to "
							+ lambda + ".");
		}
		this.lambda = lambda;
	}

}
