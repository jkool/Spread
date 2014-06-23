package spread;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
	private Map<String, Occupant> occupants = new TreeMap<String, Occupant>();

	private boolean nodata = false;

	/**
	 * Adds an Occupant to the Patch
	 * @param occupant
	 */
	
	public void addOccupant(Occupant occupant) {
		occupants.put(occupant.getName(), occupant);
	}

	/**
	 * Adds a default Occupant to the patch associated
	 * with the given species name.
	 * @param species
	 */
	
	public void addOccupant(String species) {
		Occupant occupant = new Occupant();
		occupant.setName(species);
		occupants.put(species, occupant);
	}
	
	/**
	 * Clears the Patch of infestation by the given species.
	 * @param species
	 */

	public void clearInfestation(String species) {
		occupants.get(species).clearInfestation();
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
		patch.id = id;
		Map<String, Occupant> ocopy = new TreeMap<String, Occupant>();
		for (String o : occupants.keySet()) {
			ocopy.put(o, occupants.get(o).clone());
		}
		patch.occupants = ocopy;

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
		Iterator<String> it = occupants.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			agesOfInfestation
					.put(key, occupants.get(key).getAgeOfInfestation());
		}
		return agesOfInfestation;
	}

	/**
	 * Returns the cumulative length of time occupying species have infested this Patch,
	 * e.g. the species may have been eliminated and re-infested.
	 * @return
	 */
	
	public Map<String, Long> getCumulativeAgesOfInfestation() {
		Map<String, Long> cumulativeAgesOfInfestation = new TreeMap<String, Long>();
		Iterator<String> it = occupants.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			cumulativeAgesOfInfestation.put(key, occupants.get(key)
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
		Iterator<String> it = occupants.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			dispersers.put(key, occupants.get(key).getDisperser());
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
		Map<String, Double> suitabilities = new TreeMap<String, Double>();
		Iterator<String> it = occupants.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			suitabilities.put(key, occupants.get(key).getHabitatSuitability());
		}
		return suitabilities;
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
	
	public Map<String, Integer> getMaxInfestation() {
		Map<String, Integer> maxInfestations = new TreeMap<String, Integer>();
		Iterator<String> it = occupants.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			maxInfestations.put(key, occupants.get(key).getMaxInfestation());
		}
		return maxInfestations;
	}

	/**
	 * Retrieves an Occupant by species name.
	 * @param species - the name of the species
	 * @return
	 */
	
	public Occupant getOccupant(String species) {
		return occupants.get(species);
	}
	
	/**
	 * Retrieves all Occupants in the Patch.
	 * @return
	 */

	public Map<String, Occupant> getOccupants() {
		return occupants;
	}

	/**
	 * Gets a list of propagules associated with particular species.  Note, these are
	 * the equivalent of seeds that have reached their destination
	 * @return
	 */
	public Map<String, List<Coordinate>> getPropagules() {
		Map<String, List<Coordinate>> propaguleCollection = new TreeMap<String, List<Coordinate>>();
		Iterator<String> it = occupants.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			propaguleCollection.put(key, occupants.get(key).getPropagules());
		}
		return propaguleCollection;
	}

	/**
	 * Returns the stages of infestation associated with each occupying species type.
	 * @return
	 */
	
	public Map<String, Integer> getStagesOfInfestation() {
		
		Map<String, Integer> stages = new TreeMap<String, Integer>();
		Iterator<String> it = occupants.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			stages.put(key, occupants.get(key).getStageOfInfestation());
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
	 * Indicates whether the patch has the given species as an Occupant
	 * @param species
	 * @return
	 */
	
	public boolean hasOccupant(String species) {
		return occupants.containsKey(species);
	}
	
	/**
	 * Increments all infestations by the given time increment
	 * @param increment
	 */
	
	public void incrementInfestationTime(long increment) {
		Iterator<String> it = occupants.keySet().iterator();
		while (it.hasNext()) {
			occupants.get(it.next()).incrementInfestationTime(increment);
		}
	}
	
	/**
	 * Indicates whether the Patch is controlled in some manner.
	 * @return
	 */
	
	public boolean isControlled(){
		for(Occupant o:occupants.values()){
			if (o.isControlled()){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Indicates whether the Patch is infested by a given species type.
	 * @param species
	 * @return
	 */
	
	public boolean isInfestedBy(String species){
		Occupant o = occupants.get(species);
		return o.isInfested();
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
	 * Indicates whether the Patch was visited by an operation - used to prevent
	 * duplicate processing during chain operations. 
	 * @return
	 */

	public boolean isVisited() {
		return visited;
	}
	
	/**
	 * Removes a given Occupant from the Patch.
	 * @param key
	 */

	public void removeOccupant(String key) {
		occupants.remove(key);
	}

	/**
	 * Explicitly sets the length of time a given species has infested the Patch
	 * @param species
	 * @param ageOfInfestation
	 */
	
	public void setAgeOfInfestation(String species, long ageOfInfestation) {
		occupants.get(species).setAgeOfInfestation(ageOfInfestation);
	}

	/**
	 * Sets the Disperser type associated with a given species
	 * @param species
	 * @param disperser
	 */
	
	public void setDisperser(String species, Disperser disperser) {
		occupants.get(species).setDisperser(disperser);
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
		occupants.get(species).setHabitatSuitability(habitatSuitability);
	}
	
	/**
	 * Sets the id of the Patch.
	 * @param id
	 */

	public void setID(int id) {
		this.id = id;
	}
	
	/**
	 * Sets the infestation state of the Patch by a given species (assumes the Patch is already Occupied)
	 * @param species
	 * @param infested
	 */

	public void setInfested(String species, boolean infested) {
		occupants.get(species).setInfested(infested);
	}
	
	/**
	 * Sets whether the Patch is considered to be monitored.
	 * @param monitored
	 */

	public void setMonitored(boolean monitored) {
		this.monitored = monitored;
		if(monitored){wasMonitored=true;}
	}

	/**
	 * Sets whether this is a 'NoData' Patch.
	 * @param nodata
	 */
	
	public void setNoData(boolean nodata) {
		this.nodata = nodata;
	}
	
	/**
	 * Explicitly sets the propagules associated with the patch and given species
	 * (i.e. settled seeds).
	 * 
	 * @param species
	 * @param propagules
	 */

	public void setPropagules(String species, List<Coordinate> propagules) {
		occupants.get(species).setPropagules(propagules);
	}

	/**
	 * Sets the stage of infestation associated with a given species.
	 * @param species
	 * @param stageOfInfestation
	 */
	
	public void setStageOfInfestation(String species, int stageOfInfestation) {
		occupants.get(species).setStageOfInfestation(stageOfInfestation);
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
		for(Occupant o:occupants.values()){
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
		Occupant o = occupants.get(species);
		return o.wasInfested();
	}
	
	/**
	 * Indicates whether the Patch was ever monitored at some point.
	 * @return
	 */
	
	public boolean wasMonitored(){
		return wasMonitored;
	}
}
