package spread;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import spread.util.ControlType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Represents a single discrete patch of potential habitat. Mainly serves to
 * hold state variables that are then transformed by different classes. The
 * state variables have private access and are modified using getters and
 * setters.
 * 
 */

public class Patch implements Cloneable, Comparable<Patch> {

	private int id = -1;

	private boolean visited = false;
	private boolean monitored = false;
	private boolean wasMonitored = false;
	private Geometry geom;
	private Map<String, Double> habitatSuitabilities = new TreeMap<String, Double>();
	private Map<String, Infestation> infestations = new TreeMap<String, Infestation>();
	private Set<ControlType> controls = new TreeSet<ControlType>();

	private boolean nodata = false;

	public void addControl(ControlType control){
		if(control.equals(ControlType.CONTAINMENT) && id==52487){
			System.out.println("Patch 40");
		}
		controls.add(control);
	}
	
	public void addControl(ControlType control, String species){
		if(isInfestedBy(species)){
			infestations.get(species).addControl(control);
		}
	}
	
	public void clearControls(){
		controls.clear();
		for(String s:infestations.keySet()){
			infestations.get(s).clearControls();
		}
	}
	
	public ControlType getMaxControl(){
		ControlType max = ControlType.NONE;
		for(ControlType control:controls){
			max=ControlType.values()[Math.max(max.ordinal(), control.ordinal())];
		}
		for(String species:infestations.keySet()){
			for(ControlType control:infestations.get(species).getControls().keySet()){
				max=ControlType.values()[Math.max(max.ordinal(), control.ordinal())];
			}
		}
		return max;
	}
	
	public boolean hasControl(ControlType control){
		return controls.contains(control);
	}
	
	public boolean hasControl(ControlType control, String species){
		return infestations.containsKey(species) && infestations.get(species).hasControl(control);
	}
	
	public void removeControl(ControlType control){
		controls.remove(control);
	}
	
	public Set<String> getControlled(ControlType control){
		Set<String> controlled = new TreeSet<String>();
		for(String occupant:infestations.keySet()){
			if(infestations.get(occupant).hasControl(control)){
				controlled.add(occupant);
			}
		}
		return controlled;
	}
	
	/**
	 * Adds an Infestation to the Patch
	 * @param infestation
	 */
	
	public void addInfestation(Infestation infestation) {
		infestations.put(infestation.getName(), infestation);
	}

	/**
	 * Adds a default Infestation to the patch associated
	 * with the given species name.
	 * @param species
	 */
	
	public void addInfestation(String species) {
		Infestation infestation = new Infestation();
		infestation.setSpecies(species);
		infestation.setInfested(true);
		infestations.put(species, infestation);
	}
	
	/**
	 * Clears the Patch of infestation by the given species.
	 * @param species
	 */

	public void clearInfestation(String species) {
		if(infestations.containsKey(species)){
			infestations.get(species).clearInfestation();
		}
	}

	/**
	 * Returns a copy of the current instance of the class.
	 */
	
	@Override
	public Patch clone() {
		Patch patch = new Patch();
		patch.geom = geom;
		patch.nodata = nodata;
		patch.visited = visited;
		patch.monitored = monitored;
		patch.wasMonitored=wasMonitored;
		patch.id = id;
		
		Map<String,Double> hscopy = new TreeMap<String,Double>();
		for (String s:habitatSuitabilities.keySet()){
			hscopy.put(s, habitatSuitabilities.get(s));
		}
		
		patch.habitatSuitabilities=hscopy;
		
		Map<String, Infestation> ocopy = new TreeMap<String, Infestation>();
		for (String o : infestations.keySet()) {
			ocopy.put(o, infestations.get(o).clone());
		}
		
		patch.infestations = ocopy;
		
		Set<ControlType> ccontrols = new TreeSet<ControlType>(controls);
		patch.controls=ccontrols;

		return patch;
	}
	
	/**
	 * Compares Patches on the basis of their id value.
	 */

	@Override
	public int compareTo(Patch p) {
		if (this.getID() < p.getID()) {
			return -1;
		}
		if (this.getID() > p.getID()) {
			return 1;
		}
		return 0;
	}
	
	/**
	 * Tests for equality of Patches (based on id value)
	 * 
	 * @param p - the Patch for comparison.
	 * @return - whether the current Patch and the given Patch are equal (based on ID)
	 */

	public boolean equals(Patch p) {
		return this.getID() == p.getID();
	}
	
	/**
	 * @return a Java map of species names and associated infestation times.
	 */

