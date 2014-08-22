/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package spread.impl.process;

import java.util.Arrays;
import java.util.Collection;
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

	private Map<String, Set<Patch>> visited;
	private Set<Patch> dissolveVisited;
	private Set<String> groundControlIgnore = new TreeSet<String>();
	private Set<String> containmentIgnore = new TreeSet<String>();
	private Set<String> coreControl = new TreeSet<String>();
	private Set<String> co_managed;

	/**
	 * Returns a copy of the current instance of the class.
	 */

	@Override
	public Process_Monitor clone() {
		Process_Monitor clone = new Process_Monitor();
		Map<String, double[]> cp_discovery = new TreeMap<String, double[]>();
		Iterator<String> it = p_discovery.keySet().iterator();
		while (it.hasNext()) {
			String species = it.next();
			cp_discovery.put(
					species,
					Arrays.copyOf(p_discovery.get(species),
							p_discovery.get(species).length));
		}
		clone.setPDiscovery(cp_discovery);
		clone.containmentCutoff = containmentCutoff;
		clone.coreBufferSize = coreBufferSize;
		clone.timeIncrement = timeIncrement;
		clone.counter = counter;
		clone.ms = ms;
		clone.visited = visited;
		Set<String> ci = new TreeSet<String>();
		Set<String> gci = new TreeSet<String>();
		ci.addAll(containmentIgnore);
		gci.addAll(groundControlIgnore);
		clone.containmentIgnore = ci;
		clone.groundControlIgnore = gci;

		return clone;
	}
	
	/**
	 * Processes all patches in the Mosaic
	 */

	@Override
	public void process(Mosaic mosaic) {

		visited = new TreeMap<String, Set<Patch>>();
		co_managed = new TreeSet<String>();

		for (String species : mosaic.getSpeciesList()) {
			visited.put(species, new TreeSet<Patch>());
			co_managed.add(species);
		}

		co_managed.removeAll(containmentIgnore);

		counter += timeIncrement;

		if (counter < chkFrq) {
			return;
		}

		counter = 0;

		this.ms = mosaic;

		for (Integer key : mosaic.getPatches().keySet()) {
			process(mosaic.getPatches().get(key));
		}

		visited.clear();
		
		dissolveContainment();
	}
	
	/**
	 * Performs dissolve operations on Patches undergoing containment
	 */
	
	private void dissolveContainment(){
		
		dissolveVisited = new TreeSet<Patch>();

		for (Integer key : ms.getPatches().keySet()) {
		
			Patch patch = ms.getPatch(key);
			
			if (patch.hasNoData()||dissolveVisited.contains(patch)||!(patch.hasControl(ControlType.CONTAINMENT)||patch.hasControl(ControlType.CONTAINMENT_CORE))) {
				continue;
			}
			
			Set<Patch> containmentZone = ms.getWeakContainment(patch);
		
		// Get the core and assign as Core area and remove Containment label
		// to keep management options exclusive.

		Set<Patch> core = ms.getStrongContainmentCore(containmentZone, coreBufferSize);
		ms.setControl(core, ControlType.CONTAINMENT_CORE);
		ms.removeControl(core, ControlType.CONTAINMENT);

		Iterator<String> it = ms.getSpeciesList().iterator();

		// Apply species-specific core control if needed.

		while (it.hasNext()) {
			String sp = it.next();

			if (coreControl.contains(sp)) {
				setControlled(core, sp,
						ControlType.CONTAINMENT_CORE_CONTROL);
			}
		}

		dissolveVisited.addAll(containmentZone);
		}
	}

	/**
	 * Carries out monitoring actions for a single patch. Includes detection and
	 * assignment to a control type.
	 * 
	 * @param patch
	 *            - The patch to be processed
	 */

	private void process(Patch patch) {

		if (patch.hasNoData()) {
			return;
		}

		Iterator<String> it = patch.getInfestation().keySet().iterator();

		while (it.hasNext()) {

			String species = it.next();

			// If this Patch has been processed already as part of a chain,
			// continue

			if (visited.get(species).contains(patch)) {
				continue;
			}

			Infestation o = patch.getInfestation(species);

			// If the patch is not infested, but is under ground control, then remove ground control
			
			if (!o.isInfested() && o.hasControl(ControlType.GROUND_CONTROL)) {
				o.removeControl(ControlType.GROUND_CONTROL);
			}

			// If the patch is infested, apply a random number to determine
			// whether it was detected

			if (o.isInfested()) {

				// If under control, p detection is 100%

				double p = 0;

				// Otherwise, generate a random number and compare to the p of
				// detection.

				if (!o.isControlled()) {
					p = Uniform.staticNextDouble();
				}

				int stage = o.getStageOfInfestation();

				// If there is something that needs managing

				if (p <= p_discovery.get(species)[stage - 1]) {
					patch.setMonitored(true);

					// Detect the region of infestation, and fill to get the
					// bounded area

					handleRegion(patch, species);

				}
			}
		}
	}

	/**
	 * Performs actions associated with monitoring a region of interconnected
	 * Patches starting with a single seed Patch.
	 * 
	 * @param patch
	 *            - the starting Patch
	 * @param species
	 *            - the species being considered
	 */

	private void handleRegion(Patch patch, String species) {

		// If the patch has been visited for this species, return

		if (visited.get(species).contains(patch)) {
			return;
		}

		// Default is to co-manage containment species. Otherwise
		// handle individually.

		Set<String> sp_group;
		if (co_managed.contains(species)) {
			sp_group = co_managed;
		} else {
			sp_group = new TreeSet<String>();
			sp_group.add(species);
		}

		Set<Patch> comanaged = ms.getWeakRegion(patch, sp_group,
				patch.isInfestedBy(species));

		Set<Patch> filled = ms.fill(comanaged, species);

		// Determine cells that are already controlled through containment

		Set<Patch> controlled = new TreeSet<Patch>();

		// If the species is subject to containment, then add contained list to
		// the list of already managed cells
		
		if (!containmentIgnore.contains(species)) {
			controlled
					.addAll(getControlled(filled, ControlType.CONTAINMENT));
			controlled.addAll(getControlled(filled,
					ControlType.CONTAINMENT_CORE));
		}

		// Remove pre-frozen ground-controlled cells from changeable Patches.
		
		Set<Patch> frozen = ms.getFrozen(comanaged,species);
		controlled.addAll(frozen);
		
		// If the total area is less than the containment cutoff size, put cells
		// into ground control

		if (containmentIgnore.contains(species) || comanaged.size() - controlled.size() <= containmentCutoff) {

			// If the species is exempt from ground control actions, then break.

			if (groundControlIgnore.contains(species)) {
				return;
			}

			comanaged.removeAll(controlled);

			ms.setMonitored(comanaged, true);
			ms.setControl(comanaged, ControlType.GROUND_CONTROL, species);

			// Add Patches to the visited list to avoid re-processing.

			if (!visited.containsKey(species)) {
				visited.put(species, new HashSet<Patch>(comanaged));
			} else {
				visited.get(species).addAll(filled);
			}
		}

		// Otherwise assign all cells in the bounded area to containment

		else {
			
			filled.removeAll(controlled);
			ms.setMonitored(filled, true);

			// Containment overrides ground control (unless we're ignoring
			// containment, which is handled above).

			ms.removeControl(filled, ControlType.GROUND_CONTROL, species);

			// Only assign the new cells to containment, not existing ones.
			
			ms.setControl(filled, ControlType.CONTAINMENT);
		}
	}

	// Getters and setters

	/**
	 * Filters a set of patches to indicate which are controlled for a given
	 * species and control type.
	 * 
	 * @param patches
	 *            - The patches to be filtered
	 * @param species
	 *            - The species to which the controls are applied
	 * @param control
	 *            - The control type being used
	 * @return
	 */

	private void setControlled(Set<Patch> patches, String species,
			ControlType control) {
		for (Patch p : patches) {
			p.addControl(control, species);
		}
	}

	/**
	 * Filters a set of patches to indicate which are controlled for a given
	 * species and control type.
	 * 
	 * @param patches
	 *            - The patches to be filtered
	 * @param species
	 *            - The species to which the controls are applied
	 * @param control
	 *            - The control type being used
	 * @return
	 */

	private Set<Patch> getControlled(Set<Patch> patches, ControlType control) {
		Set<Patch> controlled = new TreeSet<Patch>();
		for (Patch p : patches) {
			if (p.hasControl(control)) {
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

	public void setCheckFrequency(long checkFrequency) {
		this.chkFrq = checkFrequency;
	}

	/**
	 * Sets the number of cells required before an area is assigned to
	 * containment
	 * 
	 * @param containmentCutoff
	 */

	public void setContainmentCutoff(double containmentCutoff) {
		this.containmentCutoff = containmentCutoff;
	}

	/**
	 * Sets the number of cells away from the edge that a Patch needs to be
	 * before it is considered to be a containment core area.
	 * 
	 * @param coreBufferSize
	 */

	public void setCoreBufferSize(double coreBufferSize) {
		this.coreBufferSize = coreBufferSize;
	}

	/**
	 * Sets the probability (associated with growth stage) of discovering
	 * whether a patch is infested.
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

	public void setTimeIncrement(long timeIncrement) {
		this.timeIncrement = timeIncrement;
	}

	/**
	 * Resets the process
	 */

	@Override
	public void reset() {
	}

	public void addToContainmentIgnore(Collection<String> species) {
		containmentIgnore.addAll(species);
	}

	public void addToContainmentIgnore(String species) {
		containmentIgnore.add(species);
	}

	public void setContainmentIgnore(Set<String> containmentIgnore) {
		this.containmentIgnore = containmentIgnore;
	}

	public void clearContainmentIgnore() {
		containmentIgnore.clear();
	}

	public void removeFromContainmentIgnore(String species) {
		containmentIgnore.remove(species);
	}

	public Set<String> getContainmentIgnore() {
		return containmentIgnore;
	}

	public void addToGroundControlIgnore(Collection<String> species) {
		groundControlIgnore.addAll(species);
	}

	public void addToGroundControlIgnore(String species) {
		groundControlIgnore.add(species);
	}

	public void setGroundControlIgnore(Set<String> groundControlIgnore) {
		this.groundControlIgnore = groundControlIgnore;
	}

	public void clearGroundControlIgnore() {
		groundControlIgnore.clear();
	}

	public void removeFromGroundControlIgnore(String species) {
		groundControlIgnore.remove(species);
	}

	public Set<String> getGroundControlIgnore() {
		return groundControlIgnore;
	}

	public void addToCoreControl(Collection<String> species) {
		coreControl.addAll(species);
	}

	public void addToCoreControl(String species) {
		coreControl.add(species);
	}

	public void setCoreControl(Set<String> coreControl) {
		this.coreControl = coreControl;
	}

	public void clearCoreControlIgnore() {
		coreControl.clear();
	}

	public void removeFromCoreControl(String species) {
		coreControl.remove(species);
	}

	public Set<String> getCoreControl() {
		return coreControl;
	}
}