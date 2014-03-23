package nsp;

/**
 * Interface for handling processes that alter the population in some manner
 * 
 */

public interface Process {

	/**
	 * Returns a clone/copy of the instance
	 */

	public abstract Process clone();

	/**
	 * Processes all patches in the Mosaic
	 */

	public abstract void process(Mosaic mosaic);
}