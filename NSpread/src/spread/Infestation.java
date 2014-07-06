package spread;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import spread.util.ControlType;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Represents the Infestation of a Patch object by a single species.  The class 
 * represents the potential for a species to inhabit the patch (i.e. it is a container).  
 * The current implementation is such that if an Occupant is in place, then it is also 
 * filled, but other implementations may operate such that it might not necessarily be.
 */

public class Infestation implements Cloneable {

	private String species = "";
	private boolean infested = false;
	private boolean wasInfested = false;
	private boolean visited = false;
	private long ageOfInfestation = 0;
	private long cumulativeAgeOfInfestation = 0;
	private int stageOfInfestation = 0;
	private int maxInfestation = -99;
	private Disperser disperser;
	private List<Coordinate> propagules = new ArrayList<Coordinate>();
	private Map<ControlType, Long> controls = new TreeMap<ControlType, Long>();
	private ControlType maxControl = ControlType.NONE;
	private boolean wasControlled = false;
	private boolean NODATA = false;

	public Infestation() {
	}

	public Infestation(String name) {
		this.species = name;
	}

	/**
	 * Applies a management control to the Occupant.
	 * @param control
	 */
	
	public void addControl(ControlType control) {
		if (!controls.containsKey(control)) {
			controls.put(control, 0l);
			if(control!=ControlType.NONE){
				wasControlled=true;
			}
			if(control.ordinal()>maxControl.ordinal()){
				maxControl=control;
			}
		}
	}
	
	public void clearControls(){
		controls.clear();
	}
	
	/**
	 * Clears the Occupant
	 */

	public void clearInfestation() {
		this.infested = false;
		this.stageOfInfestation = 0;
		this.maxInfestation = 0;
	}
	
	/**
	 * Removes propagules associated with the Occupant.
	 */

	public void clearPropagules() {
		propagules = new ArrayList<Coordinate>();
	}

	/**
	 * Returns a copy of the instance of the class.
	 */
	
	@Override
	public Infestation clone() {
		Infestation occ = new Infestation();
		occ.ageOfInfestation = ageOfInfestation;
		if(disperser!=null){
			occ.disperser = disperser.clone();
		}
		occ.infested = infested;
		occ.stageOfInfestation = stageOfInfestation;
		occ.maxInfestation = maxInfestation;
		occ.maxControl=maxControl;
		occ.wasControlled=wasControlled;
		occ.species = species;
		List<Coordinate> propagules_c = new ArrayList<Coordinate>();
		for (Coordinate c : propagules) {
			propagules_c.add((Coordinate) c.clone());
		}
		for (ControlType s : controls.keySet()) {
			occ.controls.put(s, controls.get(s).longValue());
		}
		occ.propagules = propagules_c;
		return occ;
	}

	/**
	 * Performs dispersal operations associated with the Occupant.
	 */
	
