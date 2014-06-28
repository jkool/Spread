package spread.impl.process;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import spread.Mosaic;
import spread.Occupant;
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
	private Set<Occupant> visited = new HashSet<Occupant>();

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
		
		if(counter<chkFrq){
			return;
		}
		
		counter = 0;
		
		this.ms=mosaic;
		
		for (Integer key : mosaic.getPatches().keySet()) {
			process(mosaic.getPatches().get(key));
		}
		
		ms.clearVisitedOccupancies(visited);
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

		Iterator<String> it = patch.getOccupants().keySet().iterator();
		while (it.hasNext()) {
			
			String species = it.next();
			Occupant o = patch.getOccupant(species);
			
			// If this Patch has been processed already as part of a chain, continue
			
			if(o.isVisited()){
				continue;
			}
			
			if(!o.isInfested()&& o.hasControl(ControlType.GROUND_CONTROL)){
				o.removeControl(ControlType.GROUND_CONTROL);
			}
			
			// If the patch is infested, apply a random number to determine whether it was detected
			
			if (o.isInfested()) {
				double p = Uniform.staticNextDouble();
				int stage = o.getStageOfInfestation();
				
				if (p < p_discovery.get(species)[stage - 1]) {
					patch.setMonitored(true);
					
					// Detect the region of infestation, and fill to get the bounded area
					
					Set<Patch> region = ms.getWeakRegion(patch, species);
					Set<Patch> filled = ms.fill(region, species);
							
					//if(ms.getArea(filled)<=containmentCutoff){
					//	ms.setMonitored(region, true);
					//	ms.setControl(region, species, ControlType.GROUND_CONTROL);
					
					// Determine cells that are already controlled

					Set<Patch> controlled = getControlled(region,species,ControlType.CONTAINMENT);

					// If the total area is less than the containment cutoff size, put cells into ground control
					
					if(filled.size()-controlled.size()<=containmentCutoff){
						ms.setMonitored(region, true);
						region.removeAll(controlled);
						ms.setControl(region,species,ControlType.GROUND_CONTROL);
					}
					
					// Otherwise assign all cells in the bounded area to containment
				
					else{
						
						ms.setMonitored(filled, true);
						ms.setControl(filled, species, ControlType.CONTAINMENT);
						ms.setControl(ms.getStrongCore(filled, species, coreBufferSize), species, ControlType.CONTAINMENT_CORE);
						ms.removeControl(filled, species, ControlType.GROUND_CONTROL);						
					}
					
					// Mark any cells visited as part of the process chain as processed
					
					ms.setVisited(filled,species);
					
					for(Patch v:filled){
						visited.add(v.getOccupant(species));
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
			if(p.getOccupant(species).hasControl(control)){
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
	
	/**
	 * Sets the amount of time currently being incremented by the growth process
	 * 
	 * @param timeIncrement
	 */
	
	public void setTimeIncrement(long timeIncrement){
		this.timeIncrement=timeIncrement;
	}
}