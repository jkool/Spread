package spread.impl.random;

import spread.RandomGenerator;

/**
 * Number generator that implements the RandomGenerator interface, and returns
 * pseudo-random numbers drawn from a Von Mises distribution.
 * 
 */

public class RandomGenerator_VonMises implements RandomGenerator, Cloneable {

	private double mean = 0;
	private double k = 0;

	// private double n = 1;

	/**
	 * No-argument constructor
	 */

	public RandomGenerator_VonMises() {
	}

	/**
	 * Constructor that accepts parameter values to be used by the function.
	 * 
	 * @param mean
	 *            - the mean value of the distribution.
	 * @param k
	 *            - the spread of the distribution (analogous to variance)
	 */

	public RandomGenerator_VonMises(double mean, double k) {
		this.mean = mean;
		this.k = k;
	}

	/**
	 * Returns a clone/copy of the instance.
	 */

	@Override
	public RandomGenerator_VonMises clone() {
		RandomGenerator_VonMises vm = new RandomGenerator_VonMises();
		vm.setMean(mean);
		vm.setK(k);
		return vm;
	}

	/**
	 * Retrieves the spread (analogous to variance) of the distribution
	 * 
	 * @return the spread (analogous to variance) of the distribution
	 */

	public double getK() {
		return k;
	}

	/**
	 * Retrieves the mean value of the distribution.
	 * 
	 * @return - the mean value of the distribution
	 */

	public double getMean() {
		return mean;
	}

	/**
	 * Returns the next pseudo-random value from the generator.
	 */

	@Override
	public Number getNext() {
		return getNext(mean, k);
	}

	/**
	 * Returns the next pseudo-random value from the generator using the
	 * provided parameters.
	 */

	public Number getNext(double mean, double k) {

		if (k == 0) {
			return Math.random() * 2 * Math.PI - Math.PI;
		}

		double result = 0.0;

		double a = 1.0 + Math.sqrt(1 + 4.0 * (k * k));
		double b = (a - Math.sqrt(2.0 * a)) / (2.0 * k);
		double r = (1.0 + b * b) / (2.0 * b);

		while (true) {
			double U1 = Math.random();
			double z = Math.cos(Math.PI * U1);
			double f = (1.0 + r * z) / (r + z);
			double c = k * (r - f);
			double U2 = Math.random();

			if (c * (2.0 - c) - U2 > 0.0) {
				double U3 = Math.random();
				double sign = 0.0;
				if (U3 - 0.5 < 0.0)
					sign = -1.0;
				if (U3 - 0.5 > 0.0)
					sign = 1.0;
				result = sign * Math.acos(f) + mean;
				while (result >= 2.0 * Math.PI)
					result -= 2.0 * Math.PI;
				break;
			} else {
				if (Math.log(c / U2) + 1.0 - c >= 0.0) {
					double U3 = Math.random();
					double sign = 0.0;
					if (U3 - 0.5 < 0.0)
						sign = -1.0;
					if (U3 - 0.5 > 0.0)
						sign = 1.0;
					result = sign * Math.acos(f) + mean;
					while (result >= 2.0 * Math.PI)
						result -= 2.0 * Math.PI;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Sets the spread parameter to be used by the distribution (analogous to
	 * variance)
	 * 
	 * @param k
	 *            - the spread parameter to be used by the distribution
	 *            (analogous to variance)
	 */

	public void setK(double k) {
		this.k = k;
	}

	/**
	 * Sets the mean value to be used by the distribution.
	 * 
	 * @param mean
	 *            - the mean value to be used by the distribution.
	 */

	public void setMean(double mean) {
		this.mean = mean;
	}
}