	public Map<String, Long> getAgesOfInfestation() {
		Map<String, Long> agesOfInfestation = new TreeMap<String, Long>();
		Iterator<String> it = infestations.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			agesOfInfestation
					.put(key, infestations.get(key).getAgeOfInfestation());
		}
		return agesOfInfestation;
	}
	
	/**
	 * @return the length of time occupying species have infested this Patch
	 */

	public long getAgeOfInfestation(String species) {
		if(infestations.containsKey(species)){
			return infestations.get(species).getAgeOfInfestation();
		}
		return 0l;
	}

	/**
	 * @return the set of controls currently associated with this Patch
	 */
	
	public Set<ControlType> getControls(){
		return controls;
	}
	
	/**
	 * @param species - the species of interest.
	 * @return the set of controls currently associated with this Patch and the given Species
	 */
	
	public Set<ControlType> getControls(String species){
		Set<ControlType> speciesControls = new TreeSet<ControlType>();
			if(infestations.containsKey(species)){
				speciesControls.addAll(infestations.get(species).getControls().keySet());
			}
		return speciesControls;
	}
	
	/**
	 * @return the cumulative length of time occupying species have infested this Patch,
	 * e.g. the species may have been eliminated and re-infested.
	 */
	
	public Map<String, Long> getCumulativeAgesOfInfestation() {
		Map<String, Long> cumulativeAgesOfInfestation = new TreeMap<String, Long>();
		Iterator<String> it = infestations.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			cumulativeAgesOfInfestation.put(key, infestations.get(key)
					.getCumulativeAgeOfInfestation());
		}
		return cumulativeAgesOfInfestation;
	}

	/**
	 * @return a list of dispersing propagules produced by the Patch.  This is the equivalent
	 * of seeds produced by the Patch which have yet to be transported.
	 */
	
	public Map<String, Disperser> getDispersers() {
		Map<String, Disperser> dispersers = new TreeMap<String, Disperser>();
		Iterator<String> it = infestations.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			dispersers.put(key, infestations.get(key).getDisperser());
		}
		return dispersers;
	}

	/**
	 * @return the shape of the patch as a geometry.
	 */
	
	public Geometry getGeometry() {
		return geom;
	}

	/**
	 * @return the suitability of the Patch associated with species types.
	 */
	
	public Map<String, Double> getHabitatSuitabilities() {
		return habitatSuitabilities;
	}
	
	/**
	 * @return the suitability of the Patch associated with species types.
	 */
	
	public double getHabitatSuitability(String species) {
		return habitatSuitabilities.get(species);
	}
	
	/**
	 * @return the id of the Patch.
	 */

	public int getID() {
		return id;
	}

	/**
	 * @return the maximum infestation stage of the Patch
	 */
	
	public Map<String, Integer> getMaxInfestations() {
		Map<String, Integer> maxInfestations = new TreeMap<String, Integer>();
		Iterator<String> it = infestations.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			maxInfestations.put(key, infestations.get(key).getMaxInfestation());
		}
		return maxInfestations;
	}

	/**
	 * @param species - the name of the species
	 * @return an infestation of the patch by its species name.
	 */
	
	public Infestation getInfestation(String species) {
		return infestations.get(species);
	}
	
	/**
	 * @return  all Infestations in the Patch.
	 */

	public Map<String, Infestation> getInfestation() {
		return infestations;
	}
	
	/**
	 * @param species - the species of interest.
	 * @return the set of Coordinates representing propagules associated with the given species.
	 */
	
	public List<Coordinate> getPropagules(String species){
		List<Coordinate> propagules = new ArrayList<Coordinate>();
		if(infestations.containsKey(species)){
			propagules.addAll(infestations.get(species).getPropagules());
		}
		return propagules;
	}

	/**
	 * @return the stages of infestation associated with each occupying species type.
	 */
	
	public Map<String, Integer> getStagesOfInfestation() {
		
		Map<String, Integer> stages = new TreeMap<String, Integer>();
		Iterator<String> it = infestations.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			stages.put(key, infestations.get(key).getStageOfInfestation());
		}
		return stages;
	}
	
	/**
	 * @return whether this Patch has 'NoData'.
	 */

	public boolean hasNoData() {
		return nodata;
	}
	
	/**
	 * Increments all infestations by the given time increment
	 * @param increment - the time increment size
	 */
	
	public void incrementInfestationTime(long increment) {
		Iterator<String> it = infestations.keySet().iterator();
		while (it.hasNext()) {
			infestations.get(it.next()).incrementInfestationTime(increment);
		}
	}
	
	/**
	 * @return whether the Patch is controlled in some manner.
	 */
	
	public boolean isControlled(){
		for(Infestation o:infestations.values()){
			if (o.isControlled()){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return whether the Patch is being monitored - which may be potentially
	 * different than whether it is being controlled.
	 */
	
	public boolean isMonitored() {
		return monitored;
	}
	
	/**
	 * @return whether the patch is infested by any species.
	 */

	public boolean isInfested(){
		for(String species:infestations.keySet()){
			if(infestations.get(species).isInfested()){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param species - the species of interest
	 * @return whether the patch is infested by the provided species
	 */
	
	public boolean isInfestedBy(String species) {
		return infestations.containsKey(species) && infestations.get(species).isInfested();
	}
	
	/**
	 * @return whether the Patch was visited by an operation - used to prevent
	 * duplicate processing during chain operations. 
	 */

	public boolean isVisited() {
		return visited;
	}
	
	/**
	 * @return the list of species currently occupying the Patch
	 */
	
	public Set<String> getCurrentOccupants(){
		Set<String> occupants = new TreeSet<String>();
		for(String occupant:infestations.keySet()){
			if(infestations.get(occupant).isInfested()){
				occupants.add(occupant);
			}
		}
		return occupants;
	}
	
	/**
	 * Removes a given Infestation from the Patch.
	 * @param key - the species name to be removed.
	 */

	public void removeInfestation(String key) {
		infestations.remove(key);
	}

	/**
	 * Explicitly sets the length of time a given species has infested the Patch
	 * @param species - the species of interest.
	 * @param ageOfInfestation - the age of infestation to be set.
	 */
	
	public void setAgeOfInfestation(String species, long ageOfInfestation) {
		infestations.get(species).setAgeOfInfestation(ageOfInfestation);
	}

	/**
	 * Sets the Disperser type associated with a given species
	 * @param species - the species of interest.
	 * @param disperser - the Disperser to be set.
	 */
	
	public void setDisperser(String species, Disperser disperser) {
		infestations.get(species).setDisperser(disperser);
	}
	
	/**
	 * Explicitly sets the geometry of the Patch
	 * @param geom - the geometry of the Patch.
	 */

	public void setGeometry(Geometry geom) {
		this.geom = geom;
	}

	/**
	 * Sets the habitat suitability of the Patch associated with a given species.
	 * @param species - the species of interest.
	 * @param habitatSuitability - the associated habitat suitability level.
	 */
	
	public void setHabitatSuitability(String species, double habitatSuitability) {
		habitatSuitabilities.put(species, habitatSuitability);
	}
	
	/**
	 * Sets the habitat suitability of the Patch associated with a given species.
	 * @param habitatSuitabilities - a Map of species names and habitat suitability values.
	 */
	
	public void setHabitatSuitabilities(Map<String,Double> habitatSuitabilities) {
		this.habitatSuitabilities=habitatSuitabilities;
	}
	
	/**
	 * Sets the id of the Patch.
	 * @param id - the ID of the Patch.
	 */

	public void setID(int id) {
		this.id = id;
	}
	
	/**
	 * Sets the infestation state of the Patch by a given species (assumes the Patch is already Infested)
	 * @param species - the species of interest
	 * @param infested - whether the Patch will be considered as infested or not.
	 */

	public void setInfested(String species, boolean infested) {
		infestations.get(species).setInfested(infested);
	}
	
	/**
	 * Sets whether the Patch is considered to be monitored.
	 * @param monitored
	 */

	public void setMonitored(boolean monitored) {
		this.monitored = monitored;
		if(monitored){wasMonitored=true;}
	}
	
	public void clear(){
		infestations = new TreeMap<String, Infestation>();
		controls=new TreeSet<ControlType>();
		visited = false;
		monitored = false;
		wasMonitored = false;
	}

	/**
	 * Sets whether this is a 'NoData' Patch.
	 * @param nodata
	 */
	
	public void setNoData(boolean nodata) {
		this.nodata = nodata;
		if(nodata){
			clear();
		}
	}
	
	/**
	 * Explicitly sets the propagules associated with the patch and given species
	 * (i.e. settled seeds).
	 * 
	 * @param species
	 * @param propagules
	 */

	public void setPropagules(String species, List<Coordinate> propagules) {
		infestations.get(species).setPropagules(propagules);
	}

	/**
	 * Sets the stage of infestation associated with a given species.
	 * @param species
	 * @param stageOfInfestation
	 */
	
	public void setStageOfInfestation(String species, int stageOfInfestation) {
		infestations.get(species).setStageOfInfestation(stageOfInfestation);
	}
	
	/**
	 * Sets whether the patch has been 'visited' to avoid reprocessing the Patch
	 * during chain operations.
	 * @param visited
	 */

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	/**
	 * Provides a String representation of the Patch (patch id)
	 */

	@Override
	public String toString() {
		return Integer.toString(id);
	}
	
	/**
	 * @return whether the Patch was ever under management control at some point.
	 */

	public boolean wasControlled(){
		for(Infestation o:infestations.values()){
			if (o.wasControlled()){
				return true;
			}
		}
		return false;
	}

	/**
	 * @param species - the species of interest.
	 * @return whether the Patch was ever infested by he given Species at some point.
	 */
	
	public boolean wasInfestedBy(String species){
		return infestations.containsKey(species);
	}
	
	/**
	 * @return whether the Patch was ever monitored at some point.
	 */
	
	public boolean wasMonitored(){
		return wasMonitored;
	}
}