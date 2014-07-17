package spread.impl.random;

import spread.RandomGenerator;
import cern.jet.random.Exponential;

/**
 * Number generator that implements the RandomGenerator interface, and returns
 * pseudo-random numbers drawn from an exponential distribution. This class is
 * primarily a wrapper for the Colt package's Exponential class.
 * 
 */

public class RandomGenerator_Exponential implements RandomGenerator, Cloneable {

	public double lambda = 1;

	/**
	 * No-argument constructor
	 */

	public RandomGenerator_Exponential() {
	}

	/**
	 * Constructor that accepts a (lambda) value to be used as the exponential
	 * rate parameter.
	 * 
	 * @param lambda
	 *            - the (lambda) parameter to be used by the exponential
	 *            function.
	 */

	public RandomGenerator_Exponential(double lambda) {
		this.lambda = lambda;
	}

	/**
	 * Returns a clone/copy of the instance.
	 */

	@Override
	public RandomGenerator_Exponential clone() {
		RandomGenerator_Exponential rg = new RandomGenerator_Exponential();
		rg.lambda=lambda;
		return rg;
	}

	/**
	 * Returns the lambda value (rate) of the distribution.
	 * 
	 * @return - the lambda value (rate) of the distribution
	 */

	public double getLambda() {
		return lambda;
	}

	/**
	 * Returns the next pseudo-random value from the generator.
	 */

	@Override
	public Number getNext() {
		if(lambda==Double.POSITIVE_INFINITY){
			return 0;
		}
		return Exponential.staticNextDouble(lambda);
	}

	/**
	 * Sets the lambda value (rate) of the distribution.
	 * 
	 * @param lambda - the rate used by the distribution.
	 */

	public void setLambda(double lambda) {
		this.lambda = lambda;
	}
}