	public void disperse() {
		try {
			if(disperser==null){
				System.out.println("Infestation 113");
			}
			propagules = disperser.disperse();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the length of time the Occupant has been in place.
	 * @return
	 */

	public long getAgeOfInfestation() {
		return ageOfInfestation;
	}
	
	/**
	 * Returns the Map (as in Java Map) of ControlTypes associated with the occupant,
	 * with values indicating the length of time the control has been in place.
	 * @return
	 */

	public Map<ControlType, Long> getControls() {
		return controls;
	}
	
	/**
	 * Returns the length of time a given control has been in place.
	 * @param control
	 * @return
	 */

	public long getControlTime(ControlType control) {
		return controls.get(control);
	}
	
	/**
	 * Returns the cumulative time that the Occupant has been in place -
	 * e.g. if cleared and then reinfested.
	 * @return
	 */

	public long getCumulativeAgeOfInfestation() {
		return cumulativeAgeOfInfestation;
	}
	
	/**
	 * Retrieves the Disperser object associated with the Occupant.
	 * @return
	 */

	public Disperser getDisperser() {
		return disperser;
	}
	
	/**
	 * Return the maximum control level applied to this Occupant.
	 * @return
	 */

	public int getMaxControl(){
		return maxControl.ordinal();
	}

	/**
	 * Return the maximum infestation level associated with this Occupant.
	 * @return
	 */
	
	public int getMaxInfestation() {
		return maxInfestation;
	}
	
	/**
	 * Get the species name of this Occupant.
	 * @return
	 */
	
	public String getName() {
		return species;
	}

	/**
	 * Returns a List of Coordinate locations for propagules
	 * produced by this Occupant.
	 * @return
	 */
	
	public List<Coordinate> getPropagules() {
		return propagules;
	}
	
	/**
	 * Retrieve the current stage of infestation of this Occupant.
	 * @return
	 */

	public int getStageOfInfestation() {
		return stageOfInfestation;
	}

	/**
	 * Indicates whether this Occupant is currently subject to 
	 * the given ControlType.
	 * @param control
	 * @return
	 */
	
	public boolean hasControl(ControlType control) {
		return controls.containsKey(control);
	}
	
	/**
	 * Indicates whether this Occupant is a (species-specific) NoData element.
	 * @return
	 */

	public boolean hasNoData() {
		return NODATA;
	}
	
	/**
	 * Increments the infestation time of this Occupant
	 * @param increment
	 */

	public void incrementInfestationTime(long increment) {
		if (infested) {
			ageOfInfestation += increment;
			cumulativeAgeOfInfestation += increment;
		}
	}
	
	/**
	 * Indicates whether this Occupant is currently subject to 
	 * any ControlType
	 * @return
	 */
	
	public boolean isControlled(){
		for(ControlType control:controls.keySet()){
			if(control!=ControlType.NONE){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Indicates whether this Occupant is infested
	 * @return
	 */

	public boolean isInfested() {
		return infested;
	}
	
	/**
	 * Indicates if this Occupant has been visited as part of
	 * a process chain.
	 * @return
	 */
	
	public boolean isVisited(){
		return visited;
	}

	/**
	 * Removes a ControlType from the Occupant.
	 * @param control
	 */
	
	public void removeControl(ControlType control) {
		controls.remove(control);
	}
	
	/**
	 * Explicitly sets the infestation age of the Occupant.
	 * @param ageOfInfestation
	 */

	public void setAgeOfInfestation(long ageOfInfestation) {
		this.ageOfInfestation = ageOfInfestation;
	}

	/**
	 * Explicitly sets the control time of a given control.
	 * @param control
	 * @param controlTime
	 */
	
	public void setControlTime(ControlType control, long controlTime) {
		controls.put(control, controlTime);
	}

	/**
	 * Sets the Disperser class associated with the Occupant.
	 * @param disperser
	 */
	
	public void setDisperser(Disperser disperser) {
		this.disperser = disperser;
	}

	/**
	 * Sets whether the Occupant is present/infested.
	 * @param infested
	 */
	
	public void setInfested(boolean infested) {
		this.infested = infested;

		if (infested) {
			wasInfested = true;
			this.stageOfInfestation = 1;
			this.maxInfestation = 1;
		} else {
			this.stageOfInfestation = 0;
		}
	}
	
	/**
	 * Sets the species name associated with the Occupant.
	 * @param species
	 */

	public void setSpecies(String species) {
		this.species = species;
	}

	/**
	 * Sets whether the Occupant is a (species-specific) NoData element.
	 * @param noData
	 */
	
	public void setNoData(boolean noData) {
		this.NODATA = noData;
	}

	/**
	 * Explicitly sets the coordinates of propagules produced by the
	 * Occupant.
	 * @param propagules
	 */
	
	public void setPropagules(List<Coordinate> propagules) {
		this.propagules = propagules;
	}

	/**
	 * Sets the stage of infestation of the Occupant.
	 * @param stageOfInfestation
	 */
	
	public void setStageOfInfestation(int stageOfInfestation) {
		this.stageOfInfestation = stageOfInfestation;

		if (stageOfInfestation > maxInfestation) {
			maxInfestation = stageOfInfestation;
		}
	}
	
	/**
	 * Sets whether the Occupant was 'visited' in the process
	 * of a chain operation.
	 * @param visited
	 */
	
	public void setVisited(boolean visited){
		this.visited=visited;
	}

	/**
	 * Sets whether the Occupant was ever controlled at some point in time.
	 * @return
	 */
	
	public boolean wasControlled(){
		return wasControlled;
	}
	
	
	/**
	 * Sets whether the Occupant was ever present at some point in time.
	 * @return
	 */
	
	public boolean wasInfested() {
		return wasInfested;
	}
}
