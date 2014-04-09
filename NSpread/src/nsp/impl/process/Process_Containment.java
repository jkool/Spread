package nsp.impl.process;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import nsp.Mosaic;
import nsp.Patch;
import nsp.Process;
import nsp.util.ManagementTypes;

public class Process_Containment implements Process, Cloneable{
	
	private long timeIncrement = 1;
	private Set<String> ignore = new TreeSet<String>();
	
	public void process(Mosaic mosaic) {
		for (Integer key : mosaic.getPatches().keySet()) {
			process(mosaic.getPatches().get(key));
		}
	}

	private void process(Patch patch) {

		for (String species : patch.getOccupants().keySet()) {
			
			if(ignore.contains(species)){
				continue;
			}

			// I'm gonna break my rusty cage... and run!

			if (patch.getOccupant(species).hasControl(ManagementTypes.CONTAINMENT_CORE.displayName())) {
				continue;
			}
			
			if (patch.getOccupant(species).hasControl(ManagementTypes.CONTAINMENT.displayName())) {
				
				patch.getOccupant(species).setStageOfInfestation(0);
				patch.getOccupant(species).clearInfestation();
			
			}
		}
	}
	
	public Process_Containment clone(){
		Process_Containment clone = new Process_Containment();
		return clone;
	}
	
	public void setTimeIncrement(long timeIncrement) {
		this.timeIncrement = timeIncrement;
	}
	
	public void setIgnoreList(Collection<String> ignore){
		this.ignore = new TreeSet<String>(ignore);
	}
	
	public void addToIgnoreList(String species){
		this.ignore.add(species);
	}
	
	public void addToIgnoreList(Collection<String> species){
		this.ignore.addAll(species);
	}
	
	public void removeFromIgnoreList(String species){
		this.ignore.remove(species);
	}
}
