package nsp;

import java.io.IOException;
import java.util.Set;

public interface StatsWriter {
	
	public void open(Set<String> speciesList) throws IOException;
	public void close();
	public void write(Experiment exp);
	public void setOutputFolder(String folder);
	public void setOutputFile(String name);
	
}
