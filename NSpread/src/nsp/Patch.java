package nsp;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import nsp.util.ControlType;

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

	public void addOccupant(Occupant occupant) {
		occupants.put(occupant.getName(), occupant);
	}

	public void addOccupant(String key) {
		Occupant occupant = new Occupant();
		occupant.setName(key);
		occupants.put(key, occupant);
	}

	// Getters and setters ////////////////////////////////////////////////////

	public void clearInfestation(String key) {
		occupants.get(key).clearInfestation();
	}

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

	public int compareTo(Patch p) {
		if (this.getID() < p.getID()) {
			return -1;
		}
		if (this.getID() > p.getID()) {
			return 1;
		}
		return 0;
	}

	public boolean equals(Patch p) {
		return this.getID() == p.getID();
	}

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

	public Map<String, Disperser> getDispersers() {
		Map<String, Disperser> dispersers = new TreeMap<String, Disperser>();
		Iterator<String> it = occupants.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			dispersers.put(key, occupants.get(key).getDisperser());
		}
		return dispersers;
	}

	public Geometry getGeometry() {
		return geom;
	}

	public Map<String, Double> getHabitatSuitabilities() {
		Map<String, Double> suitabilities = new TreeMap<String, Double>();
		Iterator<String> it = occupants.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			suitabilities.put(key, occupants.get(key).getHabitatSuitability());
		}
		return suitabilities;
	}

	public int getID() {
		return id;
	}

	public Map<String, Integer> getMaxInfestation() {
		Map<String, Integer> maxInfestations = new TreeMap<String, Integer>();
		Iterator<String> it = occupants.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			maxInfestations.put(key, occupants.get(key).getMaxInfestation());
		}
		return maxInfestations;
	}

	public Occupant getOccupant(String key) {
		return occupants.get(key);
	}

	public Map<String, Occupant> getOccupants() {
		return occupants;
	}

	public Map<String, List<Coordinate>> getPropagules() {
		Map<String, List<Coordinate>> propaguleCollection = new TreeMap<String, List<Coordinate>>();
		Iterator<String> it = occupants.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			propaguleCollection.put(key, occupants.get(key).getPropagules());
		}
		return propaguleCollection;
	}

	public Map<String, Integer> getStagesOfInfestation() {
		Map<String, Integer> stages = new TreeMap<String, Integer>();
		Iterator<String> it = occupants.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			stages.put(key, occupants.get(key).getStageOfInfestation());
		}
		return stages;
	}

	public boolean hasNoData() {
		return nodata;
	}

	public boolean hasOccupant(String species) {
		return occupants.containsKey(species);
	}
	
	public boolean isInfestedBy(String species){
		Occupant o = occupants.get(species);
		return o.isInfested();
	}
	
	public boolean isControlled(){
		for(Occupant o:occupants.values()){
			if (o.isControlled()){
				return true;
			}
		}
		return false;
	}
	
	public boolean wasControlled(){
		for(Occupant o:occupants.values()){
			if (o.wasControlled()){
				return true;
			}
		}
		return false;
	}
	
	public boolean wasInfestedBy(String species){
		Occupant o = occupants.get(species);
		return o.wasInfested();
	}

	public void incrementControlTime(ControlType control, long increment) {
		Iterator<String> it = occupants.keySet().iterator();
		while (it.hasNext()) {
			Occupant occ = occupants.get(it.next());
			if(occ.hasControl(control)){
				occ.getControls().put(control,
						occ.getControls().get(control) + increment);
			}
		}
	}

	public void incrementInfestationTime(long increment) {
		Iterator<String> it = occupants.keySet().iterator();
		while (it.hasNext()) {
			occupants.get(it.next()).incrementInfestationTime(increment);
		}
	}

	public boolean isMonitored() {
		return monitored;
	}

	public boolean isVisited() {
		return visited;
	}

	public void removeOccupant(String key) {
		occupants.remove(key);
	}

	public void setAgeOfInfestation(String key, long ageOfInfestation) {
		occupants.get(key).setAgeOfInfestation(ageOfInfestation);
	}

	public void setDisperser(String key, Disperser disperser) {
		occupants.get(key).setDisperser(disperser);
	}

	public void setGeometry(Geometry geom) {
		this.geom = geom;
	}

	public void setHabitatSuitability(String key, double habitatSuitability) {
		occupants.get(key).setHabitatSuitability(habitatSuitability);
	}

	public void setID(int id) {
		this.id = id;
	}

	public void setInfested(String key, boolean infested) {
		occupants.get(key).setInfested(infested);
	}

	public void setMonitored(boolean monitored) {
		this.monitored = monitored;
		if(monitored){wasMonitored=true;}
	}

	public void setNoData(boolean nodata) {
		this.nodata = nodata;
	}

	public void setPropagules(String key, List<Coordinate> propagules) {
		occupants.get(key).setPropagules(propagules);
	}

	public void setStageOfInfestation(String key, int stageOfInfestation) {
		occupants.get(key).setStageOfInfestation(stageOfInfestation);
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public Map<String, Boolean> surveyInfestation() {
		Map<String, Boolean> infested = new TreeMap<String, Boolean>();
		Iterator<String> it = occupants.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			infested.put(key, occupants.get(key).isInfested());
		}
		return infested;
	}

	public Map<String, Boolean> surveyInfested() {
		Map<String, Boolean> wasInfested = new TreeMap<String, Boolean>();
		Iterator<String> it = occupants.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			wasInfested.put(key, occupants.get(key).wasInfested());
		}
		return wasInfested;
	}

	public String toString() {
		return Integer.toString(id);
	}
	
	public boolean wasMonitored(){
		return wasMonitored;
	}
}
