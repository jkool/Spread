package nsp;

import java.io.IOException;
import java.util.Set;

public interface ExperimentWriter {
	
	public void open(Set<String> speciesList) throws IOException;
	public void close();
	public void write(Experiment exp);

}
