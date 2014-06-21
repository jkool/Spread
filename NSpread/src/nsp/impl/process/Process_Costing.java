package nsp.impl.process;

import java.util.Arrays;
import java.util.Map;

import nsp.Mosaic;
import nsp.Patch;
import nsp.Process;
import nsp.util.ControlType;

/**
 * Performs actions relating to costing conservation actions related to managing infested patches.
 *
 */

public class Process_Costing implements Process, Cloneable {

	private double costTotal = 0;
	private double labourTotal = 0;
	private double containment_cost = 70;
	private double containment_labour = 1;
	private long timeIncrement = 1;
	private long chkFrq = 1;
	private long counter = 0;
	private double[] ground_control_costs = new double[] { 1000, 2000, 4200 };
	private double[] ground_control_labour = new double[] { 14, 24, 56 };

	/**
	 * Processes the entire mosaic
	 */
	
	public void process(Mosaic mosaic) {
		
		counter+=timeIncrement;
		
		if(counter<chkFrq){
			return;
		}
		
		counter = 0;
		
		for (Integer key : mosaic.getPatches().keySet()) {
			process(mosaic.getPatches().get(key));
		}
	}
	
	public double getCost(Mosaic mosaic){
		
		double total = 0;
		
		for (Integer key : mosaic.getPatches().keySet()) {
			total += getCost(mosaic.getPatches().get(key));
		}
		
		return total;
	}
	
	public double getLabour(Mosaic mosaic){
		
		double total = 0;
		
		for (Integer key : mosaic.getPatches().keySet()) {
			total += getLabour(mosaic.getPatches().get(key));
		}
		
		return total;
	}
	
	public double getCost(Patch patch){
		double patchCost = 0;
		
		if (patch.hasNoData()){return 0;}

		for (String species : patch.getOccupants().keySet()) {
			Map<ControlType, Long> controls = patch.getOccupant(species)
					.getControls();
			if (controls.containsKey(ControlType.CONTAINMENT_CORE)) {
				continue;  // cost is effectively 0
			} 
			if (controls.containsKey(ControlType.CONTAINMENT)) {
				patchCost = Math.max(patchCost, containment_cost);
			}
			if (controls.containsKey(ControlType.GROUND_CONTROL)) {

				int stage = patch.getOccupant(species).getStageOfInfestation();
				patchCost=Math.max(patchCost, ground_control_costs[stage-1] );
			}
		}
		
		return patchCost;
	}
	
	public double getLabour(Patch patch){

		double patLabor = 0;
		
		// If a Patch has NoData, ignore it
		
		if (patch.hasNoData()){return 0;}

		for (String species : patch.getOccupants().keySet()) {
			Map<ControlType, Long> controls = patch.getOccupant(species)
					.getControls();
			if (controls.containsKey(ControlType.CONTAINMENT_CORE)) {
				continue;  // cost is effectively 0
			} 
			if (controls.containsKey(ControlType.CONTAINMENT)) {
				patLabor = Math.max(patLabor, containment_labour);
			}
			if (controls.containsKey(ControlType.GROUND_CONTROL)) {

				int stage = patch.getOccupant(species).getStageOfInfestation();
				patLabor= Math.max(patLabor, ground_control_labour[stage-1] );
			}
		}

		return patLabor;		
		
	}
	
	/**
	 * Processes an individual patch
	 * @param patch
	 */

	private void process(Patch patch) {

		double patchCost = 0;
		double patLabor = 0;
		
		// If a Patch has NoData, ignore it
		
		if (patch.hasNoData()){return;}

		for (String species : patch.getOccupants().keySet()) {
			Map<ControlType, Long> controls = patch.getOccupant(species)
					.getControls();
			if (controls.containsKey(ControlType.CONTAINMENT_CORE)) {
				continue;  // cost is effectively 0
			} 
			if (controls.containsKey(ControlType.CONTAINMENT)) {
				patchCost = Math.max(patchCost, containment_cost);
				patLabor = Math.max(patLabor, containment_labour);
			}
			if (controls.containsKey(ControlType.GROUND_CONTROL)) {

				int stage = patch.getOccupant(species).getStageOfInfestation();
				patchCost=Math.max(patchCost, ground_control_costs[stage-1] );
				patLabor= Math.max(patLabor, ground_control_labour[stage-1] );
			}
		}

		costTotal += patchCost;
		labourTotal += patLabor;
	}
	
	/**
	 * Generates a cloned instance of this class
	 */

	public Process_Costing clone() {
		Process_Costing clone = new Process_Costing();
		clone.costTotal = costTotal;
		clone.labourTotal = labourTotal;
		clone.ground_control_costs = Arrays.copyOf(ground_control_costs, ground_control_costs.length);
		clone.ground_control_labour = Arrays.copyOf(ground_control_labour, ground_control_labour.length);
		clone.counter = counter;
		clone.timeIncrement = timeIncrement;
		return clone;
	}

	/**
	 * Sets the cost of containment for an individual patch
	 * @param cost
	 */
	
	public void setContainmentCost(double cost) {
		this.containment_cost = cost;
	}
	
	/**
	 * Sets the labour for containment for an individual patch
	 * @param labour
	 */

	public void setContainmentLabour(double labour) {
		this.containment_labour = labour;
	}
	
	/**
	 * Sets the cost of ground control for an individual patch
	 * @param groundControlCosts
	 */
	
	public void setGroundControlCosts(double[] groundControlCosts){
		this.ground_control_costs=groundControlCosts;
	}
	
	/**
	 * Sets the labour for ground control of an individual patch
	 * @param groundControlLabour
	 */
	
	public void setGroundControlLabour(double[] groundControlLabour){
		this.ground_control_labour=groundControlLabour;
	}
	
	/**
	 * Gets the total management cost
	 * @return
	 */
	
	public double getCostTotal(){
		return costTotal;
	}
	
	/**
	 * Gets the total labour required
	 * @return
	 */
	
	public double getLabourTotal(){
		return labourTotal;
	}
	
	/**
	 * Resets the cost value to zero.
	 */
	
	public void resetCost(){
		this.costTotal = 0;
	}
	
	/**
	 * Resets the labour value to zero.
	 */
	
	public void resetLabour(){
		this.labourTotal=0;
	}
	
	public void setTimeIncrement(long timeIncrement){
		this.timeIncrement = timeIncrement;
	}
	
	public void setCheckFrequency(long checkFrequency){
		this.chkFrq=checkFrequency;
	}
}
