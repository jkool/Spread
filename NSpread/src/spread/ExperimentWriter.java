package spread;

import java.io.IOException;
import java.util.Set;

/**
 *  Interface for writing Experiment Output
 */

public interface ExperimentWriter {
	
	/**
	 * Closes the writer
	 */
	public void close();
	
	/**
	 * Opens the writer
	 * @param speciesSet - the set of Species to be written (they must exist)
	 * @throws IOException
	 */
	public void open(Set<String> speciesSet) throws IOException;
	
	/**
	 * Writes the Experiment data to output
	 * @param exp
	 */
	public void write(Experiment exp);

}
