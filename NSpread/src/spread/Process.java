/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package spread;

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
	
	/**
	 * Re-initializes the process
	 */
	
	public void reset();
}