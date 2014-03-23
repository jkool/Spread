package test;

import java.io.IOException;
import java.util.Set;

import nsp.Experiment;
import nsp.ExperimentWriter;

/**
 * Empty ExperimentWriter class for unit testing.
 */

public class ExperimentWriter_Null implements ExperimentWriter {
	
	public void open(Set<String> speciesList) throws IOException{}
	public void close(){}
	public void write(Experiment exp){}

}
