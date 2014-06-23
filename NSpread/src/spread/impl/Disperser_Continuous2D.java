package spread.impl;

import java.util.ArrayList;
import java.util.List;

import spread.Disperser;
import spread.RandomGenerator;

import spread.impl.random.RandomGenerator_Exponential;
import spread.impl.random.RandomGenerator_Kernel;
import spread.impl.random.RandomGenerator_Poisson;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Generates a spread of Coordinates based on a combination of angle, distance
 * and number distributions.
 * 
 */

public class Disperser_Continuous2D implements Disperser {

	private String level = "None";
	private RandomGenerator angleGenerator = new RandomGenerator_Kernel();
	private RandomGenerator distanceGenerator = new RandomGenerator_Exponential();
	private RandomGenerator numberGenerator = new RandomGenerator_Poisson();
	private Coordinate position = new Coordinate(0, 0);
	private final double tau = 2 * Math.PI;

	/**
	 * Adds two coordinates together. Used to transform from relative position
	 * to absolute by adding to the positional Coordinate.
	 * 
	 * @param a
	 *            - the first Coordinate
	 * @param b
	 *            - the second Coordinate
	 * @return - The resultant Coordinate
	 */

	private Coordinate add(Coordinate a, Coordinate b) {
		return new Coordinate(a.x + b.x, a.y + b.y);
	}

	/**
	 * Returns a clone/copy of the instance
	 */

	@Override
	public Disperser_Continuous2D clone() {
		Disperser_Continuous2D sd = new Disperser_Continuous2D();
		sd.setAngleGenerator(angleGenerator.clone());
		sd.setDistanceGenerator(distanceGenerator.clone());
		sd.setNumberGenerator(numberGenerator.clone());
		sd.setPosition(position);
		return sd;
	}

	// --------------------------------------------------------------------------------------------

	/**
	 * Generates a List of Coordinates with a size equal to the result obtained
	 * from the numberGenerator.
	 */

	@Override
	public List<Coordinate> disperse() {
		return disperse(numberGenerator.getNext().intValue());
	}

	/**
	 * Generates a List of Coordinates with a size equal to the parameter
	 * provided.
	 * 
	 * @param n
	 *            - the number of Coordinates to be generated.
	 * @return - a List of Coordinate values
	 */

	public List<Coordinate> disperse(int n) {
		List<Coordinate> dispersePoints = new ArrayList<Coordinate>();

		for (int i = 0; i < n; i++) {
			double angle = angleGenerator.getNext().doubleValue() * tau;
			double distance = distanceGenerator.getNext().doubleValue();
			double x = distance * Math.cos(angle);
			double y = distance * Math.sin(angle);

			// Add the displacement to the origina position to get absolute
			// location. This is done here rather than at the level
			// of Cell because cell could release at random positions within
			// the Geometry.

			Coordinate c = add(position, new Coordinate(x, y));
			dispersePoints.add(c);
		}
		return dispersePoints;
	}

	/**
	 * Returns the RandomGenerator being used to determine angle values.
	 * 
	 * @return - the RandomGenerator being used to determine angle values
	 */

	public RandomGenerator getAngleGenerator() {
		return angleGenerator;
	}

	/**
	 * Returns the RandomGenerator being used to determine distance values.
	 * 
	 * @return - the RandomGenerator being used to determine distance values
	 */

	public RandomGenerator getDistanceGenerator() {
		return distanceGenerator;
	}

	/**
	 * Returns the current level of the Disperser. Used to change parameter
	 * values using a String.
	 */

	@Override
	public String getLevel() {
		return level;
	}

	/**
	 * Returns the RandomGenerator being used to determine the number of
	 * Coordinate values generated per call.
	 * 
	 * @return - the RandomGenerator being used to determine the number of
	 *         Coordinate values generated per call.
	 */

	public RandomGenerator getNumberGenerator() {
		return numberGenerator;
	}

	/**
	 * Returns the reference point of the Disperser (i.e. where are the
	 * dispersers originating).
	 */

	@Override
	public Coordinate getPosition() {
		return position;
	}

	/**
	 * Sets the RandomGenerator being used to determine angle values.
	 * 
	 * @param angleGenerator
	 */

	public void setAngleGenerator(RandomGenerator angleGenerator) {
		this.angleGenerator = angleGenerator;
	}

	/**
	 * Sets the RandomGenerator being used to determine distance values.
	 * 
	 * @param distanceGenerator
	 */

	public void setDistanceGenerator(RandomGenerator distanceGenerator) {
		this.distanceGenerator = distanceGenerator;
	}

	/**
	 * Sets the RandomGenerator being used to determine the number of Coordinate
	 * values generated per call.
	 * 
	 * @param numberGenerator
	 */

	@Override
	public void setNumberGenerator(RandomGenerator numberGenerator) {
		this.numberGenerator = numberGenerator;
	}

	/**
	 * Sets the reference point of the Disperser (i.e. where are the dispersers
	 * originating).
	 */

	@Override
	public void setPosition(Coordinate position) {
		this.position = position;
	}
}