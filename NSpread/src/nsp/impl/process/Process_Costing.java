package nsp.impl.process;

import java.util.Arrays;
import java.util.Map;

import nsp.Mosaic;
import nsp.Patch;
import nsp.Process;
import nsp.util.ManagementTypes;

public class Process_Costing implements Process, Cloneable {

	private double cost = 0;
	private double labour = 0;
	private double containment_cost = 70;
	private double containment_labour = 1;
	private double[] ground_control_costs = new double[] { 1000, 2000, 4200 };
	private double[] ground_control_labour = new double[] { 14, 24, 56 };

	public void process(Mosaic mosaic) {
		for (Integer key : mosaic.getPatches().keySet()) {
			process(mosaic.getPatches().get(key));
		}
	}

	private void process(Patch patch) {

		double patchCost = 0;
		double patLabor = 0;
		
		if (patch.hasNoData()){return;}

		for (String species : patch.getOccupants().keySet()) {
			Map<String, Long> controls = patch.getOccupant(species)
					.getControls();
			if (controls.containsKey(ManagementTypes.CONTAINMENT_CORE
					.displayName())) {
				continue;  // cost is effectively 0
			} 
			if (controls.containsKey(ManagementTypes.CONTAINMENT.displayName())) {
				patchCost = Math.max(patchCost, containment_cost);
				patLabor = Math.max(patLabor, containment_labour);
			}
			if (controls.containsKey(ManagementTypes.GROUND_CONTROL
					.displayName())) {

				int stage = controls.get(
						ManagementTypes.GROUND_CONTROL.displayName())
						.intValue();

				patchCost=Math.max(patchCost, ground_control_costs[stage] );
				patLabor= Math.max(patLabor, ground_control_labour[stage] );
			}
		}

		cost += patchCost;
		labour += patLabor;
	}

	public Process_Costing clone() {
		Process_Costing clone = new Process_Costing();
		clone.cost = cost;
		clone.labour = labour;
		clone.ground_control_costs = Arrays.copyOf(ground_control_costs, ground_control_costs.length);
		clone.ground_control_labour = Arrays.copyOf(ground_control_labour, ground_control_labour.length);
		return clone;
	}

	public void setContainmentCost(double cost) {
		this.containment_cost = cost;
	}

	public void setContainmentLabour(double labour) {
		this.labour = labour;
	}
	
	public void setGroundControlCosts(double[] groundControlCosts){
		this.ground_control_costs=groundControlCosts;
	}
	
	public void setGroundControlLabour(double[] groundControlLabour){
		this.ground_control_labour=groundControlLabour;
	}
	
	public double getCost(){
		return cost;
	}
	
	public double getLabour(){
		return labour;
	}
	
	public void resetCost(){
		this.cost = 0;
	}
	public void resetLabour(){
		this.labour=0;
	}
}
