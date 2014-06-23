package test;

import java.io.IOException;
import java.util.Set;

import spread.Experiment;
import spread.ExperimentWriter;


/**
 * Empty ExperimentWriter class for unit testing.
 */

public class ExperimentWriter_Null implements ExperimentWriter {
	
	@Override
	public void close(){}
	@Override
	public void open(Set<String> speciesList) throws IOException{}
	@Override
	public void write(Experiment exp){}

}
