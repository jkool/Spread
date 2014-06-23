package spread;

import java.io.IOException;
import java.util.Set;

/**
 * Interface for writing output trace files
 */

public interface StatsWriter {
	/**
	 * Closes the output file
	 */
	public void close();
	
	/**
	 * Opens and initializes the output file
	 * @param speciesList
	 * @throws IOException
	 */
	
	public void open(Set<String> speciesList) throws IOException;
	
	/**
	 * Sets the name of the output file
	 * @param name - the name of the output file
	 */
	public void setOutputFile(String name);
	
	/**
	 * Sets the path of the output folder
	 * @param folder - the path of the output folder
	 */
	public void setOutputFolder(String folder);
	
	/**
	 * Writes to the output file
	 * @param exp - the Experiment which will provide the output values
	 */
	public void write(Experiment exp);	
}
