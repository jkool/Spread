package nsp.impl.process;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import cern.jet.random.Uniform;
import nsp.Mosaic;
import nsp.Occupancy;
import nsp.Patch;
import nsp.Process;

public class Process_Monitor implements Process, Cloneable {

	private Map<String, double[]> p_discovery;
	private Mosaic ms;
	private long frequency = 0;

	public void process(Mosaic mosaic) {
		this.ms=mosaic;
		for (Integer key : mosaic.getPatches().keySet()) {
			process(mosaic.getPatches().get(key));
		}
	}

	private void process(Patch patch) {

		Iterator<String> it = patch.getOccupants().keySet().iterator();
		while (it.hasNext()) {
			String species = it.next();
			Occupancy o = patch.getOccupant(species);
			if (o.isInfested()) {
				double p = Uniform.staticNextDouble();
				int stage = o.getStageOfInfestation();

				if (p < p_discovery.get(species)[stage - 1]) {
					patch.setMonitored(true);
					Set<Patch> monitored = ms.getWeakRegion(patch, species); 
					ms.setMonitored(monitored, true);
					// additional steps to place in ground control, containment or core.
				}
			}
		}
	}

	@Override
	public Process_Monitor clone() {
		return new Process_Monitor();
	}

	public void setPDiscovery(Map<String, double[]> p_discovery) {
		this.p_discovery = p_discovery;
	}
	
	public void setFrequency(long frequency){
		this.frequency=frequency;
	}
}
