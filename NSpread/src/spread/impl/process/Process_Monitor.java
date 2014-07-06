package spread.impl.process;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import spread.Mosaic;
import spread.Infestation;
import spread.Patch;
import spread.Process;

import cern.jet.random.Uniform;
import spread.util.ControlType;

/**
 * Performs operations on a Mosaic pertaining to monitoring infestation status.
 * 
 */

public class Process_Monitor implements Process, Cloneable {

	private Map<String, double[]> p_discovery;
	private Mosaic ms;
	private double containmentCutoff = 8;
	private double coreBufferSize = 3;
	private long counter = 0;
	private long timeIncrement = 1;
	private long chkFrq = 1;
	private Set<Infestation> visited = new HashSet<Infestation>();
	private Set<String> coreControl = new HashSet<String>();
	boolean ignoreFirst = true;
	boolean first = true;

	/**
	 * Returns a copy of the current instance of the class.
	 */
	
	@Override
	public Process_Monitor clone() {
		Process_Monitor clone = new Process_Monitor();
		Map<String, double[]> cp_discovery = new TreeMap<String, double[]>();
		Iterator<String> it = p_discovery.keySet().iterator();
		while(it.hasNext()){
			String species = it.next();
			cp_discovery.put(species, Arrays.copyOf(p_discovery.get(species), p_discovery.get(species).length));
		}
		clone.setPDiscovery(cp_discovery);
		clone.containmentCutoff=containmentCutoff;
		clone.coreBufferSize=coreBufferSize;
		clone.timeIncrement=timeIncrement;
		clone.counter=counter;
		clone.ms=ms;
		clone.visited=visited;
		return clone;
	}

	/**
	 * Processes all patches in the Mosaic
	 */
	
	@Override
	public void process(Mosaic mosaic) {
		
		counter+=timeIncrement;
		
		if(ignoreFirst && first){
			first=false;
			return;
		}
		
		if(counter<chkFrq){
			return;
		}
		
		counter = 0;
		
		this.ms=mosaic;
		
		for (Integer key : mosaic.getPatches().keySet()) {
			process(mosaic.getPatches().get(key));
		}
		
		ms.clearVisitedInfestations(visited);
		visited.clear();
	}

	/**
	 * Carries out monitoring actions for a single patch.  Includes detection
	 * and assignment to a control type.
	 * 
	 * @param patch
	 *            - The patch to be processed
	 */
	
	private void process(Patch patch) {
		
		if(patch.hasNoData()){
			return;
		}

		Iterator<String> it = patch.getInfestation().keySet().iterator();
		while (it.hasNext()) {
			
			String species = it.next();
			Infestation o = patch.getInfestation(species);
			
			// If this Patch has been processed already as part of a chain, continue
			
			if(o.isVisited()){
				continue;
			}
			
			if(!o.isInfested()&& o.hasControl(ControlType.GROUND_CONTROL)){
				o.removeControl(ControlType.GROUND_CONTROL);
			}
			
			// If the patch is infested, apply a random number to determine whether it was detected
			
			if (o.isInfested()) {///////////////////////////////////////////////////// Should we check if it is already under control?
				double p = Uniform.staticNextDouble();
				int stage = o.getStageOfInfestation();
				if (p < p_discovery.get(species)[stage - 1]) {
					patch.setMonitored(true);
					
					// Detect the region of infestation, and fill to get the bounded area
					
					Set<Patch> region = ms.getWeakRegion(patch, species);
					Set<Patch> filled = ms.fill(region, species);
					
					// Determine cells that are already controlled

					Set<Patch> controlled = getControlled(region,species,ControlType.CONTAINMENT);

					// If the total area is less than the containment cutoff size, put cells into ground control
					
					if(filled.size()-controlled.size()<=containmentCutoff){
						ms.setMonitored(region, true);
						region.removeAll(controlled);
						ms.setControl(region, ControlType.GROUND_CONTROL,species);
					}
					
					// Otherwise assign all cells in the bounded area to containment
				
					else{
							ms.setMonitored(filled, true);
						    ms.setControl(filled, ControlType.CONTAINMENT);

						    Set<Patch> core = ms.getStrongCore(filled, species, coreBufferSize);
					    	ms.setControl(core, ControlType.CONTAINMENT_CORE);
					    	ms.removeControl(core, ControlType.CONTAINMENT);
						    
							Iterator<String> it2 = patch.getInfestation().keySet().iterator();

							while(it2.hasNext()){
						    	String species2 = it2.next();
						    	ms.removeControl(filled, ControlType.GROUND_CONTROL, species2); ////////Unless in core control!!!!
						    }
					}
					
					// Mark any cells visited as part of the process chain as processed
					
					ms.setVisited(filled,species);
					
					for(Patch v:filled){
						visited.add(v.getInfestation(species));
					}
				}
			}
		}
	}
	
	// Getters and setters
	
	/**
	 * Filters a set of patches to indicate which are controlled for a given species and control type.
	 * 
	 * @param patches - The patches to be filtered
	 * @param species - The species to which the controls are applied
	 * @param control - The control type being used
	 * @return
	 */

	private Set<Patch> getControlled(Set<Patch> patches, String species, ControlType control){
		Set<Patch> controlled = new TreeSet<Patch>();
		for(Patch p:patches){
			if(p.getInfestation(species).hasControl(control)){
				controlled.add(p);
			}
		}
		return controlled;
	}
	
	/**
	 * Sets the frequency with which costing actions occur.
	 * 
	 * @param checkFrequency
	 */
	
	public void setCheckFrequency(long checkFrequency){
		this.chkFrq=checkFrequency;
	}
	
	/**
	 * Sets the number of cells required before an area is assigned to containment
	 * 
	 * @param containmentCutoff
	 */
	
	public void setContainmentCutoff(double containmentCutoff){
		this.containmentCutoff=containmentCutoff;
	}
	
	/**
	 * Sets the number of cells away from the edge that a Patch needs to be before it
	 * is considered to be a containment core area.
	 * 
	 * @param coreBufferSize
	 */
	
	public void setCoreBufferSize(double coreBufferSize){
		this.coreBufferSize=coreBufferSize;
	}
	
	/**
	 * Sets the probability (associated with growth stage) of discovering whether a patch is infested.
	 * 
	 * @param p_discovery
	 */
	
	public void setPDiscovery(Map<String, double[]> p_discovery) {
		this.p_discovery = p_discovery;
	}
	
	public void ignoreFirst(boolean ignore){
		this.ignoreFirst=ignore;
	}
	
	/**
	 * Sets the amount of time currently being incremented by the growth process
	 * 
	 * @param timeIncrement
	 */
	
	public void setTimeIncrement(long timeIncrement){
		this.timeIncrement=timeIncrement;
	}
	
	public void reset(){
		this.first=true;
	}
}