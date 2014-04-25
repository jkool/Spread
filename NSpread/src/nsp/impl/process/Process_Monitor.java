package nsp.impl.process;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import cern.jet.random.Uniform;
import nsp.Mosaic;
import nsp.Occupancy;
import nsp.Patch;
import nsp.Process;
import nsp.util.ControlType;

public class Process_Monitor implements Process, Cloneable {

	private Map<String, double[]> p_discovery;
	private Mosaic ms;
	private double containmentCutoff = 8;
	private double coreBufferSize = 3;
	private long frequency = 0;
	private Set<Occupancy> visited = new HashSet<Occupancy>();

	public void process(Mosaic mosaic) {
		this.ms=mosaic;
		
		for (Integer key : mosaic.getPatches().keySet()) {
			process(mosaic.getPatches().get(key));
		}
		
		ms.clearVisitedOccupancies(visited);
		visited.clear();
	}

	private void process(Patch patch) {
		
		if(patch.hasNoData()){
			return;
		}

		Iterator<String> it = patch.getOccupants().keySet().iterator();
		while (it.hasNext()) {
			
			String species = it.next();
			Occupancy o = patch.getOccupant(species);
			
			if(o.isVisited()){
				continue;
			}
			
			if (o.isInfested()) {
				double p = Uniform.staticNextDouble();
				int stage = o.getStageOfInfestation();

				if (p < p_discovery.get(species)[stage - 1]) {
					patch.setMonitored(true);
					Set<Patch> region = ms.getWeakRegion(patch, species);
					Set<Patch> filled = ms.fill(region, species);
							
					if(ms.getArea(filled)<=containmentCutoff){
						ms.setMonitored(region, true);
						ms.setControl(region, species, ControlType.GROUND_CONTROL);
					}
					else{
						ms.setMonitored(filled, true);
						ms.setControl(filled, species, ControlType.CONTAINMENT);
						ms.setControl(ms.getStrongCore(filled, species, coreBufferSize), species, ControlType.CONTAINMENT_CORE);
						ms.removeControl(filled, species, ControlType.GROUND_CONTROL);						
					}
					
					ms.setVisited(filled,species);
					
					for(Patch v:filled){
						visited.add(v.getOccupant(species));
					}
				}
			}
		}
	}

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
		clone.frequency=frequency;
		clone.ms=ms;
		clone.visited=visited;
		return clone;
	}

	public void setPDiscovery(Map<String, double[]> p_discovery) {
		this.p_discovery = p_discovery;
	}
	
	public void setFrequency(long frequency){
		this.frequency=frequency;
	}
	
	public void setCoreBufferSize(double coreBufferSize){
		this.coreBufferSize=coreBufferSize;
	}
	
	public void setContainmentCutoff(double containmentCutoff){
		this.containmentCutoff=containmentCutoff;
	}	
}