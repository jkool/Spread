package spread;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Interface for generating Coordinates of dispersing propagules.
 * 
 */

public interface Disperser {

	/**
	 * Returns a clone/copy of the instance
	 */

	public Disperser clone();

	/**
	 * Generates a List of Coordinates with a size equal to the result obtained
	 * from the numberGenerator.
	 */

	public List<Coordinate> disperse();

	/**
	 * Returns the current level of the Disperser. Used to change parameter
	 * values using a String.
	 */

	public String getLevel();

	/**
	 * Returns the reference point of the Disperser (i.e. where are the
	 * dispersers originating).
	 */

	public Coordinate getPosition();

	/**
	 * Sets the RandomGenerator being used to determine the number of Coordinate
	 * values generated per call.
	 * 
	 * @param rg
	 *            - the RandomGenerator being used to determine the number of
	 *            Coordinate values generated per call.
	 */

	public void setNumberGenerator(RandomGenerator rg);

	/**
	 * Sets the reference point of the Disperser (i.e. where are the dispersers
	 * originating).
	 */

	public void setPosition(Coordinate c);
}