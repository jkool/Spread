package spread.impl.random;

import spread.RandomGenerator;

/**
 * Number generator that implements the RandomGenerator interface, but returns
 * deterministic values. Useful for debugging (i.e. no stochasticity).
 * 
 */

public class RandomGenerator_Determined implements RandomGenerator, Cloneable {
	double value = 0;

	/**
	 * No-argument constructor
	 */

	public RandomGenerator_Determined() {
	}

	/**
	 * Constructor that accepts the value to be returned as a parameter.
	 * 
	 * @param value
	 *            - the value to be returned by the generator when called.
	 */

	public RandomGenerator_Determined(double value) {
		this.value = value;
	}

	/**
	 * Returns a clone/copy of the instance
	 */

	@Override
	public RandomGenerator_Determined clone() {
		RandomGenerator_Determined rd = new RandomGenerator_Determined();
		rd.setValue(value);
		return rd;
	}

	/**
	 * Returns the next value from the generator (in this case, a single number
	 * equaling 'value')
	 */

	@Override
	public Number getNext() {
		return value;
	}

	/**
	 * Sets the value to be returned by the generator.
	 * 
	 * @param value
	 *            - the number to be returned.
	 */

	public void setValue(double value) {
		this.value = value;
	}
}
