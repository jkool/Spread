package spread.impl.process;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import spread.Mosaic;
import spread.Patch;
import spread.Process;

import spread.util.ControlType;

/**
 * Performs actions relating to costing conservation actions related to managing
 * infested patches.
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
	private Map<String, double[]> ground_control_costs = new TreeMap<String, double[]>();
	private Map<String, double[]> ground_control_labour = new TreeMap<String, double[]>();

	/**
	 * Generates a cloned instance of this class
	 */

	@Override
	public Process_Costing clone() {
		Process_Costing clone = new Process_Costing();
		clone.costTotal = costTotal;
		clone.labourTotal = labourTotal;

		Map<String, double[]> ground_control_costsc = new TreeMap<String, double[]>();
		Map<String, double[]> ground_control_labourc = new TreeMap<String, double[]>();

		for (String key : ground_control_costs.keySet()) {
			ground_control_costsc.put(key, Arrays.copyOf(
					ground_control_costs.get(key),
					ground_control_costs.get(key).length));
		}

		for (String key : ground_control_costs.keySet()) {
			ground_control_labourc.put(key, Arrays.copyOf(
					ground_control_labour.get(key),
					ground_control_labour.get(key).length));
		}

		clone.setGroundControlCosts(ground_control_costsc);
		clone.setGroundControlLabour(ground_control_labourc);

		clone.counter = counter;
		clone.timeIncrement = timeIncrement;
		return clone;
	}
	
	/**
	 * Retrieves the total instantaneous cost of management across the mosaic
	 * 
	 * @param mosaic
	 * @return
	 */

	public double getCost(Mosaic mosaic) {

		double total = 0;

		for (Integer key : mosaic.getPatches().keySet()) {
			total += getCost(mosaic.getPatches().get(key));
		}

		return total;
	}

	/**
	 * Retrieves the total instantaneous cost of management for a single Patch
	 * 
	 * @param patch
	 * @return
	 */

	public double getCost(Patch patch) {
		double patchCost = 0;

		if (patch.hasNoData()) {
			return 0;
		}

		for (String species : patch.getInfestation().keySet()) {
			Map<ControlType, Long> controls = patch.getInfestation(species)
					.getControls();
			if (controls.containsKey(ControlType.CONTAINMENT_CORE)) {
				continue; // cost is effectively 0
			}
			if (controls.containsKey(ControlType.CONTAINMENT)) {
				patchCost = Math.max(patchCost, containment_cost);
			}
			if (controls.containsKey(ControlType.GROUND_CONTROL)) {

				int stage = patch.getInfestation(species).getStageOfInfestation();
				patchCost = Math.max(patchCost,
						ground_control_costs.get(species)[stage - 1]);
			}
		}

		return patchCost;
	}
	
	/**
	 * Gets the total management cost
	 * 
	 * @return
	 */

	public double getCostTotal() {
		return costTotal;
	}
	
	/**
	 * Retrieves the total instantaneous labour cost of management across the mosaic
	 * 
	 * @param mosaic
	 * @return
	 */
	
	public double getLabour(Mosaic mosaic) {

		double total = 0;

		for (Integer key : mosaic.getPatches().keySet()) {
			total += getLabour(mosaic.getPatches().get(key));
		}

		return total;
	}

	/**
	 * Retrieves the total instantaneous labour cost of management for a single Patch
	 * 
	 * @param patch
	 * @return
	 */

	public double getLabour(Patch patch) {

		double patLabor = 0;

		// If a Patch has NoData, ignore it

		if (patch.hasNoData()) {
			return 0;
		}

		for (String species : patch.getInfestation().keySet()) {
			Map<ControlType, Long> controls = patch.getInfestation(species)
					.getControls();
			if (controls.containsKey(ControlType.CONTAINMENT_CORE)) {
				continue; // cost is effectively 0
			}
			if (controls.containsKey(ControlType.CONTAINMENT)) {
				patLabor = Math.max(patLabor, containment_labour);
			}
			if (controls.containsKey(ControlType.GROUND_CONTROL)) {

				int stage = patch.getInfestation(species).getStageOfInfestation();
				patLabor = Math.max(patLabor,
						ground_control_labour.get(species)[stage - 1]);
			}
		}

		return patLabor;

	}

	/**
	 * Gets the total labour required
	 * 
	 * @return
	 */

	public double getLabourTotal() {
		return labourTotal;
	}

	/**
	 * Processes the entire mosaic
	 */

	@Override
	public void process(Mosaic mosaic) {

		counter += timeIncrement;

		if (counter < chkFrq) {
			return;
		}

		counter = 0;

		for (Integer key : mosaic.getPatches().keySet()) {
			process(mosaic.getPatches().get(key));
		}
	}

	/**
	 * Processes an individual patch
	 * 
	 * @param patch
	 */

	private void process(Patch patch) {

		// If a Patch has NoData, ignore it

		if (patch.hasNoData()) {
			return;
		}

		costTotal += getCost(patch);
		labourTotal += getLabour(patch);

	}
	
	/**
	 * Resets the process
	 */
	
	public void reset(){
		this.costTotal = 0;
		this.labourTotal = 0;
	}

	/**
	 * Resets the cost value to zero.
	 */

	public void resetCost() {
		this.costTotal = 0;
	}

	/**
	 * Resets the labour value to zero.
	 */

	public void resetLabour() {
		this.labourTotal = 0;
	}

	/**
	 * Sets the frequency with which costing actions occur.
	 * 
	 * @param checkFrequency
	 */

	public void setCheckFrequency(long checkFrequency) {
		this.chkFrq = checkFrequency;
	}

	/**
	 * Sets the cost of containment for an individual patch
	 * 
	 * @param cost
	 */

	public void setContainmentCost(double cost) {
		this.containment_cost = cost;
	}

	/**
	 * Sets the labour for containment for an individual patch
	 * 
	 * @param labour
	 */

	public void setContainmentLabour(double labour) {
		this.containment_labour = labour;
	}

	/**
	 * Sets the cost of ground control for an individual patch
	 * 
	 * @param groundControlCosts
	 */

	public void setGroundControlCosts(Map<String, double[]> groundControlCosts) {
		this.ground_control_costs = groundControlCosts;
	}

	/**
	 * Sets the labour for ground control of an individual patch
	 * 
	 * @param groundControlLabour
	 */

	public void setGroundControlLabour(Map<String, double[]> groundControlLabour) {
		this.ground_control_labour = groundControlLabour;
	}
	
	/**
	 * Sets the associated time interval for each time step made by this class.
	 * 
	 * @param timeIncrement
	 */
	
	public void setTimeIncrement(long timeIncrement) {
		this.timeIncrement = timeIncrement;
	}
}
