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
	 * Adds an Occupant to the Patch
	 * @param occupant
	 */
	
	public void addInfestation(Infestation infestation) {
		infestations.put(infestation.getName(), infestation);
	}

	/**
	 * Adds a default Occupant to the patch associated
	 * with the given species name.
	 * @param species
	 */
	
	public void addOccupant(String species) {
		Infestation infestation = new Infestation();
		infestation.setSpecies(species);
		infestations.put(species, infestation);
	}
	
	/**
	 * Clears the Patch of infestation by the given species.
	 * @param species
	 */

	public void clearInfestation(String species) {
		infestations.get(species).clearInfestation();
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
		Map<String,Double> hscopy = new TreeMap<String,Double>();
		for (String s:habitatSuitabilities.keySet()){
			hscopy.put(s, habitatSuitabilities.get(s));
		}
		patch.habitatSuitabilities=hscopy;
		patch.id = id;
		Map<String, Infestation> ocopy = new TreeMap<String, Infestation>();
		for (String o : infestations.keySet()) {
			ocopy.put(o, infestations.get(o).clone());
		}
		patch.infestations = ocopy;

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
	 * @param p
	 * @return
	 */

	public boolean equals(Patch p) {
		return this.getID() == p.getID();
	}
	
	/**
	 * Returns the length of time occupying species have infested this Patch
	 * @return
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
	 * Returns the length of time occupying species have infested this Patch
	 * @return
	 */

	public long getAgeOfInfestation(String species) {
		if(infestations.containsKey(species)){
			return infestations.get(species).getAgeOfInfestation();
		}
		return 0l;
	}

	public Set<ControlType> getControls(){
		return controls;
	}
	
	public Set<ControlType> getControls(String species){
		Set<ControlType> speciesControls = new TreeSet<ControlType>();
			if(infestations.containsKey(species)){
				speciesControls.addAll(infestations.get(species).getControls().keySet());
			}
		return speciesControls;
	}
	
	/**
	 * Returns the cumulative length of time occupying species have infested this Patch,
	 * e.g. the species may have been eliminated and re-infested.
	 * @return
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
	 * Returns a list of dispersing propagules produced by the Patch.  This is the equivalent
	 * of seeds produced by the Patch which have yet to be transported.
	 * @return
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
	 * Returns the shape of the patch as a geometry.
	 * @return
	 */
	
	public Geometry getGeometry() {
		return geom;
	}

	/**
	 * Retrieves the suitability of the Patch associated with species types.
	 * @return
	 */
	
	public Map<String, Double> getHabitatSuitabilities() {
		return habitatSuitabilities;
	}
	
	/**
	 * Retrieves the suitability of the Patch associated with species types.
	 * @return
	 */
	
	public double getHabitatSuitability(String species) {
		return habitatSuitabilities.get(species);
	}
	
	/**
	 * Retrieves the id of the Patch.
	 * @return
	 */

	public int getID() {
		return id;
	}

	/**
	 * Retrieves the maximum infestation stage of the Patch
	 * @return
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
	 * Retrieves an Occupant by species name.
	 * @param species - the name of the species
	 * @return
	 */
	
	public Infestation getInfestation(String species) {
		return infestations.get(species);
	}
	
	/**
	 * Retrieves all Occupants in the Patch.
	 * @return
	 */

	public Map<String, Infestation> getInfestation() {
		return infestations;
	}
	
	public List<Coordinate> getPropagules(String species){
		List<Coordinate> propagules = new ArrayList<Coordinate>();
		if(infestations.containsKey(species)){
			propagules.addAll(infestations.get(species).getPropagules());
		}
		return propagules;
	}

	/**
	 * Returns the stages of infestation associated with each occupying species type.
	 * @return
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
	 * Indicates whether this Patch has 'NoData'.
	 * @return
	 */

	public boolean hasNoData() {
		return nodata;
	}
	
	/**
	 * Increments all infestations by the given time increment
	 * @param increment
	 */
	
	public void incrementInfestationTime(long increment) {
		Iterator<String> it = infestations.keySet().iterator();
		while (it.hasNext()) {
			infestations.get(it.next()).incrementInfestationTime(increment);
		}
	}
	
	/**
	 * Indicates whether the Patch is controlled in some manner.
	 * @return
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
	 * Indicates whether the Patch is being monitored - which may be potentially
	 * different than whether it is being controlled.
	 * @return
	 */
	
	public boolean isMonitored() {
		return monitored;
	}
	
	/**
	 * Indicates whether the patch is infested by any species
	 * @param species
	 * @return
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
	 * Indicates whether the patch is infested by the provided species
	 * @param species
	 * @return
	 */
	
	public boolean isInfestedBy(String species) {
		return infestations.containsKey(species) && infestations.get(species).isInfested();
	}
	
	/**
	 * Indicates whether the Patch was visited by an operation - used to prevent
	 * duplicate processing during chain operations. 
	 * @return
	 */

	public boolean isVisited() {
		return visited;
	}
	
	/**
	 * Retrieves a list of species currently occupying the Patch
	 * @return
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
	 * Removes a given Occupant from the Patch.
	 * @param key
	 */

	public void removeOccupant(String key) {
		infestations.remove(key);
	}

	/**
	 * Explicitly sets the length of time a given species has infested the Patch
	 * @param species
	 * @param ageOfInfestation
	 */
	
	public void setAgeOfInfestation(String species, long ageOfInfestation) {
		infestations.get(species).setAgeOfInfestation(ageOfInfestation);
	}

	/**
	 * Sets the Disperser type associated with a given species
	 * @param species
	 * @param disperser
	 */
	
	public void setDisperser(String species, Disperser disperser) {
		infestations.get(species).setDisperser(disperser);
	}
	
	/**
	 * Explicitly sets the geometry of the Patch
	 * @param geom
	 */

	public void setGeometry(Geometry geom) {
		this.geom = geom;
	}

	/**
	 * Sets the habitat suitability of the Patch associated with a given species.
	 * @param species
	 * @param habitatSuitability
	 */
	
	public void setHabitatSuitability(String species, double habitatSuitability) {
		habitatSuitabilities.put(species, habitatSuitability);
	}
	
	/**
	 * Sets the habitat suitability of the Patch associated with a given species.
	 * @param species
	 * @param habitatSuitability
	 */
	
	public void setHabitatSuitabilities(Map<String,Double> habitatSuitabilities) {
		this.habitatSuitabilities=habitatSuitabilities;
	}
	
	/**
	 * Sets the id of the Patch.
	 * @param id
	 */

	public void setID(int id) {
		this.id = id;
	}
	
	/**
	 * Sets the infestation state of the Patch by a given species (assumes the Patch is already Infested)
	 * @param species
	 * @param infested
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
	 * Returns whether the Patch was ever under management control at some point.
	 * @return
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
	 * Returns whether the Patch was ever infested by he given Species at some point.
	 * @param species
	 * @return
	 */
	
	public boolean wasInfestedBy(String species){
		return infestations.containsKey(species);
	}
	
	/**
	 * Indicates whether the Patch was ever monitored at some point.
	 * @return
	 */
	
	public boolean wasMonitored(){
		return wasMonitored;
	}
}
