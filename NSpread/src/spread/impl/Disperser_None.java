package spread.impl;

import java.util.ArrayList;
import java.util.List;

import spread.Disperser;
import spread.RandomGenerator;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Generates a spread of Coordinates based on a combination of angle, distance
 * and number distributions.
 * 
 */

public class Disperser_None implements Disperser {

	/**
	 * Returns a clone/copy of the instance
	 */

	@Override
	public Disperser_None clone() {
		Disperser_None sd = new Disperser_None();
		return sd;
	}

	/**
	 * Generates a List of Coordinates with a size equal to the result obtained
	 * from the numberGenerator.
	 */

	@Override
	public List<Coordinate> disperse() {
		return new ArrayList<Coordinate>();
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
		return new ArrayList<Coordinate>();
	}

	/**
	 * Returns the RandomGenerator being used to determine angle values.
	 * 
	 * @return - the RandomGenerator being used to determine angle values
	 */

	public RandomGenerator getAngleGenerator() {
		return null;
	}

	/**
	 * Returns the RandomGenerator being used to determine distance values.
	 * 
	 * @return - the RandomGenerator being used to determine distance values
	 */

	public RandomGenerator getDistanceGenerator() {
		return null;
	}

	/**
	 * Returns the current level of the Disperser. Used to change parameter
	 * values using a String.
	 */

	@Override
	public String getLevel() {
		return "";
	}

	/**
	 * Returns the RandomGenerator being used to determine the number of
	 * Coordinate values generated per call.
	 * 
	 * @return - the RandomGenerator being used to determine the number of
	 *         Coordinate values generated per call.
	 */

	public RandomGenerator getNumberGenerator() {
		return null;
	}

	/**
	 * Returns the reference point of the Disperser (i.e. where are the
	 * dispersers originating).
	 */

	@Override
	public Coordinate getPosition() {
		return null;
	}

	/**
	 * Sets the RandomGenerator being used to determine angle values.
	 * 
	 * @param angleGenerator
	 */

	public void setAngleGenerator(RandomGenerator angleGenerator) {
	}

	/**
	 * Sets the RandomGenerator being used to determine distance values.
	 * 
	 * @param distanceGenerator
	 */

	public void setDistanceGenerator(RandomGenerator distanceGenerator) {
	}

	/**
	 * Sets the RandomGenerator being used to determine the number of Coordinate
	 * values generated per call.
	 * 
	 * @param numberGenerator
	 */

	@Override
	public void setNumberGenerator(RandomGenerator numberGenerator) {
	}

	/**
	 * Sets the reference point of the Disperser (i.e. where are the dispersers
	 * originating).
	 */

	@Override
	public void setPosition(Coordinate position) {
	}
}