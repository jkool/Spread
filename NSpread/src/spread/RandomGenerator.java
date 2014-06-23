package spread;

/**
 * Interface for random number generation. The clone method ensures that
 * independent copies of the generator can be made.
 * 
 */

public interface RandomGenerator {

	/**
	 * Returns a clone/copy of the instance
	 */

	public RandomGenerator clone();

	/**
	 * Returns the next value from the generator
	 */

	public Number getNext();
}
