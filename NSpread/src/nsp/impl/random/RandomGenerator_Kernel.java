package nsp.impl.random;

import java.util.Arrays;

import nsp.RandomGenerator;
import cern.jet.random.Empirical;
import cern.jet.random.engine.RandomEngine;

/**
 * Number generator that implements the RandomGenerator interface, and returns
 * pseudo-random numbers drawn from an empirical distribution. This class is
 * primarily a wrapper for the Colt package's Empirical class.
 * 
 */

public class RandomGenerator_Kernel implements RandomGenerator, Cloneable {

	private double[] weights = new double[] { .125, .250, .375, .5, .625, .75,
			.875, 1 };
	private Empirical emp = new Empirical(weights,
			Empirical.LINEAR_INTERPOLATION, RandomEngine.makeDefault());
	private boolean rotate = true;
	private double inc = 1d / (weights.length * 2d);

	/**
	 * Returns a clone/copy of the instance.
	 */

	@Override
	public RandomGenerator_Kernel clone() {
		RandomGenerator_Kernel rgk = new RandomGenerator_Kernel();
		rgk.weights = Arrays.copyOf(this.weights, weights.length);
		rgk.emp = (Empirical) emp.clone();
		rgk.rotate = rotate;
		rgk.inc = inc;
		return rgk;
	}

	/**
	 * Returns the next pseudo-random value from the generator. If the rotate
	 * parameter is true, then the random number is rotated such that the
	 * midpoint of the first bin will fall at zero. e.g. If there are eight
	 * bins, and the random number falls in the first bin, .0625 is subtracted
	 * from the number. A modulus of 1 is then applied so that values will
	 * remain in the interval zero to 1. This way, the values can be
	 * post-multiplied by tau=2*pi to get an angle.
	 */

	@Override
	public Number getNext() {
		double val = emp.nextDouble();
		return rotate ? (1+(val - inc)) % 1d : val;
	}

	/**
	 * Sets whether probability values should be rotated so that the midpoint of
	 * the first bin falls on zero.
	 * 
	 * @param rotate
	 *            - indicates whether probability values should be rotated.
	 */

	public void setRotate(boolean rotate) {
		this.rotate = rotate;
	}

	/**
	 * Sets the weights (and number) of the bins
	 * 
	 * @param weights - the pdf to be used to weight the bins
	 */

	public void setWeights(double[] weights) {
		this.weights = weights;
		inc = 1d / (weights.length * 2d);
		emp.setState(weights, Empirical.LINEAR_INTERPOLATION);
	}
}
